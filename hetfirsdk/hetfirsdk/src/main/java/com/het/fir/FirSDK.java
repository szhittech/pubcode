package com.het.fir;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


import com.het.fir.api.FirApi;
import com.het.fir.bean.FirAppBean;

import java.io.IOException;
import java.util.regex.Pattern;


public class FirSDK {
    public static final String TAG = "fir";
    static AlertDialog myDialog = null;
    static Activity mActivity;
    public static void setAppId(String appId) {
        FirApi.getApi().setAppid(appId);

    }

    public static void setHost(String host) {
        FirApi.getApi().setHost(host);
    }

    public static void setApiToken(String token) {
        FirApi.getApi().setApiToken(token);
    }

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
    private static int getAppVersionCode(final Context context) {
        int iAppVersionCode = 0;
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(),
                    0);
            iAppVersionCode = info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return iAppVersionCode;
    }

    private static String getAppVersionName(final Context context) {
        String strAppVersionName = "0.0.1";
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(),
                    0);
            strAppVersionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return strAppVersionName;
    }

    private static boolean isNum(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();

    }

    private static void showDialog(final Activity activity, final FirAppBean appBean) {
        int currentVersoin = getAppVersionCode(activity);
        int remoteVersion = 0;
        if (isNum(appBean.getVersion())) {
            remoteVersion = Integer.valueOf(appBean.getVersion());
        }
        if (remoteVersion <= currentVersoin) {
            Toast.makeText(activity, activity.getString(R.string.current_version) + getAppVersionName(activity),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(activity.getString(R.string.current_version));
        sb.append(getAppVersionName(activity));
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
                            myDialog.dismiss();
                            Intent intent = new Intent();
                            intent.setData(Uri.parse(appBean.getInstallUrl()));
                            intent.setAction(Intent.ACTION_VIEW);
                            activity.startActivity(intent);

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
