//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.het.websocket.log;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.het.log.Logc;
import com.het.websocket.MqClient;
import com.het.websocket.ob.EventManager;
import com.het.websocket.ob.IWebSocketCallback;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import okhttp3.Response;
import okhttp3.WebSocket;

public class LogcatService extends IntentService {
    private static final String TAG = "LogcatService";
    private static final String KEY_PORT = "key_port";
    private static int mPort = 8080;
    private static boolean mIsRunning;
    private ServerSocket mServerSocket;
    private RequestHandler mRequestHandler;
    private WebSocket webSocket;

    public LogcatService() {
        this("LogcatService");
    }

    public LogcatService(String name) {
        super(name);
    }

    public void onCreate() {
        super.onCreate();
        mIsRunning = true;
        this.mRequestHandler = new RequestHandler(this.getApplicationContext());
        EventManager.getInstance().registerObserver(new IWebSocketCallback() {
            public void onOpen(WebSocket ws, Response response) {
                LogcatService.this.webSocket = ws;
                MqTigger tig = MqClient.getInstance().getMqTigger();
                if (tig != null) {
                    tig.setWebSocket(LogcatService.this.webSocket);
                }

            }

            public void onMessage(WebSocket ws, String string) {
                LogTool.TAG = string;
            }

            public void onClosing(WebSocket webSocket, int code, String reason) {
            }

            public void onClosed(WebSocket webSocket, int code, String reason) {
            }

            public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            }
        });
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        mIsRunning = true;
        int cmd = intent.getIntExtra("cmd", -1);
        if (cmd == 1) {
            ;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            mPort = intent.getIntExtra("key_port", 8080);
        }

        try {
            this.mServerSocket = new ServerSocket(mPort);
            Logc.i("####mqtt ServerSocket.port:" + mPort);

            while(mIsRunning) {
                Socket socket = this.mServerSocket.accept();
                this.mRequestHandler.handle(socket);
                socket.close();
            }
        } catch (SocketException var3) {
            ;
        } catch (IOException var4) {
            Log.e("LogcatService", "####mqtt Web server error.", var4);
        } catch (Exception var5) {
            ;
        }

    }

    public void onDestroy() {
        super.onDestroy();
        this.stop();
    }

    public static void start(Context context, int port) {
        Intent intent = new Intent(context, LogcatService.class);
        intent.putExtra("key_port", port);
        context.startService(intent);
    }

    public static void shutDown(Context context) {
        context.stopService(new Intent(context, LogcatService.class));
    }

    public void stop() {
        try {
            mIsRunning = false;
            if (null != this.mServerSocket) {
                this.mServerSocket.close();
                this.mServerSocket = null;
            }
        } catch (Exception var2) {
            Log.e("LogcatService", "####mqttError closing the server socket.", var2);
        }

    }

    public static boolean isRunning() {
        return mIsRunning;
    }

    public static int getPort() {
        return mPort;
    }
}
