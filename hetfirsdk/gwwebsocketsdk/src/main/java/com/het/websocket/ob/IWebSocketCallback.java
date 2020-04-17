package com.het.websocket.ob;

import okhttp3.Response;
import okhttp3.WebSocket;

/**
 * -----------------------------------------------------------------
 * Copyright (C) 2014-2017, by het, Shenzhen, All rights reserved.
 * -----------------------------------------------------------------
 * <p>
 * <p>描述：</p>
 * 名称: IWebSocketCallback <br>
 * 作者: uuxia-mac<br>
 * 版本: 1.0<br>
 * 日期: 2017/12/15 22:33<br>
 **/
public interface IWebSocketCallback {
    void onOpen(WebSocket webSocket, Response response);
    void onMessage(WebSocket ws, String string);
    void onClosing(WebSocket webSocket, int code, String reason);
    void onClosed(WebSocket webSocket, int code, String reason);
    void onFailure(WebSocket webSocket, Throwable t, Response response);
}
