package com.het.fir.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.IOException;

public class ApkInstall {
    /**
     * 创建apk文件
     *
     * @return
     */
    public static File createApkFile() {
        File file = new File(Environment.getExternalStorageDirectory() + "/cbeauty/update");
        if (!file.exists()) {
            file.mkdirs();
        }

        File apkFile = new File(file, "cbeauty.apk");
        if (!apkFile.exists()) {
            try {
                apkFile.createNewFile();
            } catch (IOException var3) {
                var3.printStackTrace();
            }
        }

        return apkFile;
    }

    /**
     * 获取安装intent
     *
     * @param uri
     * @return
     */
    public static Intent getInstallIntent(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri,
                "application/vnd.android.package-archive");
        return intent;
    }

//    /**
//     * 开始安装
//     *
//     * @param mContext
//     * @param uri
//     */
//    public static void startInstall(Context mContext, Uri uri) {
//        if (uri != null) {
//            Intent i = new Intent(Intent.ACTION_VIEW);
//            i.setDataAndType(uri,
//                    "application/vnd.android.package-archive");
//            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            mContext.startActivity(i);
//        }
//
//
//    }

    /**
     * 开始安装
     *
     * @param mContext
     * @param path
     */
    public static void startInstall(Context mContext, String path) {
        String packageName = SystemInfoUtils.getPackageName(mContext);
        startInstall(mContext, path, packageName);
    }

    /**
     * 检测该包名所对应的应用是否存在
     *
     * @param packageName
     * @return
     */
    public static boolean checkPackage(Context mContext, String packageName) {
        if (packageName == null || "".equals(packageName)) {
            return false;
        }
        try {
            mContext.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void startInstall(Context mContext, String path, String applicationId) {
        if (!TextUtils.isEmpty(path)) {
            Uri uri;
            Intent i = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= 24) {

                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                try {
                    String packageName = null;
                    if (TextUtils.isEmpty(applicationId)) {
                        packageName = SystemInfoUtils.getPackageName(mContext);
                    } else {
                        packageName = applicationId;
                    }
                    //uri = FileProvider.getUriForFile(mContext, packageName , new File(path));
                    uri = FileProvider.getUriForFile(mContext, packageName + ".provider", new File(path));//通过FileProvider创建一个content类型的Uri
                } catch (Exception e) {
                    uri = Uri.parse("file:///" + path);
                }
            } else {
                uri = Uri.parse("file:///" + path);
            }


            i.setDataAndType(uri, "application/vnd.android.package-archive");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(i);

        }
    }
}
