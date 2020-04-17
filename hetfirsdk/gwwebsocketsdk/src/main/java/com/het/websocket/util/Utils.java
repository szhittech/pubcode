package com.het.websocket.util;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;

import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import java.io.File;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

public class Utils {
    public static List<File> getTxTFileList(String strPath) {
        List<File> filelist = new ArrayList();
        File dir = new File(strPath);
        File[] files = dir.listFiles();
        if (files != null) {
            for(int i = 0; i < files.length; ++i) {
                String fileName = files[i].getName();
                if (files[i].isDirectory()) {
                    getTxTFileList(files[i].getAbsolutePath());
                } else if (fileName.endsWith("txt")) {
                    String strFileName = files[i].getAbsolutePath();
                    System.out.println("---" + strFileName);
                    filelist.add(files[i]);
                }
            }
        }

        return filelist;
    }
    public static void reboot(Context context,int time) {
        if (time < 1){
            time = 1000;
        }
        Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(context.getApplicationContext().getPackageName());
        PendingIntent restartIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager mgr = (AlarmManager) context.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + time, restartIntent); // 1秒钟后重启应用
        System.exit(0);
    }
    public static String getClientId(Context context) {
        String model = Build.MODEL;
        model = model.trim();
        model = model.replaceAll(" ", "");
        model = model + "_" + getDeviceMacAddress(context);
        model = model + "_" + context.getPackageName();
        return model.trim();
    }

    public static String getDeviceString(Context context) {
        String string = "";
        string += getDeviceModelName() + " ";
        string += getOSVersion() + " ";
        string += getPackageName(context);
        return string;
    }

    public static String getDeviceModelName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        if (model.startsWith(manufacturer)) {
            return capitalize(model);
        } else {
            return capitalize(manufacturer) + " " + model;
        }
    }

    public static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }

    public static String getOSVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    public static String getPackageName(Context context) {
        String packageName = context.getPackageName();
        return packageName;
    }

    public static String getDeviceMacAddress(Context context) {
        String addr = getMacAddrOld(context);
        if (TextUtils.isEmpty(addr) || addr.equals("02:00:00:00:00:00")) {
            addr = getMacAddr();
            System.out.println(" #### getDeviceMacAddress" + addr);
        }
        if (TextUtils.isEmpty(addr))
            return null;
        return addr.replaceAll(":", "");
    }

    private static String getMacAddr() {
        String eth0 = null;
        String wlan0 = null;
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (nif.getName().equalsIgnoreCase("wlan0")) {
                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes != null) {
                        StringBuilder res1 = new StringBuilder();
                        for (byte b : macBytes) {
                            res1.append(String.format("%02X:", b));
                        }

                        if (res1.length() > 0) {
                            res1.deleteCharAt(res1.length() - 1);
                        }
                        wlan0 = res1.toString().replace(":", "").trim();
                    }
                }

                if (nif.getName().equalsIgnoreCase("eth0")) {
                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes != null) {
                        StringBuilder res1 = new StringBuilder();
                        for (byte b : macBytes) {
                            res1.append(String.format("%02X:", b));
                        }

                        if (res1.length() > 0) {
                            res1.deleteCharAt(res1.length() - 1);
                        }
                        wlan0 = res1.toString().replace(":", "").trim();
                    }
                }
                /*if (!nif.getName().equalsIgnoreCase("wlan0"))//wlan0
                    continue;
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    return "";
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(String.format("%02X:", b));
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString().replace(":", "").trim();*/
            }
        } catch (Exception ex) {
        }
        if (!TextUtils.isEmpty(wlan0)) {
            return wlan0;
        } else {
            return eth0;
        }
    }

    private static String getMacAddrOld(Context context) {
        String macString = "";
        WifiManager wifimsg = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifimsg != null) {
            if (wifimsg.getConnectionInfo() != null) {
                if (wifimsg.getConnectionInfo().getMacAddress() != null) {
                    macString = wifimsg.getConnectionInfo().getMacAddress();
                }
            }
        }
        System.out.println(" #### " + macString);
        return macString;
    }

    /**
     * 获取应用程序名称
     */
    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean isGoodJson(String json) {
        try {
            new JsonParser().parse(json);
            return true;
        } catch (JsonParseException e) {
            System.out.println("bad json: " + json);
            return false;
        }
    }

    public static InetAddress getLocalIpAddress() {
        InetAddress hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;

            while (true) {
                while (nis.hasMoreElements()) {
                    NetworkInterface ni = (NetworkInterface) nis.nextElement();
                    Enumeration ias = ni.getInetAddresses();

                    while (ias.hasMoreElements()) {
                        ia = (InetAddress) ias.nextElement();
                        if (!(ia instanceof Inet6Address)) {
                            String ip = ia.getHostAddress();
                            if (!"127.0.0.1".equals(ip)) {
                                hostIp = ia;
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

    public static String getLocalIP() {
        return getLocalIpAddress().getHostName();
    }

    public static String getLocalIP(Context ctx) {
        @SuppressLint("WrongConstant") WifiManager wm = (WifiManager)ctx.getSystemService("wifi");
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
        if(TextUtils.isEmpty(ipStr) || ipStr.equalsIgnoreCase("0.0.0.0")) {
            String ipStr1 = getLocalIpAddress1();
            if(!TextUtils.isEmpty(ipStr1)) {
                return ipStr1;
            }
        }

        return sb.toString();
    }

    public static String getLocalIpAddress1() {
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
                        if(!(ia instanceof Inet6Address)) {
                            String ip = ia.getHostAddress();
                            if(!"127.0.0.1".equals(ip)) {
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
}
