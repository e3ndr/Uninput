package xyz.e3ndr.uninput;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.jetbrains.annotations.Nullable;

import co.casterlabs.rakurai.io.http.HttpResponse;
import co.casterlabs.rakurai.io.http.HttpSession;
import co.casterlabs.rakurai.io.http.StandardHttpStatus;
import co.casterlabs.rakurai.io.http.server.HttpListener;
import co.casterlabs.rakurai.io.http.server.HttpServer;
import co.casterlabs.rakurai.io.http.server.HttpServerBuilder;
import co.casterlabs.rakurai.io.http.websocket.Websocket;
import co.casterlabs.rakurai.io.http.websocket.WebsocketListener;
import co.casterlabs.rakurai.io.http.websocket.WebsocketSession;
import co.casterlabs.rakurai.json.Rson;
import co.casterlabs.rakurai.json.element.JsonObject;
import lombok.NonNull;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.uninput.Config.BorderConfig;
import xyz.e3ndr.uninput.events.UEvent;
import xyz.e3ndr.uninput.events.UEvent.UEventType;

public class NetworkTransport {
    private static final String WS_URL_FORMAT = "ws://%s:%d/uninput/ws";

    private final Map<String, Target> targets = new HashMap<>();
    private final FastLogger logger = new FastLogger();

    private Uninput uninput;
    private HttpServer server;

    @SneakyThrows
    public NetworkTransport(Uninput uninput) {
        this.uninput = uninput;

        int port = this.uninput.getConfig().getPort();

        // Connect to all of the targets.
        for (BorderConfig borderConfig : this.uninput.getConfig().getBorders().values()) {
            if (borderConfig == null) continue;

            String targetName = borderConfig.getTargetDisplay().split("=")[0];
            URI uri = new URI(String.format(WS_URL_FORMAT, targetName, port));

            this.targets.put(targetName, new Target(uri, targetName));
        }

        // Open our listener.
        this.server = HttpServerBuilder
            .getUndertowBuilder()
            .setPort(port)
            .build(new HttpListener() {

                @Override
                public @Nullable HttpResponse serveSession(@NonNull String host, @NonNull HttpSession session, boolean secure) {
                    return HttpResponse.newFixedLengthResponse(
                        StandardHttpStatus.OK,
                        "<!DOCTYPE html><html>Checkout <a href=\"https://github.com/e3ndr/uninput\">https://github.com/e3ndr/uninput</a> :^)</html>"
                    );
                }

                @Override
                public @Nullable WebsocketListener serveWebsocketSession(@NonNull String host, @NonNull WebsocketSession session, boolean secure) {
                    String path = session.getUri();

                    if (path.equals("/uninput/ws")) {
                        return new RemoteListener();
                    }

                    return null;
                }
            });

        this.server.start(); // Open up http://127.0.0.1:8080
    }

    public void send(String targetName, UEvent event) {
        Target target = this.targets.get(targetName);

        if ((target == null) || !target.isOpen()) {
            this.logger.warn("Unable to send event to %s, not connected.", targetName);
            return;
        }

        target.send(Rson.DEFAULT.toJsonString(event));
    }

    private class RemoteListener implements WebsocketListener {

        @SneakyThrows
        @Override
        public void onText(Websocket websocket, String raw) {
            JsonObject message = Rson.DEFAULT.fromJson(raw, JsonObject.class);

            UEvent event = UEventType.parseEvent(message);

            uninput.remoteEvent(event);
        }

    }

    private class Target extends WebSocketClient {
        private FastLogger logger = new FastLogger();
        private String targetName;

        public Target(URI serverUri, String targetName) {
            super(serverUri);
            this.targetName = targetName;
            this.logger = new FastLogger(String.format("NetworkTarget: %s", this.targetName));
            targets.put(this.targetName, this);

            this.setConnectionLostTimeout(5);
            this.setReuseAddr(true);
            this.setTcpNoDelay(true);
            this.connect();
        }

        @Override
        public void onOpen(ServerHandshake handshakedata) {
            this.logger.info("Connected to %s successfully.", this.targetName);

        }

        @Override
        public void onMessage(String raw) {} // This class is only used for sending!

        @Override
        public void onClose(int code, String reason, boolean remote) {
            this.logger.info("Disconnected from %s, isRemote=%b.", this.targetName, remote);

            new Thread(() -> {
                try {
                    TimeUnit.SECONDS.sleep(10);
                } catch (InterruptedException ignored) {}
                this.reconnect();
            }).start();
        }

        @Override
        public void onError(Exception ex) {
            this.logger.exception(ex);
        }

    }

}
