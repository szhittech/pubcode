package com.het.ws;

import android.content.Context;

import com.het.ws.service.HtmlService;

public class WsSDK {

    public static void bootstrap(Context context) {
        HtmlService.start(context);
    }

    public static void shutdown(Context context) {
        HtmlService.stop(context);
    }
}
