//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.het.websocket;

import android.content.Context;
import com.fsix.mqtt.MqttConnManager;
import com.het.log.Logc;
import com.het.websocket.bean.ApiResult;
import com.het.websocket.log.Logcat;
import com.het.websocket.ob.EventManager;
import com.het.websocket.util.GsonUtil;
import com.het.websocket.util.Utils;
import java.io.IOException;
import java.net.InetAddress;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class WsBootstrap {
    static final String TAG = "MockWebServer-";
    private static MockWebServer mockWebServer;
    public static WebSocket webSocket;

    public WsBootstrap() {
    }

    public static void send(ApiResult api) {
        if (webSocket != null) {
            String json = GsonUtil.getInstance().toJson(api);
            webSocket.send(json);
        }

    }

    public static void init(Context context) {
        MqClient.getInstance().start(context);
        (new Thread(new Runnable() {
            public void run() {
                WsBootstrap.bootstrap();
            }
        })).start();
        Logcat.startLogCatServer(context);
    }

    public static void destroy(Context context) {
        MqClient.getInstance().stop();
        (new Thread(new Runnable() {
            public void run() {
                WsBootstrap.shutdown();
            }
        })).start();
        Logcat.shutDownServer(context);
        MqttConnManager.getInstances().stop();
    }

    public static void bootstrap() {
        try {
            mockWebServer = new MockWebServer();
            InetAddress ipAddr = Utils.getLocalIpAddress();
            mockWebServer.start(ipAddr, 8081);
            initMockServer();
            Logc.i("start websocket:" + mockWebServer.getHostName() + ":" + mockWebServer.getPort());
        } catch (IOException var1) {
            var1.printStackTrace();
        }

    }

    public static void shutdown() {
        if (mockWebServer != null) {
            try {
                mockWebServer.close();
                mockWebServer = null;
            } catch (IOException var1) {
                var1.printStackTrace();
            }
        }

    }

    private static void initMockServer() {
        mockWebServer.enqueue((new MockResponse()).withWebSocketUpgrade(new WebSocketListener() {
            public void onOpen(WebSocket ws, Response response) {
                Logc.i("server onOpen");
                Logc.i("server request header:" + response.request().headers());
                Logc.i("server response header:" + response.headers());
                Logc.i("server response:" + response);
                WsBootstrap.webSocket = ws;
                EventManager.getInstance().onOpen(ws, response);
            }

            public void onMessage(WebSocket ws, String string) {
                Logc.i("server onMessage");
                Logc.i("message:" + string);
                EventManager.getInstance().onMessage(ws, string);
                WsBootstrap.webSocket = ws;
            }

            public void onClosing(WebSocket webSocket, int code, String reason) {
                Logc.i("server onClosing");
                Logc.i("code:" + code + " reason:" + reason);
                EventManager.getInstance().onClosing(webSocket, code, reason);
                WsBootstrap.shutdown();
                WsBootstrap.bootstrap();
            }

            public void onClosed(WebSocket webSocket, int code, String reason) {
                Logc.i("server onClosed");
                Logc.i("code:" + code + " reason:" + reason);
                EventManager.getInstance().onClosed(webSocket, code, reason);
            }

            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                Logc.i("server onFailure");
                Logc.i("throwable:" + t);
                Logc.i("response:" + response);
                EventManager.getInstance().onFailure(webSocket, t, response);
            }
        }));
    }
}
