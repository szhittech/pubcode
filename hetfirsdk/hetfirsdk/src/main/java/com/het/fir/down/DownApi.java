package com.het.fir.down;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.het.fir.util.Base64;
import com.het.fir.util.FileUtils;
import com.het.fir.util.SharePreferencesUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class DownApi {
    public static final String TAG = "down_api";

    private static boolean checkFileValidate(long length, DownStateBean downStateBean) {
        if (downStateBean == null)
            return false;
        if (downStateBean.getFileState() == -1)//还未下载过
            return true;
        if (length == downStateBean.getContentLength())
            return true;
        return false;
    }

    private static File getFile(Context context, String url,String fileName) {
        if (context == null)
            return null;
        //存储在/data/data/<packagename>/cache下面
        //String path = context.getApplicationContext().getCacheDir().toString();
        //存储在/sdcard/Android/data/<packagename>/cache下面
        String path = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
            path = context.getApplicationContext().getExternalCacheDir().getAbsolutePath();
        }
        if (path == null)
            return null;
        String tmpName = FileUtils.getFileName(url);
        if (tmpName == null)
            return null;
        if (fileName!=null&&!fileName.contains(".")){
            fileName = tmpName;
        }
        File file = new File(path, fileName);
        return file;
    }

    private static long getFileStart(File file) {
        if (file == null)
            return -1;
        long len = file.length();
//        return len > 0 ? len - 1 : len;
        return len;
    }


    private static void writeFile(Context context, Response response, File file, final DownStateBean downStateBean, final OnDownListener downListener) {
        long startsPoint = getFileStart(file);
        long length = response.body().contentLength();
        Log.e(TAG,"=====>state:" + downStateBean.getFileState() + ",cache.total:" + downStateBean.getContentLength() + ",startsPoint:" + startsPoint + ",length:" + length);
        if (downStateBean.getFileState() == 0) {
            //表示这个文件以及是下载成功的，这个时候要对比一下新文件和老文件大小是否相等
            if (startsPoint == length) {
                //文件大小相等，不必再下载了
                if (downListener != null) {
                    downListener.onDownSucess(String.valueOf(file.getAbsoluteFile()));
                }
                return;
            } else {
                startsPoint = 0;
                //文件大小不相等，则删除重新下载
                boolean isdel = file.delete();
                Log.d(TAG,">>>>isdel:" + isdel);
            }
        } else {
            downStateBean.setFileCurrentSize(startsPoint);
            if (startsPoint == 0) {
//                downStateBean.setContentLength(length);
            }
        }



        /*if (length == 0 || length == 1) {
            // 说明文件已经下载完，直接跳转安装就好
            if (downListener != null) {
                downListener.onDownSucess(String.valueOf(file.getAbsoluteFile()));
            }
            return;
        }*/
        //downloadListener.start(length+startsPoint);
        // 保存文件到本地
        InputStream is = null;
        RandomAccessFile randomAccessFile = null;
        BufferedInputStream bis = null;

        byte[] buff = new byte[9048];
        int len = 0;
        try {
            is = response.body().byteStream();
            bis = new BufferedInputStream(is);
            // 随机访问文件，可以指定断点续传的起始位置
            randomAccessFile = new RandomAccessFile(file, "rwd");
            randomAccessFile.seek(startsPoint);
            while ((len = bis.read(buff)) != -1) {
                randomAccessFile.write(buff, 0, len);
            }

            String fileLocalPath = String.valueOf(file.getAbsoluteFile());


            if (downListener != null) {
                //表示这个文件以及是下载成功的，这个时候要对比一下新文件和老文件大小是否相等
                if (downStateBean.getContentLength() == file.length()) {
                    //文件大小相等，不必再下载了
                    downListener.onDownSucess(fileLocalPath);
                } else {
                    //文件大小不相等，则删除重新下载
                    boolean isdel = file.delete();
                    Log.e(TAG,"finish and failed>>>>isdel:" + isdel);
                    downListener.onDownFailed(new Exception("size error"));
                    return;
                }

                // 下载完成
                //downListener.onDownSucess(fileLocalPath);
            }

            downStateBean.setFileState(0);
            downStateBean.setFileLocalPath(fileLocalPath);
            Log.e(TAG,"=====================下载完成");
        } catch (Exception e) {
            e.printStackTrace();
            if (downListener != null) {
                downListener.onDownFailed(e);
            }
            downStateBean.setFileState(2);
            Log.e(TAG,"=====================Exception " + e.getMessage());
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (bis != null) {
                    bis.close();
                }
                if (randomAccessFile != null) {
                    randomAccessFile.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            SharePreferencesUtil.putString(context, downStateBean.getFileUrl(), Base64.objBase64Str(downStateBean));
        }
    }


    private static void setSSL(OkHttpClient.Builder builder) {
        try {
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            builder.sslSocketFactory(sslSocketFactory);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public interface OnDownListener {
        void onDownSucess(String localPath);

        void onDownFailed(Throwable throwable);

        void onDownProgress(long read, long contentLength, boolean done);
    }


    /**
     * 验证是否是URL
     *
     * @param url
     * @return
     */
    public static boolean verifyUrl(String url) {
        // URL验证规则
        String regEx = "[a-zA-z]+://[^\\s]*";
        // 编译正则表达式
        Pattern pattern = Pattern.compile(regEx);
        // 忽略大小写的写法
        // Pattern pat = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(url);
        // 字符串是否与正则表达式相匹配
        boolean rs = matcher.matches();
        return rs;

    }


    public static boolean isHalfFile(Context context, String url) {
        if (!verifyUrl(url)) {
            return false;
        }

        Object object = Base64.strBase64Obj(SharePreferencesUtil.getString(context, url));
        if (object != null && object instanceof DownStateBean) {
            DownStateBean tmp = (DownStateBean) object;
            if (tmp != null && tmp.getFileState() == 2) {
                return true;
            }
        }
        return false;
    }


    //获取文件大小
    public static void getFileLength(Callback callback, String url) {
        Request request = new Request.Builder()
                .url(url)
                .method("HEAD", null).build();

        getClient().newCall(request).enqueue(callback);
    }

    //OkHttpClient配置
    private static OkHttpClient getClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS).build();
    }


    public static void down(final Context context, final String url, final OnDownListener downListener) {
        getFileLength(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (downListener != null) {
                    downListener.onDownFailed(e);
                }
            }

            @Override
            public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                    //获取下载文件长度
                    int length = Integer.valueOf(response.header("Content-Length"));
                    String fileName =  HeaderUtil.getHeaderFileName(response);
                    down(context, url, downListener, length,fileName);
                }
            }
        }, url);
    }

    private static void down(final Context context, final String url, final OnDownListener downListener, long length,String fileName) {
        if (context == null) {
            if (downListener != null) {
                downListener.onDownFailed(new Exception("context is null"));
            }
            return;
        }
        if (TextUtils.isEmpty(url)) {
            if (downListener != null) {
                downListener.onDownFailed(new Exception("url is null"));
            }
            return;
        }
        if (!verifyUrl(url)) {
            if (downListener != null) {
                downListener.onDownFailed(new Exception("url is err:" + url));
            }
            return;
        }

        final File file = getFile(context, url,fileName);
        long startsPoint = getFileStart(file);
        String endPoint = "";
        Log.d(TAG,"startsPoint====>" + startsPoint);

        //----------------------------->
        final DownStateBean downStateBean = new DownStateBean(length);
        Object object = Base64.strBase64Obj(SharePreferencesUtil.getString(context, url));
        if (object != null && object instanceof DownStateBean) {
            DownStateBean tmp = (DownStateBean) object;
            downStateBean.copy(tmp);
            if (downStateBean.getFileState() == 0) {
                startsPoint = 0;//如果这个文件已经下载完成，那么就置0，重新请求文件大小
            }
        } else {
            downStateBean.setFileState(-1);
            downStateBean.setFileUrl(url);
        }
        //----------------------------->1080593


        Request request = null;
        int state = downStateBean.getFileState();
        if (state == 0 || state == -1) {
            request = new Request.Builder()
                    .url(url)
                    .build();
        } else {
            if (downStateBean.getContentLength() > startsPoint){
                //endPoint = String.valueOf(downStateBean.getContentLength()-startsPoint);
            }

            request = new Request.Builder()
                    .url(url)
                    .method("GET",null)
                    .header("RANGE", "bytes=" + startsPoint + "-" + endPoint)//断点续传
                    .build();

            Log.i(TAG,"开始断点下载,起点:"+startsPoint+(TextUtils.isEmpty(endPoint)?"":" 剩余大小:"+endPoint));
//            Logc.i("RANGE====bytes=" + startsPoint + "-" + endPoint);
        }


        final long startSize = startsPoint;
        // 重写ResponseBody监听请求
        Interceptor interceptor = new Interceptor() {

            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                long start = startSize;
                if (start >= downStateBean.getContentLength()){
                    start = 0;
                }
                return originalResponse.newBuilder()
                        .body(new DownResponseBody(originalResponse, start, downStateBean.getContentLength(), new DownResponseBody.IDownProgress() {
                            @Override
                            public void onProgress(long read, long contentLength, boolean done) {
                                if (downListener != null) {
                                    downListener.onDownProgress(read, contentLength, done);
                                }
                            }

                        }))
                        .build();
            }
        };

        OkHttpClient.Builder dlOkhttp = new OkHttpClient.Builder()
                .addNetworkInterceptor(interceptor);
        // 绕开证书
        try {
            setSSL(dlOkhttp);
        } catch (Exception e) {
            e.printStackTrace();
        }

        OkHttpClient ret = dlOkhttp.build();
        // 发起请求
        final Call call = ret.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG,"=====================IOException " + e.getMessage());
                if (downListener != null) {
                    downListener.onDownFailed(e);
                }
                final long size = getFileStart(file);
                downStateBean.setFileState(size == 0 ? 2 : 3);
                downStateBean.setFileCurrentSize(size);
            }

            @Override
            public void onResponse(Call call, Response response) {
                //writeFile(context, response, file, downStateBean, downListener);
                long length = Integer.valueOf(response.header("Content-Length"));
                Log.w(TAG,"下载开始，剩余大小："+length);
                if (length >= startSize) {
                    writeFile(context, response, file, downStateBean, downListener);
                } else {
                    SharePreferencesUtil.removeKey(context, url);
                    boolean isdel = file.delete();
                    Log.e(TAG,"断点下载出了点问题，重新下载，删除文件:"+isdel+",错误的长度："+response.body().contentLength());
                    down(context, url, downListener);
                }
            }
        });
    }

    public static void main(String[] args) {
        String host = "http://uuxia.cn:8123/file/libs.zip";
        System.out.println(verifyUrl(host));
    }
}
