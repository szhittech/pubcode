package com.het.ws.util;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IpUtils {

    public IpUtils() {
    }

    public static String getBroadcastAddress(Context ctx) {
        try {
            String broad = getBroadcast();
            return broad;
        } catch (SocketException var2) {
            var2.printStackTrace();
            return null;
        }
    }

    @TargetApi(9)
    public static String getBroadcast() throws SocketException {
        System.setProperty("java.net.preferIPv4Stack", "true");
        Enumeration niEnum = NetworkInterface.getNetworkInterfaces();

        while(true) {
            NetworkInterface ni;
            do {
                if (!niEnum.hasMoreElements()) {
                    return null;
                }

                ni = (NetworkInterface)niEnum.nextElement();
            } while(ni.isLoopback());

            Iterator var2 = ni.getInterfaceAddresses().iterator();

            while(var2.hasNext()) {
                InterfaceAddress interfaceAddress = (InterfaceAddress)var2.next();
                if (interfaceAddress.getBroadcast() != null && (ni.getName().contains("wlan") || ni.getName().contains("eth1") || ni.getName().contains("eth0"))) {
                    String broad = interfaceAddress.getBroadcast().toString().substring(1);
                    Log.i("MYACTIVITY", ni.getName() + " 广播地址==" + broad);
                    return broad;
                }
            }
        }
    }

    @TargetApi(9)
    public static String getBroadcast1() throws SocketException {
        System.setProperty("java.net.preferIPv4Stack", "true");
        Enumeration niEnum = NetworkInterface.getNetworkInterfaces();

        while(true) {
            NetworkInterface ni;
            do {
                if (!niEnum.hasMoreElements()) {
                    return null;
                }

                ni = (NetworkInterface)niEnum.nextElement();
            } while(ni.isLoopback());

            Iterator var2 = ni.getInterfaceAddresses().iterator();

            while(var2.hasNext()) {
                InterfaceAddress interfaceAddress = (InterfaceAddress)var2.next();
                if (interfaceAddress.getBroadcast() != null) {
                    String broad = interfaceAddress.getBroadcast().toString().substring(1);
                    return broad;
                }
            }
        }
    }

    public static String getBroadcastAddress1(Context ctx) {
        @SuppressLint("WrongConstant") WifiManager cm = (WifiManager)ctx.getSystemService("wifi");
        DhcpInfo myDhcpInfo = cm.getDhcpInfo();
        if (myDhcpInfo == null) {
            return "255.255.255.255";
        } else {
            int broadcast = myDhcpInfo.ipAddress & myDhcpInfo.netmask | ~myDhcpInfo.netmask;
            byte[] quads = new byte[4];

            for(int k = 0; k < 4; ++k) {
                quads[k] = (byte)(broadcast >> k * 8 & 255);
            }

            try {
                return InetAddress.getByAddress(quads).getHostAddress();
            } catch (Exception var6) {
                return "255.255.255.255";
            }
        }
    }

    public static String pieceIP(Context ctx, byte lastBit) {
        @SuppressLint("WrongConstant") WifiManager wm = (WifiManager)ctx.getSystemService("wifi");
        DhcpInfo di = wm.getDhcpInfo();
        StringBuffer sb = new StringBuffer();
        sb.append(String.valueOf(di.ipAddress & 255));
        sb.append('.');
        sb.append(String.valueOf(di.ipAddress >> 8 & 255));
        sb.append('.');
        sb.append(String.valueOf(di.ipAddress >> 16 & 255));
        sb.append('.');
        sb.append(String.valueOf(lastBit & 255));
        String ipStr = sb.toString();
        return ipStr;
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
        if (TextUtils.isEmpty(ipStr) || ipStr.equalsIgnoreCase("0.0.0.0")) {
            String ipStr1 = getLocalIpAddress();
            if (!TextUtils.isEmpty(ipStr1)) {
                return ipStr1;
            }
        }

        return sb.toString();
    }

    public static String getLocalIpAddress() {
        String hostIp = null;

        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia = null;

            while(true) {
                while(nis.hasMoreElements()) {
                    NetworkInterface ni = (NetworkInterface)nis.nextElement();
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

    public static byte getIpLastByte(Context ctx) {
        @SuppressLint("WrongConstant") WifiManager wm = (WifiManager)ctx.getSystemService("wifi");
        DhcpInfo di = wm.getDhcpInfo();
        byte lastIPByte = (byte)(di.ipAddress >> 24 & 255);
        return lastIPByte;
    }

    public static byte getIpLastByte(String ip) {
        byte[] ips = ipv4Address2BinaryArray(ip);
        return ips.length >= 4 ? ips[3] : 0;
    }

    public static boolean isIpv4(String ipAddress) {
        if (ipAddress != null && !ipAddress.equalsIgnoreCase("")) {
            String ip = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\.(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\.(00?\\d|1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
            Pattern pattern = Pattern.compile(ip);
            Matcher matcher = pattern.matcher(ipAddress);
            return matcher.matches();
        } else {
            return false;
        }
    }

    public static String binaryArray2Ipv4Address(byte[] addr) {
        String ip = "";

        for(int i = 0; i < addr.length; ++i) {
            ip = ip + (addr[i] & 255) + ".";
        }

        return ip.substring(0, ip.length() - 1);
    }

    public static byte[] ipv4Address2BinaryArray(String ipAdd) {
        byte[] binIP = new byte[4];
        String[] strs = ipAdd.split("\\.");

        for(int i = 0; i < strs.length; ++i) {
            binIP[i] = (byte)Integer.parseInt(strs[i]);
        }

        return binIP;
    }
}
