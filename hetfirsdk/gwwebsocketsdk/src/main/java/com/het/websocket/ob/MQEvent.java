//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.het.websocket.ob;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class MQEvent {
    private static Set<IMQEvent> obs = new HashSet();
    private static MQEvent instance;

    public MQEvent() {
    }

    public static MQEvent getInstance() {
        if (instance == null) {
            Class var0 = MQEvent.class;
            synchronized(MQEvent.class) {
                if (null == instance) {
                    instance = new MQEvent();
                }
            }
        }

        return instance;
    }

    public synchronized void registerObserver(IMQEvent o) {
        if (o != null && !obs.contains(o)) {
            obs.add(o);
        }

    }

    public synchronized void unregisterObserver(IMQEvent o) {
        if (obs.contains(o)) {
            obs.remove(o);
        }

    }

    public synchronized void clear() {
        obs.clear();
    }

    public synchronized void notify(int type, Object response) {
        if (response != null) {
            Iterator it = obs.iterator();

            while(it.hasNext()) {
                IMQEvent mgr = (IMQEvent)it.next();
                if (mgr != null) {
                    mgr.onNotify(type, response);
                }
            }

        }
    }
}
