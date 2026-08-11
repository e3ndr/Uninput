package xyz.e3ndr.uninput.net;

import java.awt.TrayIcon.MessageType;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import co.casterlabs.commons.io.bytes.writing.ByteWriter;
import co.casterlabs.commons.io.bytes.writing.StreamByteWriter;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.uninput.Tray;
import xyz.e3ndr.uninput.Uninput;
import xyz.e3ndr.uninput.config.Config.BorderConfig;
import xyz.e3ndr.uninput.events.UEvent;
import xyz.e3ndr.uninput.events.UPingEvent;

public class NetworkTransport {
    private static FastLogger logger = new FastLogger();

    private final Map<String, Target> targets = new HashMap<>();

    @SneakyThrows
    public void init() {
        // Connect to all of the targets.
        for (BorderConfig borderConfig : Uninput.config.borders.values()) {
            if (borderConfig == null) continue;

            String targetName = borderConfig.getTargetDisplay().split("=")[0];
            this.targets.put(targetName, new Target(targetName, Uninput.config.port, targetName));
        }

        // Open our listener.
        new Thread(Server::start).start();
    }

    public boolean isConnected(String targetName) {
        Target target = this.targets.get(targetName);
        return target != null && target.isAlive();
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

    private class Target {
        private final FastLogger logger;

        private final String hostname;
        private final int port;
        private final String targetName;

        private boolean hadConnected = false;
        private Socket client;
        private ByteWriter writer;

        public Target(String hostname, int port, String targetName) {
            this.hostname = hostname;
            this.port = port;
            this.targetName = targetName;
            this.logger = new FastLogger(String.format("NetworkTarget: %s", this.targetName));
            targets.put(this.targetName, this);

            new Thread(this::connect).start();
        }

        public void connect() {
            this.client = new Socket();

            try {
                this.client.connect(new InetSocketAddress(resolve(this.hostname), this.port), 5000);
                this.client.setTcpNoDelay(true);
                this.client.setSoTimeout(5000);

                this.writer = new StreamByteWriter(this.client.getOutputStream());

                this.hadConnected = true;
                this.logger.info("Connected to %s successfully.", this.targetName);
                Tray.sendNotification("Uninput Connected", String.format("Connected to %s successfully.", this.targetName), MessageType.INFO);

                while (!this.client.isClosed()) {
                    this.sendSync(UPingEvent.INSTANCE);
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (InterruptedException ignored) {}
                }

                if (this.hadConnected) {
                    this.logger.info("Disconnected from %s.", this.targetName);
                    Tray.sendNotification("Uninput Disconnected", String.format("Disconnected from %s, reconnecting.", this.targetName), MessageType.INFO);

                    this.hadConnected = false;
                }
            } catch (IOException ignored) {} finally {
                try {
                    this.client.close();
                } catch (IOException ignored) {}
                this.client = null;
            }

            new Thread(() -> {
                this.logger.info("Attempting to reconnect to %s...", this.targetName);
                try {
                    TimeUnit.SECONDS.sleep(2);
                    this.connect();
                } catch (Exception ignored) {}
            }).start();
        }

        public synchronized void sendSync(UEvent event) throws IOException {
            UEvent.serialize(event, this.writer);
        }

        public synchronized void send(UEvent event) {
            try {
                this.sendSync(event);
            } catch (IOException ignored) {}
        }

        public boolean isAlive() {
            return this.client != null;
        }

    }

    private static String resolve(String hostname) {
        try {
            String address = InetAddress
                .getByName(hostname)
                .getHostAddress();

            logger.debug("Resolved: %s -> %s", hostname, address);

            return address;
        } catch (UnknownHostException e) {
            return hostname;
        }
    }

}
