package com.het.fir.down;


import android.util.Log;

import java.io.Serializable;

public class DownStateBean implements Serializable {
    private String fileUrl;
    private String fileLocalPath;
    //-1:还未下载；0:成功；1：文件为空下载失败；2：文件有大小下载中途失败;
    private int fileState;
    private long fileCurrentSize;
    private long contentLength;

    public void copy(DownStateBean bean) {
        if (bean == null)
            return;
        fileUrl = bean.getFileUrl();
        fileLocalPath = bean.getFileLocalPath();
        fileState = bean.getFileState();
        fileCurrentSize = bean.getFileCurrentSize();
        contentLength = bean.getContentLength();
    }

    public DownStateBean(long contentLength) {
        this.contentLength = contentLength;
        fileState = -1;
        Log.w(this.getClass().getSimpleName(), contentLength + "---------");
    }

    public DownStateBean() {
        fileState = -1;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileLocalPath() {
        return fileLocalPath;
    }

    public void setFileLocalPath(String fileLocalPath) {
        this.fileLocalPath = fileLocalPath;
    }

    public int getFileState() {
        return fileState;
    }

    public void setFileState(int fileState) {
        this.fileState = fileState;
    }

    public long getFileCurrentSize() {
        return fileCurrentSize;
    }

    public void setFileCurrentSize(long fileCurrentSize) {
        this.fileCurrentSize = fileCurrentSize;
    }

    public long getContentLength() {
        return contentLength;
    }

//    public void setContentLength(long contentLength) {
//        this.contentLength = contentLength;
//        Logc.w(contentLength+"---------");
//    }

    @Override
    public String toString() {
        return "DownStateBean{" +
                "fileUrl='" + fileUrl + '\'' +
                ", fileLocalPath='" + fileLocalPath + '\'' +
                ", fileState=" + fileState +
                ", fileCurrentSize=" + fileCurrentSize +
                ", contentLength=" + contentLength +
                '}';
    }

}
