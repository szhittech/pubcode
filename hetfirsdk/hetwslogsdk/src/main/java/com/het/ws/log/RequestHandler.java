//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.het.ws.log;

import android.content.Context;
import android.content.res.AssetManager;

import com.het.ws.util.Utils;
import com.het.ws.util.Logc;
import com.het.ws.util.NetUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class RequestHandler {
    private final AssetManager mAssets;
    private String appName;
    private Context context;

    public RequestHandler(Context context) {
        this.context = context;
        this.mAssets = context.getResources().getAssets();
        this.appName = Utils.getAppName(context);
    }

    private String processPostData(BufferedReader reader) throws IOException {
        int contentLength = 0;
        String line = reader.readLine();

        while(line != null) {
            System.out.println(line);
            line = reader.readLine();
            if ("".equals(line)) {
                break;
            }

            if (line.indexOf("Content-Length") != -1) {
                contentLength = Integer.parseInt(line.substring(line.indexOf("Content-Length") + 16));
            }
        }

        String data = null;
        Logc.i("begin read posted data......");
        if (contentLength != 0) {
            char[] buf = new char[contentLength];
            reader.read(buf, 0, contentLength);
            data = new String(buf);
            Logc.i("The data user posted: " + data);
        }

        return data;
    }

    public void handle(Socket socket) throws IOException {
        BufferedReader reader = null;
        PrintStream output = null;

        try {
            String route = null;
            int type = 0;
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            for(String line = reader.readLine(); line != null; line = reader.readLine()) {
                //Logc.i("#@@@@@@@@@@@@### " + line);
                int start;
                int end;
                if (line.startsWith("GET /")) {
                    start = line.indexOf(47) + 1;
                    end = line.indexOf(32, start);
                    route = line.substring(start, end);
                    break;
                }

                if (line.startsWith("POST /")) {
                    start = line.indexOf(47) + 1;
                    end = line.indexOf(32, start);
                    String tag = line.substring(start, end);
                    if (tag.endsWith("request")) {
                        type = 3;
                    }

                    route = this.processPostData(reader);
                }

                if (!reader.ready()) {
                    break;
                }
            }

            //Logc.i("#@##Client ## " + route);
            output = new PrintStream(socket.getOutputStream());
            if (route == null) {
                type = -1;
            } else if (route.startsWith("html")) {
                type = 2;
            } else if (route.startsWith("js")) {
                type = 1;
            } else if (route.startsWith("css")) {
                type = 4;
            } else if (route.startsWith("img")) {
                type = 5;
            }

            byte[] bytes;
            if (type == 1) {
                bytes = NetUtil.loadContent(route, this.mAssets);
            } else if (type == 2) {
                bytes = NetUtil.loadContent(route, this.mAssets);
            } else if (type == 4) {
                bytes = NetUtil.loadContent(route, this.mAssets);
            } else if (type == 5) {
                bytes = NetUtil.loadContent(route, this.mAssets);
            } else if (type == 3) {
                bytes = null;//this.processData(route);
            } else if (type == -1) {
                bytes = null;
            } else {
                bytes = NetUtil.loadContent("index.html", this.mAssets);
            }

            if (null == bytes) {
                this.writeServerError(output);
                return;
            }

            output.println("HTTP/1.0 200 OK");
            output.println("Content-Type: " + NetUtil.detectMimeType(route));
            output.println("Content-Length: " + bytes.length);
            output.println();
            output.write(bytes);
            output.flush();
        } finally {
            try {
                if (null != output) {
                    output.close();
                }

                if (null != reader) {
                    reader.close();
                }
            } catch (Exception var16) {
                var16.printStackTrace();
            }

        }

    }

    /*private byte[] processData(String data) {
        try {
            ApiResult result = GsonUtil.getInstance().parseApiResult(data);
            String json;
            if (result != null) {
                Logc.w("#@#@#@" + result.toString());
                if (result.getType() == 1) {
                    ServerInfo serverInfo = new ServerInfo();
                    serverInfo.setServerIp(Utils.getLocalIP(this.context));
                    serverInfo.setPort(8081);
                    serverInfo.setAppName(this.appName);
                    result.setData(new ConfigBean(GwLog.gLogArgs, serverInfo));
                } else if (result.getType() == 2) {
                    json = result.getData().toString();
                    Type type = (new TypeToken<LoginGwBean>() {
                    }).getType();
                    LoginGwBean loginGwBean = (LoginGwBean)GsonUtil.getInstance().toObject(json, type);
                    if (loginGwBean != null) {
                        loginGwBean.setAppName(this.appName);
                        if (loginGwBean.getUsername().equalsIgnoreCase("admin") && loginGwBean.getPassword().equalsIgnoreCase("admin")) {
                            result.setData("login sucess");
                        } else {
                            result.setType(-1);
                            result.setData("login failed");
                        }
                    }
                }
            }

            try {
                Object dResult = HttpEvent.getInstance().notify(result.getType(), result.getData());
                if (dResult != null) {
                    if (dResult instanceof String) {
                        String string = (String)dResult;
                        if (TextUtils.isEmpty(string)) {
                            result.setData(dResult);
                        }
                    }

                    Logc.w("#@#@#@ dResult " + dResult.toString());
                }
            } catch (Exception var6) {
                Logc.w("#@#@#@ 1 " + var6.getMessage());
            }

            json = GsonUtil.getInstance().toJson(result);
            return json.getBytes();
        } catch (JSONException var7) {
            var7.printStackTrace();
            return null;
        }
    }
*/
    private void writeServerError(PrintStream output) {
        output.println("HTTP/1.0 500 Internal Server Error");
        output.flush();
    }
}
