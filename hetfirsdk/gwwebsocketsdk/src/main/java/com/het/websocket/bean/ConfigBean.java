package com.het.websocket.bean;

import com.het.gwlog.bean.LogBean;

import java.io.Serializable;

public class ConfigBean implements Serializable{
    private LogBean logBean;
    private ServerInfo serverInfo;

    public ConfigBean() {
    }

    public ConfigBean(LogBean logBean, ServerInfo serverInfo) {
        this.logBean = logBean;
        this.serverInfo = serverInfo;
    }

    public LogBean getLogBean() {
        return logBean;
    }

    public void setLogBean(LogBean logBean) {
        this.logBean = logBean;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    @Override
    public String toString() {
        return "ConfigBean{" +
                "logBean=" + logBean +
                ", serverInfo=" + serverInfo +
                '}';
    }
}
