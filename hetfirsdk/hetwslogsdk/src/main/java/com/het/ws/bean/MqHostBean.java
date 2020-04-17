//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.het.ws.bean;

import java.io.Serializable;

public class MqHostBean implements Serializable {
    private String host;
    private int port;
    private String username;
    private String password;

    public MqHostBean() {
    }

    public String getHost() {
        return this.host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String toString() {
        return "MqHostBean{host='" + this.host + '\'' + ", port=" + this.port + ", username='" + this.username + '\'' + ", password='" + this.password + '\'' + '}';
    }
}
