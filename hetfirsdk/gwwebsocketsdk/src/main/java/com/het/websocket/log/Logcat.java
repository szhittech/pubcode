//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.het.websocket.log;

import android.content.Context;
import android.util.Log;
import com.het.websocket.util.NetworkUtils;

public class Logcat {
    private static final String TAG = "Logcat";

    public Logcat() {
    }

    public static void startLogCatServer(Context context) {
        startLogCatServer(context, 8080);
    }

    public static void startLogCatServer(Context context, int port) {
        LogcatService.start(context, port);
        Log.d("Logcat", NetworkUtils.getWebLogcatAddress(context, port));
    }

    public static void shutDownServer(Context context) {
        LogcatService.shutDown(context);
    }

    public static boolean isServerRunning() {
        return LogcatService.isRunning();
    }

    public static void jumpToLogcatActivity(Context context) {
    }
}
