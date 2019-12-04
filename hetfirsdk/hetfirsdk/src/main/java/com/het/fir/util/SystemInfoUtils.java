package com.het.fir.util;
/**
 * <b>类名称：</b> SystemInfoUtils<br/>
 * <b>类描述：</b>系统信息工具类<br/>
 * <b>创建人：</b> 林肯 <br/>
 * <b>修改人：</b> 编辑人 <br/>
 * <b>修改时间：</b> 2015年07月29日 下午2:18 <br/>
 * <b>修改备注：</b> <br/>
 *
 * @version 1.0.0 <br/>
 */

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;


import java.util.Locale;
import java.util.UUID;

public final class SystemInfoUtils {

    /**
     * 私有的构造方法.
     */
    private SystemInfoUtils() {

    }

    /**
     * 获取userAgent
     * <p>
     * <p>
     * 新格式：
     * <p>
     * APPID;应用版本;平台;OS版本;OS版本名称;厂商;机型;设备唯一标识;渠道标识;网络;包名;国际化
     * 新示例：
     * <p>
     * 10001;2.2.0;Android;4.2.2;N7100XXUEMI6BYTuifei;samsung;GT-I9300;xxxxxxxxxxxxxx;D0001;WIFI;cn.clife.www
     *
     * @param context 上下文信息
     * @return 系统相关的信息
     */


    public static String getSysInternationalization(Context context) {
        Resources resources = context.getResources();// 获得res资源对象
        Configuration config = resources.getConfiguration();// 获得设置对象
        return config.locale.toString();
    }

    /**
     * 获取渠道，用于打包，by weatherfish
     *
     * @param context
     * @param metaName
     * @return
     */
    public static String getAppSource(Context context, String metaName) {
        String result = null;
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo.metaData == null)
                return "Android";
            result = appInfo.metaData.getString(metaName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }

    @SuppressLint("MissingPermission")
    public static String getImei(final Context context) {
        String szImei = "";
        try {
            TelephonyManager TelephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            szImei = TelephonyMgr.getDeviceId();
            return szImei;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return szImei;
    }

    public static boolean isNum(String strNum) {
        return strNum.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
    }
    /**
     * 获取网络类型
     *
     * @param context
     * @return
     */


    /**
     * 获取包名 by weatherfish
     *
     * @param context
     * @return
     */
    public static String getPackageName(Context context) {
        return context.getPackageName();
    }

    /**
     * 获取App版本号.
     *
     * @param context 上下文信息
     * @return App版本号
     */
    public static int getAppVersionCode(final Context context) {
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

    /**
     * 获取App版本名.
     *
     * @param context 上下文信息
     * @return App版本名
     */
    public static String getAppVersionName(final Context context) {
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


    /**
     * 获取屏幕分辨率
     *
     * @param context
     * @return
     */
    public static String getPhoneSize(final Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        dm = context.getResources().getDisplayMetrics();

        float density = dm.density; // 屏幕密度（像素比例：0.75/1.0/1.5/2.0）
        int densityDPI = dm.densityDpi; // 屏幕密度（每寸像素：120/160/240/320）
        float xdpi = dm.xdpi;
        float ydpi = dm.ydpi;
        int screenWidth = dm.widthPixels; // 屏幕宽（像素，如：480px）
        int screenHeight = dm.heightPixels; // 屏幕高（像素，如：800px）

        return screenWidth + "*" + screenHeight;
    }

    /**
     * 唯一识别码ID
     * @param context
     * @return
     */

    /**
     * 获取设备制造商名称.
     *
     * @return 设备制造商名称
     */
    public static String getManufacturerName() {
        return Build.MANUFACTURER;
    }

    /**
     * 获取设备名称.
     *
     * @return 设备名称
     */
    public static String getModelName() {
        return Build.MODEL;
    }

    /**
     * 获取产品名称.
     *
     * @return 产品名称
     */
    public static String getProductName() {
        return Build.PRODUCT;
    }

    /**
     * 获取品牌名称.
     *
     * @return 品牌名称
     */
    public static String getBrandName() {
        return Build.BRAND;
    }

    /**
     * 获取操作系统版本号.
     *
     * @return 操作系统版本号
     */
    public static int getOSVersionCode() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * 获取操作系统版本名.
     *
     * @return 操作系统版本名
     */
    public static String getOSVersionName() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取操作系统版本显示名.
     *
     * @return 操作系统版本显示名
     */
    public static String getOSVersionDisplayName() {
        return Build.DISPLAY;
    }







    /**
     * 获取主机地址.
     *
     * @return 主机地址
     */
    public static String getHost() {
        return Build.HOST;
    }


    public final class CommonConsts {

        /**
         * 设备分类
         */
        public static final String SourceType = "Android";

        /**
         * 空格字符�?
         */
        public static final String SPACE = " ";

        /**
         * 逗号.
         */
        public static final String COMMA = ",";

        /**
         * 句点.
         */
        public static final String PERIOD = ".";

        /**
         * 左引�?
         */
        public static final String LEFT_QUOTES = "'";

        /**
         * 右引�?
         */
        public static final String RIGHT_QUOTES = "'";

        /**
         * 左圆括号.
         */
        public static final String LEFT_PARENTHESIS = "(";

        /**
         * 右圆括号.
         */
        public static final String RIGHT_PARENTHESIS = ")";

        /**
         * 左方括号.
         */
        public static final String LEFT_SQUARE_BRACKET = "[";

        /**
         * 右方括号.
         */
        public static final String RIGHT_SQUARE_BRACKET = "]";

        /**
         * 换行�?
         */
        public static final String LINE_BREAK = "\r\n";

        /**
         * 换行�?
         */
        public static final String LINE_BREAK_SHORT = "\n";


        /**
         * 问号.
         */
        public static final String QUESTION_MARK = "?";

        /**
         * 符号&.
         */
        public static final String AMPERSAND = "&";

        /**
         * 等于�?
         */
        public static final String EQUAL = "=";

        /**
         * 分号.
         */
        public static final String SEMICOLON = ";";
        /**
         * App渠道对应的meta名字
         */
        public static final String APP_SOURCE = "AppSource";

        /**
         * 私有的构造方�?
         */
        private CommonConsts() {
        }
    }

    /**
     * 判断系统语言是否为英文
     * @return
     */
    public static boolean isZh(){
        Locale locale = Locale.getDefault();
//Locale.getDefault() 和 LocaleList.getAdjustedDefault().GET(0) 同等效果，还不需要考虑版本问题，推荐直接使用
        String language = locale.getLanguage() + "-" + locale.getCountry();
        if (language.contains("zh")){
            return  true;
        }else {
            return false;
        }
    }

}
