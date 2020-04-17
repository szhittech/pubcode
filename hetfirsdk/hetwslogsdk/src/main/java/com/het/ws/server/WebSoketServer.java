package com.het.ws.server;

import com.het.ws.util.Logc;
import com.het.ws.util.Utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.InetAddress;

import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okio.ByteString;

public class WebSoketServer {
    private MockWebServer mockWebServer;
    private IWebSocketServerState webSocketServerState;

    public WebSoketServer() {
        mockWebServer = new MockWebServer();
        mockWebServer.enqueue(withWebSocketUpgrade());
        mockWebServer.enqueue(withWebSocketUpgrade());

    }

    public void setWebSocketServerState(IWebSocketServerState webSocketServerState) {
        this.webSocketServerState = webSocketServerState;
    }

    public void start(int port) {
        try {
            InetAddress ipAddr = Utils.getLocalIpAddress();
            mockWebServer.start(ipAddr, port);
            Logc.i("ws://" + mockWebServer.getHostName() + ":" + mockWebServer.getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void start() {
        start(8081);
    }

    private MockResponse withWebSocketUpgrade() {
        MockResponse response = new MockResponse();
        response.withWebSocketUpgrade(new WebSocketListener() {
            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                super.onClosed(webSocket, code, reason);
                Logc.i("onClosed code:" + code + " reason:" + reason);
                WebSocketClients.getInstance().unregister(webSocket);
                mockWebServer.enqueue(withWebSocketUpgrade());
                if (webSocketServerState != null && !WebSocketClients.getInstance().hasClient()) {
                    webSocketServerState.onClientEmpty();
                }
            }

            @Override
            public void onClosing(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                super.onClosing(webSocket, code, reason);
                Logc.i("onClosing code:" + code + " reason:" + reason);
                onClosed(webSocket, 1, reason);
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                super.onFailure(webSocket, t, response);
                Logc.i("throwable:" + t);
                Logc.i("onFailure response:" + response);
                onClosed(webSocket, 1, t.getMessage());
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
                super.onMessage(webSocket, text);
                Logc.i("server onMessage");
                Logc.i("message:" + text);
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
                super.onMessage(webSocket, bytes);
                Logc.i("message:" + bytes.toString());
            }

            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                super.onOpen(webSocket, response);
                Logc.i("onOpen response:" + response);
                if (webSocketServerState != null && !WebSocketClients.getInstance().hasClient()) {
                    webSocketServerState.onClientTiger();
                }
                WebSocketClients.getInstance().register(webSocket);
            }
        });
        return response;
    }

    public void stop() {
        if (mockWebServer != null) {
            try {
                mockWebServer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mockWebServer = null;
        }
    }
}
