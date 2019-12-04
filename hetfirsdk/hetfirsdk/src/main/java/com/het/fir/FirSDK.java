package com.het.fir;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.het.fir.api.FirApi;
import com.het.fir.bean.FirAppBean;
import com.het.fir.util.SystemInfoUtils;

import java.io.IOException;


public class FirSDK {

    public static final String TAG = "fir";
    static AlertDialog myDialog = null;
    static Activity mActivity;
    //声明进度条对话框

    public static void setAppId(String appId) {
        FirApi.getApi().setAppid(appId);

    }

    public static void setApiToken(String token) {
        FirApi.getApi().setApiToken(token);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static void update(final Activity activity) {
        if (myDialog == null) {
            mActivity = activity;
            myDialog = new AlertDialog.Builder(mActivity).create();
        }
        if (mActivity == activity) {
            System.out.println("============= same Activity");
        } else {
            mActivity = activity;
            System.out.println("============= dif Activity");
            myDialog = new AlertDialog.Builder(mActivity).create();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final FirAppBean bean = FirApi.getApi().getAppInfo();
                    if (bean != null) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showDialog(activity, bean);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }


    private static void showDialog(final Activity activity, final FirAppBean appBean) {
        int currentVersoin = SystemInfoUtils.getAppVersionCode(activity);
        int remoteVersion = 0;
        if (SystemInfoUtils.isNum(appBean.getVersion())) {
            remoteVersion = Integer.valueOf(appBean.getVersion());
        }
        if (remoteVersion <= currentVersoin) {
            Toast.makeText(activity, activity.getString(R.string.current_version) + SystemInfoUtils.getAppVersionName(activity),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(activity.getString(R.string.current_version));
        sb.append(SystemInfoUtils.getAppVersionName(activity));
        sb.append(activity.getString(R.string.last_version));
        sb.append(appBean.getVersionShort());
        sb.append(activity.getString(R.string.update_content));
        sb.append("\r\n");
        sb.append(appBean.getChangelog());
        if (!myDialog.isShowing()) {
            myDialog.show();
            myDialog.getWindow().setContentView(R.layout.updatedlg);
            TextView content = (TextView) myDialog.getWindow().findViewById(R.id.content);
            content.setText(sb.toString());
            myDialog.getWindow().findViewById(R.id.ok)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //调用以下方法，DownloadFileListener 才有效；如果完全使用自己的下载方法，不需要设置DownloadFileListener
                            myDialog.dismiss();
                            Intent intent = new Intent();
                            intent.setData(Uri.parse(appBean.getInstallUrl()));//Url 就是你要打开的网址
                            intent.setAction(Intent.ACTION_VIEW);
                            activity.startActivity(intent); //启动浏览器

                        }
                    });
            myDialog.getWindow().findViewById(R.id.cancel)
                    .setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            myDialog.dismiss();
                        }
                    });
        }
    }
}
