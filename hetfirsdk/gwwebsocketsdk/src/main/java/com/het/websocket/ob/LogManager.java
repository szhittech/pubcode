package com.het.websocket.ob;

import com.het.websocket.log.LogTool;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class LogManager {
    private static Set<LogTool.ILogNotify> obs = new HashSet<LogTool.ILogNotify>();
    private LogTool.ILogNotify dataInterceptor;
    private static LogManager instance;

    public static LogManager getInstance() {
        if (instance == null) {
            synchronized (LogManager.class) {
                if (null == instance) {
                    instance = new LogManager();
                }
            }
        }
        return instance;
    }

    public synchronized void registerObserver(LogTool.ILogNotify o) {
        if (o != null) {
            if (!obs.contains(o)) {
                obs.add(o);
            }
        }
    }

    public synchronized void unregisterObserver(LogTool.ILogNotify o) {
        if (obs.contains(o)) {
            obs.remove(o);
        }
    }

    public synchronized void clear() {
        obs.clear();
    }

    public synchronized void post(String reason) {
        if (reason == null)
            return;
        Iterator<LogTool.ILogNotify> it = obs.iterator();
        while (it.hasNext()) {
            LogTool.ILogNotify mgr = it.next();
            if (mgr!=null) {
                mgr.notify(reason);
            }
        }
    }
}
