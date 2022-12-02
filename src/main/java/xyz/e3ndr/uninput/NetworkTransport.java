package xyz.e3ndr.uninput;

import java.awt.TrayIcon.MessageType;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.uninput.Config.BorderConfig;
import xyz.e3ndr.uninput.events.UEvent;
import xyz.e3ndr.uninput.events.UKeyboardPressEvent;
import xyz.e3ndr.uninput.events.UKeyboardReleaseEvent;
import xyz.e3ndr.uninput.events.UMouseMoveEvent;
import xyz.e3ndr.uninput.events.UMousePressEvent;
import xyz.e3ndr.uninput.events.UMouseWheelEvent;
import xyz.e3ndr.uninput.events.USpawnEvent;

public class NetworkTransport {
    private static FastLogger logger = new FastLogger();

    private Map<String, Target> targets = new HashMap<>();
    private Uninput uninput;

    public static void setupKryo(Kryo kryo) {
        // We MUST retain this ordering. Kryo generates it's internal IDs using the
        // registration order.
        kryo.register(byte[].class);
        kryo.register(UKeyboardPressEvent.class);
        kryo.register(UKeyboardReleaseEvent.class);
        kryo.register(UMouseMoveEvent.class);
        kryo.register(UMousePressEvent.class);
        kryo.register(UMouseWheelEvent.class);
        kryo.register(USpawnEvent.class);
    }

    @SneakyThrows
    public NetworkTransport(Uninput uninput) {
        this.uninput = uninput;

        int port = this.uninput.getConfig().getPort();

        // Connect to all of the targets.
        for (BorderConfig borderConfig : this.uninput.getConfig().getBorders().values()) {
            if (borderConfig == null) continue;

            String targetName = borderConfig.getTargetDisplay().split("=")[0];
            this.targets.put(targetName, new Target(targetName, port, targetName));
        }

        // Open our listener.
        Server server = new Server();
        setupKryo(server.getKryo());
        server.addListener(new KryoServerListener());
        server.start();
        server.bind(port);
    }

    public boolean send(String targetName, UEvent event) {
        Target target = this.targets.get(targetName);

        if ((target == null) || !target.isAlive()) {
            logger.warn("Unable to send event to %s, not connected.", targetName);
            return false;
        }

        target.send(event);
        return true;
    }

    private class KryoServerListener extends Listener {
        @Override
        public void connected(Connection conn) {
            logger.info("New client connected.");
        }

        @Override
        public void disconnected(Connection conn) {
            logger.info("Lost connection to a client.");
        }

        @Override
        public void received(Connection conn, Object obj) {
            UEvent event = (UEvent) obj;

            logger.trace("Received: %s", event);
            uninput.remoteEvent(event);
        }
    }

    private class Target extends Listener {
        private FastLogger logger = new FastLogger();
        private String targetName;

        private boolean hadConnected = false;
        private Client client;

        public Target(String hostname, int port, String targetName) {
            this.targetName = targetName;
            this.logger = new FastLogger(String.format("NetworkTarget: %s", this.targetName));
            targets.put(this.targetName, this);

            this.client = new Client();
            setupKryo(this.client.getKryo());
            this.client.start();
            try {
                this.client.connect(5000, resolve(hostname), port);
            } catch (IOException e) {
                e.printStackTrace();
                this.disconnected(null);
            }
        }

        public void send(UEvent event) {
            this.client.sendTCP(event);
        }

        public boolean isAlive() {
            return this.client.isConnected();
        }

        @Override
        public void connected(Connection conn) {
            this.hadConnected = true;
            this.logger.info("Connected to %s successfully.", this.targetName);
            Tray.sendNotification("Uninput Connected", String.format("Connected to %s successfully.", this.targetName), MessageType.INFO);
        }

        @Override
        public void disconnected(Connection _1) {
            if (this.hadConnected) {
                this.logger.info("Disconnected from %s.", this.targetName);
                Tray.sendNotification("Uninput Disconnected", String.format("Disconnected from %s, reconnecting.", this.targetName), MessageType.INFO);

                this.hadConnected = false;
            }

            new Thread(() -> {
                try {
                    TimeUnit.SECONDS.sleep(10);
                    this.client.reconnect();
                } catch (Exception ignored) {}
            }).start();
        }

    }

    private static String resolve(String hostname) {
        try {
            String address = InetAddress
                .getByName("www.example.com")
                .getHostAddress();

            logger.debug("Resolved: %s -> %s", hostname, address);

            return address;
        } catch (UnknownHostException e) {
            return hostname;
        }
    }

}
