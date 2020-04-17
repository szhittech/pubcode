//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.het.websocket.http;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.Map;

public class BasicAuthenticator extends Authenticator {
    String userName;
    String password;

    public BasicAuthenticator(String userName, String password) {
        this.userName = userName;
        this.password = password;
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(this.userName, this.password.toCharArray());
    }

    public static void main(String[] args) {
        String html = null;

        try {
            Map<String, String> headers = new HashMap();
            headers.put("username", "admin");
            headers.put("password", "public");
            html = SimpleHttpUtils.get("https://192.168.1.100:8421/api/v2/nodes/emq@127.0.0.1/clients", headers);
        } catch (Exception var3) {
            var3.printStackTrace();
        }

        System.out.println(html);
    }
}
