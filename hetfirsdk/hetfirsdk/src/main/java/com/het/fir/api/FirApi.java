package com.het.fir.api;

import android.util.Log;

import com.google.gson.Gson;
import com.het.fir.FirSDK;
import com.het.fir.bean.FirAppBean;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FirApi {
    private static FirApi api = null;
    private String appid = null;
    private String apiToken = null;
    private String host = "http://api.fir.im";

    public void setHost(String host) {
        this.host = host;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public static FirApi getApi() {
        if (api == null) {
            synchronized (FirApi.class) {
                if (api == null) {
                    api = new FirApi();
                }
            }
        }
        return api;
    }

    public FirAppBean getAppInfo() throws IOException {
        //http://api.fir.im/apps/latest/5ddccaebf945482374c3d5ce?api_token=f3ecab6024a139a17630ec5d85ce8f35
        String url = host + "/apps/latest/";
        url += appid;
        url += "?api_token=";
        url += apiToken;
        Log.e(FirSDK.TAG, url);
        String respose = run(url);
        if (respose != null) {
            Log.i(FirSDK.TAG, respose);
            FirAppBean appBean = new Gson().fromJson(respose, FirAppBean.class);
            return appBean;
        }
        return null;
    }

    OkHttpClient client = new OkHttpClient();

    String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        if (response != null) {
            int code = response.code();
            if (code == 200) {
                return response.body().string();
            }
        }
        return null;

//        try (Response response = client.newCall(request).execute()) {
//            return response.body().string();
//        }
    }


    public static void main(String[] args) throws IOException {
        FirApi example = new FirApi();
        String response = example.run("https://raw.github.com/square/okhttp/master/README.md");
        System.out.println(response);
    }
}
