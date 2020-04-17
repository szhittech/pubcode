//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.het.websocket.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleHttpUtils {
    private static final int TEXT_REQUEST_MAX_LENGTH = 5242880;
    private static final Map<String, String> DEFAULT_REQUEST_HEADERS = new HashMap();
    private static final ReadWriteLock RW_LOCK = new ReentrantReadWriteLock();
    private static final String USER_AGENT_FOR_PC = "Mozilla 0.0 Mozilla/5.0 (Windows NT 10.0; Trident/7.0; rv:11.0) like Gecko";
    private static final String USER_AGENT_FOR_MOBILE = "Chrome Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD92D) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.132 Mobile Safari/537.36";
    private static int CONNECT_TIME_OUT = 15000;
    private static int READ_TIME_OUT = 0;

    public SimpleHttpUtils() {
    }

    public static void setMobileBrowserModel(boolean isMobileBrowser) {
        setDefaultRequestHeader("User-Agent", isMobileBrowser ? "Chrome Mozilla/5.0 (Linux; Android 7.0; Nexus 6 Build/NBD92D) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.132 Mobile Safari/537.36" : "Mozilla 0.0 Mozilla/5.0 (Windows NT 10.0; Trident/7.0; rv:11.0) like Gecko");
    }

    public static void setTimeOut(int connectTimeOut, int readTimeOut) {
        if (connectTimeOut >= 0 && readTimeOut >= 0) {
            RW_LOCK.writeLock().lock();

            try {
                CONNECT_TIME_OUT = connectTimeOut;
                READ_TIME_OUT = readTimeOut;
            } finally {
                RW_LOCK.writeLock().unlock();
            }

        } else {
            throw new IllegalArgumentException("timeout can not be negative");
        }
    }

    public static void setDefaultRequestHeader(String key, String value) {
        RW_LOCK.writeLock().lock();

        try {
            DEFAULT_REQUEST_HEADERS.put(key, value);
        } finally {
            RW_LOCK.writeLock().unlock();
        }

    }

    public static void removeDefaultRequestHeader(String key) {
        RW_LOCK.writeLock().lock();

        try {
            DEFAULT_REQUEST_HEADERS.remove(key);
        } finally {
            RW_LOCK.writeLock().unlock();
        }

    }

    public static String get(String url) throws Exception {
        return get(url, (Map)null, (File)null);
    }

    public static String get(String url, Map<String, String> headers) throws Exception {
        return get(url, headers, (File)null);
    }

    public static String get(String url, File saveToFile) throws Exception {
        return get(url, (Map)null, saveToFile);
    }

    public static String get(String url, Map<String, String> headers, File saveToFile) throws Exception {
        return sendRequest(url, "GET", headers, (InputStream)null, saveToFile);
    }

    public static String post(String url, byte[] body) throws Exception {
        return post(url, (Map)null, (byte[])body);
    }

    public static String post(String url, Map<String, String> headers, byte[] body) throws Exception {
        InputStream in = null;
        if (body != null && body.length > 0) {
            in = new ByteArrayInputStream(body);
        }

        return post(url, headers, (InputStream)in);
    }

    public static String post(String url, File bodyFile) throws Exception {
        return post(url, (Map)null, (File)bodyFile);
    }

    public static String post(String url, Map<String, String> headers, File bodyFile) throws Exception {
        InputStream in = null;
        if (bodyFile != null && bodyFile.exists() && bodyFile.isFile() && bodyFile.length() > 0L) {
            in = new FileInputStream(bodyFile);
        }

        return post(url, headers, (InputStream)in);
    }

    public static String post(String url, InputStream bodyStream) throws Exception {
        return post(url, (Map)null, (InputStream)bodyStream);
    }

    public static String post(String url, Map<String, String> headers, InputStream bodyStream) throws Exception {
        return sendRequest(url, "POST", headers, bodyStream, (File)null);
    }

    private static boolean setContentType(HttpURLConnection conn, Map<String, String> headers) {
        if (headers != null && conn != null) {
            String clientId = (String)headers.get("mqtt-clientid");
            if (clientId != null && !clientId.equals("")) {
                String boundary = UUID.randomUUID().toString();
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private static void setDisposition(String boundary, String key, String fileName) {
        String newLine = "\r\n";
        String boundaryPrefix = "--";
        StringBuilder sb = new StringBuilder();
        sb.append("--");
        sb.append(boundary);
        sb.append("\r\n");
        sb.append("Content-Disposition: form-data;name=\"" + key + "\";filename=\"" + fileName + "\"" + "\r\n");
        sb.append("Content-Type:application/octet-stream");
        sb.append("\r\n");
        sb.append("\r\n");
    }

    public static String sendRequest(String url, String method, Map<String, String> headers, InputStream bodyStream, File saveToFile) throws Exception {
        assertUrlValid(url);
        HttpURLConnection conn = null;

        String var12;
        try {
            URL urlObj = new URL(url);
            conn = (HttpURLConnection)urlObj.openConnection();
            setDefaultProperties(conn);
            if (method != null && method.length() > 0) {
                conn.setRequestMethod(method);
            }

            if (headers != null && headers.size() > 0) {
                Iterator var7 = headers.entrySet().iterator();

                while(var7.hasNext()) {
                    Entry<String, String> entry = (Entry)var7.next();
                    conn.setRequestProperty((String)entry.getKey(), (String)entry.getValue());
                }
            }

            String location;
            if (headers != null) {
                final String username = (String)headers.get("username");
                location = (String)headers.get("password");
                final String tmp = location;
                if (username != null && location != null) {
                    Authenticator.setDefault(new Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, tmp.toCharArray());
                        }
                    });
                }
            }

            if (bodyStream != null) {
                conn.setDoOutput(true);
                OutputStream out = conn.getOutputStream();
                copyStreamAndClose(bodyStream, out);
            }

            int code = conn.getResponseCode();
            if (code == 301 || code == 302) {
                location = conn.getHeaderField("Location");
                if (location != null) {
                    closeStream(bodyStream);
                    String var9 = sendRequest(location, "GET", headers, (InputStream)null, saveToFile);
                    return var9;
                }
            }

            long contentLength = (long)conn.getContentLength();
            String contentType = conn.getContentType();
            InputStream in = conn.getInputStream();
            if (code != 200) {
                throw new IOException("Http Error: " + code + "; Desc: " + handleResponseBodyToString(in, contentType));
            }

            if (saveToFile == null) {
                if (contentLength > 5242880L) {
                    throw new IOException("Response content length too large: " + contentLength);
                }

                var12 = handleResponseBodyToString(in, contentType);
                return var12;
            }

            handleResponseBodyToFile(in, saveToFile);
            var12 = saveToFile.getPath();
        } finally {
            closeConnection(conn);
        }

        return var12;
    }

    private static void assertUrlValid(String url) throws IllegalAccessException {
        boolean isValid = false;
        if (url != null) {
            url = url.toLowerCase();
            if (url.startsWith("http://") || url.startsWith("https://")) {
                isValid = true;
            }
        }

        if (!isValid) {
            throw new IllegalAccessException("Only support http or https url: " + url);
        }
    }

    private static void setDefaultProperties(HttpURLConnection conn) {
        RW_LOCK.readLock().lock();

        try {
            conn.setConnectTimeout(CONNECT_TIME_OUT);
            conn.setReadTimeout(READ_TIME_OUT);
            if (DEFAULT_REQUEST_HEADERS.size() > 0) {
                Iterator var1 = DEFAULT_REQUEST_HEADERS.entrySet().iterator();

                while(var1.hasNext()) {
                    Entry<String, String> entry = (Entry)var1.next();
                    conn.setRequestProperty((String)entry.getKey(), (String)entry.getValue());
                }
            }
        } finally {
            RW_LOCK.readLock().unlock();
        }

    }

    private static void handleResponseBodyToFile(InputStream in, File saveToFile) throws Exception {
        FileOutputStream out = null;

        try {
            out = new FileOutputStream(saveToFile);
            copyStreamAndClose(in, out);
        } finally {
            closeStream(out);
        }

    }

    private static String handleResponseBodyToString(InputStream in, String contentType) throws Exception {
        ByteArrayOutputStream bytesOut = null;

        String var6;
        try {
            bytesOut = new ByteArrayOutputStream();
            copyStreamAndClose(in, bytesOut);
            byte[] contentBytes = bytesOut.toByteArray();
            String charset = parseCharset(contentType);
            if (charset == null) {
                charset = parseCharsetFromHtml(contentBytes);
                if (charset == null) {
                    charset = "utf-8";
                }
            }

            String content = null;

            try {
                content = new String(contentBytes, charset);
            } catch (UnsupportedEncodingException var10) {
                content = new String(contentBytes);
            }

            var6 = content;
        } finally {
            closeStream(bytesOut);
        }

        return var6;
    }

    private static void copyStreamAndClose(InputStream in, OutputStream out) {
        try {
            byte[] buf = new byte[1024];
            boolean var3 = true;

            int len;
            while((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
            }

            out.flush();
        } catch (Exception var7) {
            var7.printStackTrace();
        } finally {
            closeStream(in);
            closeStream(out);
        }

    }

    private static String parseCharsetFromHtml(byte[] htmlBytes) {
        if (htmlBytes != null && htmlBytes.length != 0) {
            String html = null;

            try {
                html = new String(htmlBytes, "ISO-8859-1");
                return parseCharsetFromHtml(html);
            } catch (UnsupportedEncodingException var3) {
                html = new String(htmlBytes);
                return parseCharsetFromHtml(html);
            }
        } else {
            return null;
        }
    }

    private static String parseCharsetFromHtml(String html) {
        if (html != null && html.length() != 0) {
            html = html.toLowerCase();
            Pattern p = Pattern.compile("<meta [^>]+>");
            Matcher m = p.matcher(html);
            String meta = null;
            String charset = null;

            while(m.find()) {
                meta = m.group();
                charset = parseCharset(meta);
                if (charset != null) {
                    break;
                }
            }

            return charset;
        } else {
            return null;
        }
    }

    private static String parseCharset(String content) {
        if (content == null) {
            return null;
        } else {
            content = content.trim().toLowerCase();
            Pattern p = Pattern.compile("(?<=((charset=)|(charset=')|(charset=\")))[^'\"/> ]+(?=($|'|\"|/|>| ))");
            Matcher m = p.matcher(content);
            String charset = null;

            while(m.find()) {
                charset = m.group();
                if (charset != null) {
                    break;
                }
            }

            return charset;
        }
    }

    private static void closeConnection(HttpURLConnection conn) {
        if (conn != null) {
            try {
                conn.disconnect();
            } catch (Exception var2) {
                ;
            }
        }

    }

    private static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception var2) {
                ;
            }
        }

    }

    public static String uploadFile(String host, Map<String, String> headers, File file, String key) throws Exception {
        String newLine = "\r\n";
        String boundaryPrefix = "--";
        String BOUNDARY = UUID.randomUUID().toString();
        String target = host;
        HttpURLConnection urlConn = null;

        String var22;
        try {
            URL url = new URL(target);
            urlConn = (HttpURLConnection)url.openConnection();
            urlConn.setRequestMethod("POST");
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);
            urlConn.setInstanceFollowRedirects(true);
            if (headers != null && headers.size() > 0) {
                Iterator var10 = headers.entrySet().iterator();

                while(var10.hasNext()) {
                    Entry<String, String> entry = (Entry)var10.next();
                    urlConn.setRequestProperty((String)entry.getKey(), (String)entry.getValue());
                }
            }

            urlConn.setRequestProperty("connection", "Keep-Alive");
            urlConn.setRequestProperty("Charset", "UTF-8");
            urlConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            DataOutputStream out = new DataOutputStream(urlConn.getOutputStream());
            StringBuilder sb = new StringBuilder();
            sb.append("--");
            sb.append(BOUNDARY);
            sb.append("\r\n");
            sb.append("Content-Disposition: form-data;name=\"" + key + "\";filename=\"" + file.getName() + "\"" + "\r\n");
            sb.append("Content-Type:application/octet-stream");
            sb.append("\r\n");
            sb.append("\r\n");
            System.out.println(sb.toString());
            out.write(sb.toString().getBytes());
            DataInputStream in = new DataInputStream(new FileInputStream(file));
            byte[] bufferOut = new byte[1024];
            boolean var14 = false;

            int bytes;
            while((bytes = in.read(bufferOut)) != -1) {
                out.write(bufferOut, 0, bytes);
            }

            out.write("\r\n".getBytes());
            in.close();
            byte[] end_data = ("\r\n--" + BOUNDARY + "--" + "\r\n").getBytes();
            out.write(end_data);
            out.flush();
            out.close();
            int code = urlConn.getResponseCode();
            System.out.println(" getResponseCode:" + code);
            if (code != 200) {
                return null;
            }

            long contentLength = (long)urlConn.getContentLength();
            String contentType = urlConn.getContentType();
            InputStream inn = urlConn.getInputStream();
            if (code != 200) {
                throw new IOException("Http Error: " + code + "; Desc: " + handleResponseBodyToString(inn, contentType));
            }

            if (contentLength > 5242880L) {
                throw new IOException("Response content length too large: " + contentLength);
            }

            String content = handleResponseBodyToString(inn, contentType);
            var22 = content;
        } finally {
            closeConnection(urlConn);
        }

        return var22;
    }

    static {
        CookieHandler.setDefault(new CookieManager());
        setMobileBrowserModel(false);
    }
}
