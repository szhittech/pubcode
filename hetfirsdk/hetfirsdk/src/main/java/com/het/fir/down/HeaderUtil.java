package com.het.fir.down;

import android.text.TextUtils;

import okhttp3.Response;

public class HeaderUtil {
    /**
     * 解析文件头
     * Content-Disposition:attachment;filename=FileName.txt
     * attachment; filename* = UTF-8''clifeapp_v_v1.0.1.2_2019.12.03.17.15.55_release.apk
     * Content-Disposition: attachment; filename*="UTF-8''%E6%9B%BF%E6%8D%A2%E5%AE%9E%E9%AA%8C%E6%8A%A5%E5%91%8A.pdf"
     */
    public static String getHeaderFileName(Response response) {
        String dispositionHeader = response.header("Content-Disposition");
        if (!TextUtils.isEmpty(dispositionHeader)) {
            dispositionHeader = dispositionHeader.trim();
            dispositionHeader = dispositionHeader.replace(" ", "");
            dispositionHeader = dispositionHeader.replace("attachment;filename=", "");
            dispositionHeader = dispositionHeader.replace("filename*=utf-8", "");
            dispositionHeader = dispositionHeader.replace("attachment;", "");
            dispositionHeader = dispositionHeader.replace("filename*=UTF-8", "");
            dispositionHeader = dispositionHeader.replace("''", "");
            dispositionHeader = dispositionHeader.replace(" ", "");
            return dispositionHeader;
        }
        return null;
    }

    public static void main(String[] args) {
        String dispositionHeader = "attachment; filename* = UTF-8''clifeapp_v_v1.0.1.2_2019.12.03.17.15.55_release.apk";
        dispositionHeader = dispositionHeader.trim();
        dispositionHeader = dispositionHeader.replace(" ", "");
        dispositionHeader = dispositionHeader.replace("attachment;filename=", "");
        dispositionHeader = dispositionHeader.replace("filename*=utf-8", "");
        dispositionHeader = dispositionHeader.replace("attachment;", "");
        dispositionHeader = dispositionHeader.replace("filename*=UTF-8", "");
        dispositionHeader = dispositionHeader.replace("''", "");
        dispositionHeader = dispositionHeader.replace(" ", "");
        String[] strings = dispositionHeader.split("; ");
        if (strings.length > 1) {
            dispositionHeader = strings[1].replace("filename=", "");
            dispositionHeader = dispositionHeader.replace("\"", "");
        }
    }
}
