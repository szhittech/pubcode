package com.het.ws.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;

import com.het.ws.bean.ApiResult;
import com.het.ws.log.LogTool;
import com.het.ws.server.WebSocketClients;
import com.het.ws.util.GsonUtil;
import com.het.ws.util.Logc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SysLogService extends IntentService {

    public static void start(Context context) {
        Intent intent = new Intent(context, SysLogService.class);
        context.startService(intent);
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, SysLogService.class));
    }

    public SysLogService() {
        this("SysLogService");
    }

    public SysLogService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Logc.e("onHandleIntent");
        listenSystemLog();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logc.e("onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logc.e("onDestroy");
    }

    private void onSystemLog(String syslog) {
        if (WebSocketClients.getInstance().hasClient() && syslog != null) {
            ApiResult<String> api = new ApiResult();
            api.setData(syslog);
            String json = GsonUtil.getInstance().toJson(api);
            WebSocketClients.getInstance().post(json);
        }
    }

    private void listenSystemLog() {
        BufferedReader bufferedReader = null;
        Process process = null;

        try {
            String cmd = "logcat -v time";
            Logc.i("+===*********************startLiveLogThread " + cmd);
            process = Runtime.getRuntime().exec(cmd);
            bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line != null && line.contains(LogTool.TAG)) {
                    onSystemLog(line);
                }
            }
        } catch (IOException var13) {
            var13.printStackTrace();
            Logc.i("+===*********************2" + var13.getMessage());
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException var12) {
                    var12.printStackTrace();
                }
            }

            if (process != null) {
                process.destroy();
            }

            Logc.i("+===*****************finally*****3");
        }

    }
}
