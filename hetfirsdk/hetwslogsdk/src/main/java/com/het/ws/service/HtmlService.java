package com.het.ws.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.het.ws.log.RequestHandler;
import com.het.ws.server.IWebSocketServerState;
import com.het.ws.server.WebSoketServer;
import com.het.ws.util.Logc;
import com.het.ws.util.Utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class HtmlService extends IntentService {
    private static int port = 8080;
    private ServerSocket serverSocket = null;
    private RequestHandler requestHandler = null;
    private WebSoketServer webSoketServer;

    public static void start(Context context) {
        start(context, port);
    }

    public static void start(Context context, int p) {
        port = p;
        Intent intent = new Intent(context, HtmlService.class);
        context.startService(intent);
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, HtmlService.class));
    }

    public HtmlService() {
        this("HtmlService");
    }

    public HtmlService(String name) {
        super(name);
    }

    public void onCreate() {
        super.onCreate();
        requestHandler = new RequestHandler(this.getApplicationContext());
        webSoketServer = new WebSoketServer();
        webSoketServer.setWebSocketServerState(new IWebSocketServerState() {
            @Override
            public void onClientTiger() {
                SysLogService.start(HtmlService.this);
            }

            @Override
            public void onClientEmpty() {
                SysLogService.stop(HtmlService.this);
            }
        });

        Logc.e("onCreate");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logc.e("onHandleIntent");
        webSoketServer.start();
        listenHtml();
    }


    private void listenHtml() {
        try {
            InetAddress ipAddr = Utils.getLocalIpAddress();
            this.serverSocket = new ServerSocket(port);
            Logc.i("http://" + ipAddr.getHostName()+":"+port);
            while (true) {
                Socket socket = serverSocket.accept();
                requestHandler.handle(socket);
                socket.close();
            }
        } catch (SocketException var3) {
        } catch (IOException var4) {
            Log.e("LogcatService", "####mqtt Web server error.", var4);
        } catch (Exception var5) {
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logc.e("onDestroy");
        webSoketServer.stop();
        try {
            if (null != this.serverSocket) {
                this.serverSocket.close();
                this.serverSocket = null;
            }
        } catch (Exception var2) {
            Log.e("LogcatService", "####mqttError closing the server socket.", var2);
        }
    }
}
