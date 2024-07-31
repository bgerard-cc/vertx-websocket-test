package org.example;

import io.vertx.core.Vertx;
import io.vertx.core.http.WebSocket;
import io.vertx.core.http.WebSocketConnectOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.WebSocketFrame;
import io.vertx.core.json.JsonObject;

public class WebSocketClient {
    private final Vertx vertx;
    private final String host;
    private final Integer port;
    private final String uri;
    private long reconnectDelay = 1000;
    private static final long MAX_RECONNECT_DELAY = 5000;

    public WebSocketClient(String host, Integer port, String uri) {
        this.vertx = Vertx.vertx();
        this.host = host;
        this.port = port;
        this.uri = uri;
    }

    private void handleTextMessage(WebSocket ws, String message) {
        try {
            JsonObject json = new JsonObject(message);
            System.out.println("Parsed JSON: " + json);
        } catch (Exception e) {
            System.out.println("Failed to parse JSON: " + e.getMessage());
        }
    }

    private void handleFrame(WebSocket ws, WebSocketFrame frame) {
        if (frame.isText()) {
            String message = frame.textData();
            System.out.println("Raw text: " + message);
        } else if (frame.isClose()) {
            System.out.println("Got close frame");
            ws.close();
        } else if (frame.isPing()) {
            System.out.println("Got ping frame");
            // Looks like the lib automatically responds with a pong
        }
    }

    private void handleClose(Void v) {
        System.out.println("Socket closed gracefully");
        scheduleReconnect();
    }

    private void handleException(Throwable e) {
        System.out.println("Socket force closed: " + e);
    }

    public void connect() {
        HttpClient client = vertx.createHttpClient();

        WebSocketConnectOptions options = new WebSocketConnectOptions()
                .setHost(this.host)
                .setPort(this.port)
                .setURI(this.uri);

        client.webSocket(options)
                .onComplete(res -> {
                    if (res.succeeded()) {
                        WebSocket ws = res.result();
                        System.out.println("Connected!");
                        ws.frameHandler(frame -> handleFrame(ws, frame))
                                .closeHandler(this::handleClose)
                                .exceptionHandler(this::handleException);
                    } else {
                        System.out.println("Failed to connect: " + res.cause().getMessage());
                        scheduleReconnect();
                    }
                });
    }

    private void scheduleReconnect() {
        vertx.setTimer(reconnectDelay, id -> {
            connect();
            reconnectDelay = Math.min(reconnectDelay * 2, MAX_RECONNECT_DELAY);
        });
    }
}
