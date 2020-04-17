package com.het.ws.event;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Event {
    private static Event instance;
    private static Set<IEvent> obs = new HashSet();

    public static Event getInstance() {
        if (instance == null) {
            synchronized (Event.class) {
                if (null == instance) {
                    instance = new Event();
                }
            }
        }

        return instance;
    }

    public synchronized void register(IEvent o) {
        if (o != null && !obs.contains(o)) {
            obs.add(o);
        }

    }

    public synchronized void unregister(IEvent o) {
        if (obs.contains(o)) {
            obs.remove(o);
        }

    }

    public synchronized void clear() {
        obs.clear();
    }

    public synchronized <T> void post(T object) {
        if (object != null) {
            Iterator it = obs.iterator();
            while(it.hasNext()) {
                IEvent mgr = (IEvent)it.next();
                if (mgr != null) {
                    mgr.onEvent(object);
                }
            }

        }
    }

}
