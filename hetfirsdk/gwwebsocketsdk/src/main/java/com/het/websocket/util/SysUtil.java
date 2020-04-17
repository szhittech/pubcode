//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.het.websocket.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.Build.VERSION;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SysUtil {
    public SysUtil() {
    }

    public static String getNetType(Context context) {
        @SuppressLint("WrongConstant") ConnectivityManager connectionManager = (ConnectivityManager)context.getSystemService("connectivity");
        NetworkInfo networkInfo = connectionManager.getActiveNetworkInfo();
        return networkInfo == null ? "" : networkInfo.getTypeName();
    }

    public static String getNetIp() {
        URL infoUrl = null;
        InputStream inStream = null;
        String ipLine = "";
        HttpURLConnection httpConnection = null;

        try {
            infoUrl = new URL("http://pv.sohu.com/cityjson?ie=utf-8");
            URLConnection connection = infoUrl.openConnection();
            httpConnection = (HttpURLConnection)connection;
            int responseCode = httpConnection.getResponseCode();
            if (responseCode == 200) {
                inStream = httpConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inStream, "utf-8"));
                StringBuilder strber = new StringBuilder();
                String line = null;

                while((line = reader.readLine()) != null) {
                    strber.append(line + "\n");
                }

                Pattern pattern = Pattern.compile("((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))");
                Matcher matcher = pattern.matcher(strber.toString());
                if (matcher.find()) {
                    ipLine = matcher.group();
                }
            }
        } catch (MalformedURLException var26) {
            var26.printStackTrace();
        } catch (IOException var27) {
            var27.printStackTrace();
        } finally {
            try {
                if (inStream != null) {
                    inStream.close();
                }

                httpConnection.disconnect();
            } catch (IOException var24) {
                var24.printStackTrace();
            } catch (Exception var25) {
                var25.printStackTrace();
            }

        }

        Log.e("getNetIp", ipLine);
        return ipLine;
    }

    public static String getLocalIP(Context ctx) {
        WifiManager wm = (WifiManager)ctx.getSystemService("wifi");
        DhcpInfo di = wm.getDhcpInfo();
        long ip = (long)di.ipAddress;
        StringBuffer sb = new StringBuffer();
        sb.append(String.valueOf((int)(ip & 255L)));
        sb.append('.');
        sb.append(String.valueOf((int)(ip >> 8 & 255L)));
        sb.append('.');
        sb.append(String.valueOf((int)(ip >> 16 & 255L)));
        sb.append('.');
        sb.append(String.valueOf((int)(ip >> 24 & 255L)));
        String ipStr = sb.toString();
        if (TextUtils.isEmpty(ipStr) || ipStr.equalsIgnoreCase("0.0.0.0")) {
            String ipStr1 = getLocalIpAddress();
            if (!TextUtils.isEmpty(ipStr1)) {
                return ipStr1;
            }
        }

        return sb.toString();
    }

    private static String getLocalIpAddress() {
        String hostIp = null;

        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;

            while(true) {
                while(e.hasMoreElements()) {
                    NetworkInterface ni = (NetworkInterface)e.nextElement();
                    Enumeration ias = ni.getInetAddresses();

                    while(ias.hasMoreElements()) {
                        ia = (InetAddress)ias.nextElement();
                        if (!(ia instanceof Inet6Address)) {
                            String ip = ia.getHostAddress();
                            if (!"127.0.0.1".equals(ip)) {
                                hostIp = ia.getHostAddress();
                                break;
                            }
                        }
                    }
                }

                return hostIp;
            }
        } catch (SocketException var6) {
            var6.printStackTrace();
            return hostIp;
        }
    }


    public static String getBuildDateAsString(Context context) {
        String buildDate;
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
            File file = new File(ai.sourceDir);
            long longTime = file.lastModified();
            Date date = new Date(longTime);
            buildDate = dateFormat.format(date);
            System.out.println(buildDate);
        } catch (Exception var8) {
            buildDate = "Unknown";
        }

        return buildDate;
    }

    public static int getAppVersionCode(Context context) {
        int iAppVersionCode = 0;

        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            iAppVersionCode = info.versionCode;
        } catch (NameNotFoundException var4) {
            ;
        }

        return iAppVersionCode;
    }

    public static String getModelName() {
        return Build.MODEL;
    }

    public static String getProductName() {
        return Build.PRODUCT;
    }

    public static String getBrandName() {
        return Build.BRAND;
    }

    public static int getOSVersionCode() {
        return VERSION.SDK_INT;
    }

    public static String getOSVersionName() {
        return VERSION.RELEASE;
    }

    public static String getOSVersionDisplayName() {
        return Build.DISPLAY;
    }

    private static String getVersionName(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (Exception var2) {
            return "Unknown";
        }
    }

    public static String getPhoneModel() {
        return Build.MODEL;
    }

    public static String getPackageName(Context context) {
        return context.getPackageName();
    }

    public static String getSystemVersion() {
        return VERSION.RELEASE;
    }

    private static String getMacAddrOld(Context context) {
        String macString = "";
        @SuppressLint("WrongConstant") WifiManager wifimsg = (WifiManager)context.getSystemService("wifi");
        if (wifimsg != null && wifimsg.getConnectionInfo() != null && wifimsg.getConnectionInfo().getMacAddress() != null) {
            macString = wifimsg.getConnectionInfo().getMacAddress();
        }

        System.out.println(" #### " + macString);
        return macString;
    }

    public static String getDeviceMacAddress(Context context) {
        String addr = getMacAddrOld(context);
        if (TextUtils.isEmpty(addr) || addr.equals("02:00:00:00:00:00")) {
            addr = getMacAddr();
            System.out.println(" #### getDeviceMacAddress" + addr);
        }

        return addr.replaceAll(":", "");
    }

    @TargetApi(9)
    private static String getMacAddr() {
        String eth0 = null;
        String wlan0 = null;

        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            Iterator var3 = all.iterator();

            while(true) {
                byte[] macBytes;
                StringBuilder res1;
                byte[] var7;
                int var8;
                int var9;
                byte b;
                do {
                    NetworkInterface nif;
                    do {
                        if (!var3.hasNext()) {
                            return (String)(!TextUtils.isEmpty(wlan0) ? wlan0 : eth0);
                        }

                        nif = (NetworkInterface)var3.next();
                        if (nif.getName().equalsIgnoreCase("wlan0")) {
                            macBytes = nif.getHardwareAddress();
                            if (macBytes != null) {
                                res1 = new StringBuilder();
                                var7 = macBytes;
                                var8 = macBytes.length;

                                for(var9 = 0; var9 < var8; ++var9) {
                                    b = var7[var9];
                                    res1.append(String.format("%02X:", b));
                                }

                                if (res1.length() > 0) {
                                    res1.deleteCharAt(res1.length() - 1);
                                }

                                wlan0 = res1.toString().replace(":", "").trim();
                            }
                        }
                    } while(!nif.getName().equalsIgnoreCase("eth0"));

                    macBytes = nif.getHardwareAddress();
                } while(macBytes == null);

                res1 = new StringBuilder();
                var7 = macBytes;
                var8 = macBytes.length;

                for(var9 = 0; var9 < var8; ++var9) {
                    b = var7[var9];
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }

                wlan0 = res1.toString().replace(":", "").trim();
            }
        } catch (Exception var11) {
            return (String)(!TextUtils.isEmpty(wlan0) ? wlan0 : eth0);
        }
    }

    @TargetApi(18)
    public static String getSdSpace(Context context) {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSizeLong();
        long totalBlocks = stat.getBlockCountLong();
        long availableBlocks = stat.getAvailableBlocksLong();
        long totalSize = blockSize * totalBlocks;
        long availSize = availableBlocks * blockSize;
        String totalStr = Formatter.formatFileSize(context, totalSize);
        String availStr = Formatter.formatFileSize(context, availSize);
        String txt = "总空间：" + totalStr + "\r\n可用空间：" + availStr;
        return txt;
    }

    public static String getPhoneSize(Context context) {
        new DisplayMetrics();
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        float density = dm.density;
        int densityDPI = dm.densityDpi;
        float xdpi = dm.xdpi;
        float ydpi = dm.ydpi;
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        return screenWidth + "*" + screenHeight;
    }

    private static float getDensity(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.density;
    }

    private static int getDensityDpi(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.densityDpi;
    }

    private static int getWifiState(Context context) {
        @SuppressLint("WrongConstant") WifiManager wifiManager = (WifiManager)context.getSystemService("wifi");
        if (wifiManager != null) {
            int wifiState = wifiManager.getWifiState();
            return wifiState;
        } else {
            return 4;
        }
    }

    public static String getSSid(Context ctx) {
        @SuppressLint("WrongConstant") WifiManager mWifiManager = (WifiManager)ctx.getSystemService("wifi");
        if (mWifiManager == null) {
            return "";
        } else {
            WifiInfo mWifiInfo = mWifiManager.getConnectionInfo();
            if (mWifiInfo == null) {
                return "";
            } else if (mWifiInfo.getSSID() == null) {
                return "";
            } else {
                String ssid = mWifiInfo.getSSID();
                if (VERSION.SDK_INT > 16 && !TextUtils.isEmpty(ssid)) {
                    ssid = ssid.substring(1, ssid.length() - 1);
                }

                return ssid;
            }
        }
    }

    public static boolean existSDCard() {
        return Environment.getExternalStorageState().equals("mounted");
    }

    @SuppressLint("MissingPermission")
    @TargetApi(18)
    public static String getPhoneAllInfo(Context context) {
        StringBuilder sb = new StringBuilder();
        sb.append("App名称：" + getPackageName(context) + "\r\n");
        sb.append("App版本号：" + getAppVersionCode(context) + "\r\n");
        sb.append("App版本名：" + getVersionName(context) + "\r\n");
        sb.append("编译日期：" + getBuildDateAsString(context) + "\r\n");
        sb.append("MAC地址：" + getDeviceMacAddress(context) + "\n");

        try {
            sb.append("当前WiFi：" + getSSid(context) + "\r\n");
        } catch (Exception var22) {
            ;
        }

        sb.append("WiFi IP：" + getLocalIP(context) + "\n");

        try {
            sb.append("外网IP：" + getNetIp() + "\r\n");
        } catch (Exception var21) {
            ;
        }

        if (VERSION.SDK_INT >= 18) {
            try {
                if (existSDCard()) {
                    File path = Environment.getDataDirectory();
                    StatFs stat = new StatFs(path.getPath());
                    long blockSize = stat.getBlockSizeLong();
                    long totalBlocks = stat.getBlockCountLong();
                    long availableBlocks = stat.getAvailableBlocksLong();
                    long totalSize = blockSize * totalBlocks;
                    long availSize = availableBlocks * blockSize;
                    String totalStr = Formatter.formatFileSize(context, totalSize);
                    String availStr = Formatter.formatFileSize(context, availSize);
                    sb.append("总空间：" + totalStr + "\r\n");
                    sb.append("可用空间：" + availStr + "\r\n");
                }
            } catch (Exception var20) {
                ;
            }
        }

        sb.append("分辨率：" + getPhoneSize(context) + "\r\n");
        sb.append("屏幕密度：" + getDensity(context) + "\r\n");
        sb.append("屏幕密度Dpi：" + getDensityDpi(context) + "\r\n");
        @SuppressLint("WrongConstant") TelephonyManager tm = (TelephonyManager)context.getSystemService("phone");

        try {
            sb.append("手机号码：" + tm.getLine1Number() + "\r\n");
        } catch (Exception var19) {
            ;
        }

        try {
            sb.append("运营商名称：" + tm.getNetworkOperatorName() + "\r\n");
        } catch (Exception var18) {
            ;
        }

        try {
            sb.append("数据状态：" + tm.getDataState() + "\r\n");
        } catch (Exception var17) {
            ;
        }

        try {
            sb.append("SIM运营商名称：" + tm.getSimOperatorName() + "\r\n");
        } catch (Exception var16) {
            ;
        }

        sb.append("处理器：" + Build.BOARD + "\r\n");
        sb.append("手机设备版本：" + Build.MODEL + "\r\n");
        sb.append("设备名称：" + Build.PRODUCT + "\r\n");
        sb.append("设备主机地址：" + Build.HOST + "\r\n");
        sb.append("设备唯一码：" + Build.ID + "\r\n");
        sb.append("手机平台版本：" + Build.DISPLAY + "\r\n");
        sb.append("设备序列号：" + Build.SERIAL + "\r\n");
        sb.append("设备品牌：" + Build.BRAND + "\r\n");
        sb.append("cpu版本：" + Build.CPU_ABI + "\r\n");
        sb.append("cpu版本：" + Build.CPU_ABI2 + "\r\n");
        sb.append("系统版本号：" + VERSION.SDK_INT + "\r\n");
        sb.append("系统版本名：" + VERSION.RELEASE + "\r\n");
        sb.append("手机语言：" + Locale.getDefault().getLanguage() + "\r\n");
        return sb.toString();
    }

    public static String getPhoneAllInfo1(Context context) {
        if (context == null) {
            return "";
        } else {
            StringBuffer sb = new StringBuffer();
            sb.append("App名称：");
            sb.append(getPackageName(context));
            sb.append("\r\n");
            sb.append("编译日期：");
            sb.append(getBuildDateAsString(context));
            sb.append("\r\n");
            sb.append("App版本号：");
            sb.append(getAppVersionCode(context));
            sb.append("\r\n");
            sb.append("App版本名：");
            sb.append(getVersionName(context));
            sb.append("\r\n");
            sb.append("手机名称：");
            sb.append(getModelName());
            sb.append("\r\n");
            sb.append("手机品牌：");
            sb.append(getBrandName());
            sb.append("\r\n");
            sb.append("品牌名称：");
            sb.append(getProductName());
            sb.append("\r\n");
            sb.append("系统版本号：");
            sb.append(getOSVersionCode());
            sb.append("\r\n");
            sb.append("系统版本名：");
            sb.append(getOSVersionName());
            sb.append("\r\n");
            sb.append("分辨率：");
            sb.append(getPhoneSize(context));
            sb.append("\r\n");
            sb.append(getSdSpace(context));
            sb.append("\r\n");
            sb.append("内网IP：");
            sb.append(getLocalIP(context));
            sb.append("\r\n");
            sb.append("外网IP：");
            sb.append(getNetIp());
            sb.append("\r\n");
            sb.append("网络类型：");
            sb.append(getNetType(context));
            sb.append("\r\n");
            sb.append("Mac地址：");
            sb.append(getDeviceMacAddress(context));
            sb.append("\r\n");
            return sb.toString();
        }
    }
}
