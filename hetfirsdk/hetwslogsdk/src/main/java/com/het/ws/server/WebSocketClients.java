package com.het.ws.server;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import okhttp3.WebSocket;

public class WebSocketClients {
    private static WebSocketClients instance;
    private static Map<Integer, WebSocket> obs = new HashMap<>();

    public static WebSocketClients getInstance() {
        if (instance == null) {
            synchronized (WebSocketClients.class) {
                if (null == instance) {
                    instance = new WebSocketClients();
                }
            }
        }

        return instance;
    }

    public boolean hasClient(){
        return obs.size() > 0 ? true : false;
    }

    public synchronized void register(WebSocket o) {
        if (o != null && !obs.containsKey(o.hashCode())) {
            obs.put(o.hashCode(), o);
        }

    }

    public synchronized void unregister(WebSocket o) {
        if (obs.containsKey(o.hashCode())) {
            obs.remove(o.hashCode());
        }

    }

    public synchronized void post(String object) {
        if (object != null) {
            Iterator<Map.Entry<Integer, WebSocket>> entries = obs.entrySet().iterator();
            while (entries.hasNext()) {
                Map.Entry<Integer, WebSocket> entry = entries.next();
                Integer key = entry.getKey();
                WebSocket value = entry.getValue();
                if (value != null) {
                    value.send(object);
                }
            }
        }
    }
}
