package com.het.websocket.bean;

import java.io.Serializable;

public class ServerInfo implements Serializable {
    private String serverIp;
    private int port;
    private String appName;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "ServerInfo{" +
                "serverIp='" + serverIp + '\'' +
                ", port=" + port +
                '}';
    }
}
