//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.het.websocket.ob;

public class HttpEvent {
    private IHttpEvent dataInterceptor;
    private static HttpEvent instance;

    public HttpEvent() {
    }

    public static HttpEvent getInstance() {
        if (instance == null) {
            Class var0 = HttpEvent.class;
            synchronized(HttpEvent.class) {
                if (null == instance) {
                    instance = new HttpEvent();
                }
            }
        }

        return instance;
    }

    public void setDataInterceptor(IHttpEvent dataInterceptor) {
        this.dataInterceptor = dataInterceptor;
    }

    public Object notify(int type, Object response) {
        return this.dataInterceptor == null ? null : this.dataInterceptor.onData(type, response);
    }
}
