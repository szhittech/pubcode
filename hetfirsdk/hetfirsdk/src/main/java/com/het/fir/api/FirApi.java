package com.het.fir.api;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

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

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public static FirApi getApi() {
        if (api==null){
            synchronized (FirApi.class){
                if (api==null){
                    api = new FirApi();
                }
            }
        }
        return api;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public FirAppBean getAppInfo() throws IOException {
        //http://api.fir.im/apps/latest/5ddccaebf945482374c3d5ce?api_token=f3ecab6024a139a17630ec5d85ce8f35
        String host = "http://api.fir.im/apps/latest/";
        host += appid;
        host += "?api_token=";
        host += apiToken;
        Log.e(FirSDK.TAG,host);
        String respose = run(host);
        if (respose != null) {
            Log.i(FirSDK.TAG,respose);
            FirAppBean appBean = new Gson().fromJson(respose, FirAppBean.class);
            return appBean;
        }
        return null;
    }

    OkHttpClient client = new OkHttpClient();
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    String run(String url) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return response.body().string();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void main(String[] args) throws IOException {
        FirApi example = new FirApi();
        String response = example.run("https://raw.github.com/square/okhttp/master/README.md");
        System.out.println(response);
    }
}
