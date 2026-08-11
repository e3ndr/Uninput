package xyz.e3ndr.uninput.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import co.casterlabs.commons.io.bytes.reading.ByteReader;
import co.casterlabs.commons.io.bytes.reading.StreamByteReader;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import xyz.e3ndr.fastloggingframework.logging.FastLogger;
import xyz.e3ndr.uninput.Uninput;
import xyz.e3ndr.uninput.events.UEvent;
import xyz.e3ndr.uninput.events.UPingEvent;

public class Server {
    private static FastLogger logger = new FastLogger();

    @SneakyThrows
    public static void start() {
        try (ServerSocket socket = new ServerSocket(Uninput.config.port)) {
            logger.info("Server started on port " + Uninput.config.port);
            while (true) {
                Socket client = socket.accept();
                client.setTcpNoDelay(true);
                client.setSoTimeout(5000);

                new Thread(new ClientHandler(client)).start();
            }
        }
    }

    @RequiredArgsConstructor
    private static class ClientHandler implements Runnable {
        private final Socket client;

        @Override
        public void run() {
            try {
                ByteReader reader = new StreamByteReader(this.client.getInputStream());

                while (true) {
                    UEvent event = UEvent.deserialize(reader);
                    if (event instanceof UPingEvent) continue; // no need to handle.

                    Uninput.remoteEvent(event);
                }
            } catch (IOException ignored) {} finally {
                try {
                    this.client.close();
                } catch (IOException ignored) {}
            }
        }
    }

}
