//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.het.websocket.ob;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import okhttp3.Response;
import okhttp3.WebSocket;

public class EventManager {
    private static Set<IWebSocketCallback> obs = new HashSet();
    private IWebSocketCallback dataInterceptor;
    private static EventManager instance;

    public EventManager() {
    }

    public static EventManager getInstance() {
        if (instance == null) {
            Class var0 = EventManager.class;
            synchronized(EventManager.class) {
                if (null == instance) {
                    instance = new EventManager();
                }
            }
        }

        return instance;
    }

    public synchronized void registerObserver(IWebSocketCallback o) {
        if (o != null && !obs.contains(o)) {
            obs.add(o);
        }

    }

    public synchronized void unregisterObserver(IWebSocketCallback o) {
        if (obs.contains(o)) {
            obs.remove(o);
        }

    }

    public synchronized void clear() {
        obs.clear();
    }

    public synchronized void onOpen(WebSocket webSocket, Response response) {
        if (webSocket != null && response != null) {
            Iterator it = obs.iterator();

            while(it.hasNext()) {
                IWebSocketCallback mgr = (IWebSocketCallback)it.next();
                if (mgr != null) {
                    mgr.onOpen(webSocket, response);
                }
            }

        }
    }

    public synchronized void onMessage(WebSocket webSocket, String string) {
        if (webSocket != null && string != null) {
            Iterator it = obs.iterator();

            while(it.hasNext()) {
                IWebSocketCallback mgr = (IWebSocketCallback)it.next();
                if (mgr != null) {
                    mgr.onMessage(webSocket, string);
                }
            }

        }
    }

    public synchronized void onClosing(WebSocket webSocket, int code, String reason) {
        if (webSocket != null) {
            Iterator it = obs.iterator();

            while(it.hasNext()) {
                IWebSocketCallback mgr = (IWebSocketCallback)it.next();
                if (mgr != null) {
                    mgr.onClosing(webSocket, code, reason);
                }
            }

        }
    }

    public synchronized void onClosed(WebSocket webSocket, int code, String reason) {
        if (webSocket != null) {
            Iterator it = obs.iterator();

            while(it.hasNext()) {
                IWebSocketCallback mgr = (IWebSocketCallback)it.next();
                if (mgr != null) {
                    mgr.onClosed(webSocket, code, reason);
                }
            }

        }
    }

    public synchronized void onFailure(WebSocket webSocket, Throwable t, Response response) {
        if (webSocket != null) {
            Iterator it = obs.iterator();

            while(it.hasNext()) {
                IWebSocketCallback mgr = (IWebSocketCallback)it.next();
                if (mgr != null) {
                    mgr.onFailure(webSocket, t, response);
                }
            }

        }
    }
}
