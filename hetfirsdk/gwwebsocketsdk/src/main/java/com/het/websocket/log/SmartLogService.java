//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.het.websocket.log;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Build.VERSION;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import com.het.log.Logc;
import com.het.websocket.http.SimpleHttpUtils;
import com.het.websocket.util.Utils;
import com.het.websocket.util.WSConst.MQTT;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SmartLogService extends Service {
    private static final String TAG = "SmartLogService";
    private static final int MEMORY_LOG_FILE_MAX_SIZE = 524288;
    private static final int MEMORY_LOG_FILE_MONITOR_INTERVAL = 10000;
    private static final int SDCARD_LOG_FILE_SAVE_DAYS = 7;
    private String LOG_PATH_MEMORY_DIR;
    private String LOG_PATH_SDCARD_DIR;
    private String LOG_SERVICE_LOG_PATH;
    private final int SDCARD_TYPE = 0;
    private final int MEMORY_TYPE = 1;
    private int CURR_LOG_TYPE = 0;
    private String CURR_INSTALL_LOG_NAME;
    private String logServiceLogName = "Log.txt";
    private SimpleDateFormat myLogSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private OutputStreamWriter writer;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HHmmsss");
    private Process process;
    private WakeLock wakeLock;
    private SmartLogService.SDStateMonitorReceiver sdStateReceiver;
    private SmartLogService.LogTaskReceiver logTaskReceiver;
    public int count = 0;
    private boolean logSizeMoniting = false;
    private static String MONITOR_LOG_SIZE_ACTION = "MONITOR_LOG_SIZE";
    private static String SWITCH_LOG_FILE_ACTION = "SWITCH_LOG_FILE_ACTION";

    private static final int INTERVAL = 1000 * 60 * 60 * 24;// 24h

    public SmartLogService() {
    }

    public IBinder onBind(Intent intent) {
        return null;
    }

    public void onCreate() {
        super.onCreate();
        this.init();
        this.register();
        this.deploySwitchLogFileTask();
        (new SmartLogService.LogCollectorThread()).start();
        Log.i("SmartLogService", "日志有没有被记录呢");
    }

    private void init() {
        this.LOG_PATH_MEMORY_DIR = this.getFilesDir().getAbsolutePath() + File.separator + "log";
        this.LOG_SERVICE_LOG_PATH = this.LOG_PATH_MEMORY_DIR + File.separator + this.logServiceLogName;
        if (VERSION.SDK_INT >= 8) {
            if (this.getExternalFilesDir("mq/log") != null) {
                this.LOG_PATH_SDCARD_DIR = this.getExternalFilesDir("mq/log").getAbsolutePath() + File.separator;
            } else {
                this.LOG_PATH_SDCARD_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + this.getPackageName() + "/mq/log";
            }
        }

        this.createLogDir();
        @SuppressLint("WrongConstant")
        PowerManager pm = (PowerManager)this.getApplicationContext().getSystemService("power");
        this.wakeLock = pm.newWakeLock(1, "SmartLogService");
        this.CURR_LOG_TYPE = this.getCurrLogType();
        Logc.i("SmartLogService", this.CURR_LOG_TYPE + " ===##mqtt SmartLogService onCreate " + this.LOG_PATH_SDCARD_DIR);
    }

    private void register() {
        IntentFilter sdCarMonitorFilter = new IntentFilter();
        sdCarMonitorFilter.addAction("android.intent.action.MEDIA_MOUNTED");
        sdCarMonitorFilter.addAction("android.intent.action.MEDIA_UNMOUNTED");
        sdCarMonitorFilter.addDataScheme("file");
        this.sdStateReceiver = new SmartLogService.SDStateMonitorReceiver();
        this.registerReceiver(this.sdStateReceiver, sdCarMonitorFilter);
        IntentFilter logTaskFilter = new IntentFilter();
        logTaskFilter.addAction(MONITOR_LOG_SIZE_ACTION);
        logTaskFilter.addAction(SWITCH_LOG_FILE_ACTION);
        this.logTaskReceiver = new SmartLogService.LogTaskReceiver();
        this.registerReceiver(this.logTaskReceiver, logTaskFilter);
    }

    public int getCurrLogType() {
        return !Environment.getExternalStorageState().equals("mounted") ? 1 : 0;
    }

    private void deploySwitchLogFileTask() {
        Intent intent = new Intent(SWITCH_LOG_FILE_ACTION);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        @SuppressLint("WrongConstant")
        AlarmManager am = (AlarmManager)this.getSystemService("alarm");

        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), INTERVAL, sender);
        this.recordLogServiceLog("deployNextTask succ,next task time is:" + this.myLogSdf.format(calendar.getTime()));
    }

    private void clearLogCache() {
        Process proc = null;q
        List<String> commandList = new ArrayList();
        commandList.add("logcat");
        commandList.add("-c");

        try {
            proc = Runtime.getRuntime().exec((String[])commandList.toArray(new String[commandList.size()]));
            SmartLogService.StreamConsumer errorGobbler = new SmartLogService.StreamConsumer(proc.getErrorStream());
            SmartLogService.StreamConsumer outputGobbler = new SmartLogService.StreamConsumer(proc.getInputStream());
            errorGobbler.start();
            outputGobbler.start();
            if (proc.waitFor() != 0) {
                Log.e("SmartLogService", " clearLogCache proc.waitFor() != 0");
                this.recordLogServiceLog("clearLogCache clearLogCache proc.waitFor() != 0");
            }
        } catch (Exception var13) {
            Log.e("SmartLogService", "clearLogCache failed", var13);
            this.recordLogServiceLog("clearLogCache failed");
        } finally {
            try {
                proc.destroy();
            } catch (Exception var12) {
                Log.e("SmartLogService", "clearLogCache failed", var12);
                this.recordLogServiceLog("clearLogCache failed");
            }

        }

    }

    private void killLogcatProc(List<SmartLogService.ProcessInfo> allProcList) {
        if (this.process != null) {
            this.process.destroy();
        }

        String packName = this.getPackageName();
        String myUser = this.getAppUser(packName, allProcList);
        Iterator var4 = allProcList.iterator();

        while(var4.hasNext()) {
            SmartLogService.ProcessInfo processInfo = (SmartLogService.ProcessInfo)var4.next();
            Logc.e("+===**********************killLogcatProc " + processInfo.name + " user:" + processInfo.user + " myUser:" + myUser);
            if (processInfo.name.toLowerCase().equals("logcat") && processInfo.user.equals(myUser)) {
                android.os.Process.killProcess(Integer.parseInt(processInfo.pid));
            }
        }

    }

    private String getAppUser(String packName, List<SmartLogService.ProcessInfo> allProcList) {
        Iterator var3 = allProcList.iterator();

        SmartLogService.ProcessInfo processInfo;
        do {
            if (!var3.hasNext()) {
                return null;
            }

            processInfo = (SmartLogService.ProcessInfo)var3.next();
        } while(!processInfo.name.equals(packName));

        return processInfo.user;
    }

    private List<SmartLogService.ProcessInfo> getProcessInfoList(List<String> orgProcessList) {
        List<SmartLogService.ProcessInfo> procInfoList = new ArrayList();

        for(int i = 1; i < orgProcessList.size(); ++i) {
            String processInfo = (String)orgProcessList.get(i);
            String[] proStr = processInfo.split(" ");
            List<String> orgInfo = new ArrayList();
            String[] var7 = proStr;
            int var8 = proStr.length;

            for(int var9 = 0; var9 < var8; ++var9) {
                String str = var7[var9];
                if (!"".equals(str)) {
                    orgInfo.add(str);
                }
            }

            if (orgInfo.size() == 9) {
                SmartLogService.ProcessInfo pInfo = new SmartLogService.ProcessInfo();
                pInfo.user = (String)orgInfo.get(0);
                pInfo.pid = (String)orgInfo.get(1);
                pInfo.ppid = (String)orgInfo.get(2);
                pInfo.name = (String)orgInfo.get(8);
                procInfoList.add(pInfo);
            }
        }

        return procInfoList;
    }

    private List<String> getAllProcess() {
        List<String> orgProcList = new ArrayList();
        Process proc = null;

        try {
            proc = Runtime.getRuntime().exec("ps");
            SmartLogService.StreamConsumer errorConsumer = new SmartLogService.StreamConsumer(proc.getErrorStream());
            SmartLogService.StreamConsumer outputConsumer = new SmartLogService.StreamConsumer(proc.getInputStream(), orgProcList);
            errorConsumer.start();
            outputConsumer.start();
            if (proc.waitFor() != 0) {
                Log.e("SmartLogService", "getAllProcess proc.waitFor() != 0");
                this.recordLogServiceLog("getAllProcess proc.waitFor() != 0");
            }
        } catch (Exception var13) {
            Log.e("SmartLogService", "getAllProcess failed", var13);
            this.recordLogServiceLog("getAllProcess failed");
        } finally {
            try {
                proc.destroy();
            } catch (Exception var12) {
                Log.e("SmartLogService", "getAllProcess failed", var12);
                this.recordLogServiceLog("getAllProcess failed");
            }

        }

        return orgProcList;
    }

    public void createLogCollector() {
        String logFileName = this.sdf.format(new Date()) + ".txt";
        List<String> commandList = new ArrayList();
        commandList.add("logcat");
        commandList.add("-f");
        commandList.add(this.getLogPath());
        commandList.add("-v");
        commandList.add("time");

        try {
            String[] cmd = (String[])commandList.toArray(new String[commandList.size()]);
            this.process = Runtime.getRuntime().exec(cmd);
            this.recordLogServiceLog("start collecting the log,and log name is:" + logFileName);
        } catch (Exception var4) {
            Log.e("SmartLogService", "CollectorThread == >" + var4.getMessage(), var4);
            this.recordLogServiceLog("CollectorThread == >" + var4.getMessage());
        }

        Logc.e("+===**********************createLogCollector");
    }

    public String getLogPath() {
        this.createLogDir();
        String logFileName = this.sdf.format(new Date()) + ".txt";
        if (this.CURR_LOG_TYPE == 1) {
            this.CURR_INSTALL_LOG_NAME = logFileName;
            Log.d("SmartLogService", "Log stored in memory, the path is:" + this.LOG_PATH_MEMORY_DIR + File.separator + logFileName);
            return this.LOG_PATH_MEMORY_DIR + File.separator + logFileName;
        } else {
            this.CURR_INSTALL_LOG_NAME = logFileName;
            Log.d("SmartLogService", "Log stored in SDcard, the path is:" + this.LOG_PATH_SDCARD_DIR + File.separator + logFileName);
            return this.LOG_PATH_SDCARD_DIR + File.separator + logFileName;
        }
    }

    public void handleLog() {
        Logc.d("====##mqtt handleLog " + this.logSizeMoniting + " CURR_LOG_TYPE:" + this.CURR_LOG_TYPE);
        this.deployLogSizeMonitorTask();
        if (this.CURR_LOG_TYPE == 1) {
            this.deleteMemoryExpiredLog();
        } else {
            this.moveLogfile();
            this.deleteSDcardExpiredLog();
        }

    }

    private void deployLogSizeMonitorTask() {
        Logc.d("====##mqtt 部署日志大小监控任务 " + this.logSizeMoniting);
        if (!this.logSizeMoniting) {
            this.logSizeMoniting = true;
            Intent intent = new Intent(MONITOR_LOG_SIZE_ACTION);
            PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
            @SuppressLint("WrongConstant") AlarmManager am = (AlarmManager)this.getSystemService("alarm");
            am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 10000L, sender);
            Log.d("SmartLogService", "deployLogSizeMonitorTask() succ !");
        }
    }

    private void cancelLogSizeMonitorTask() {
        this.logSizeMoniting = false;
        @SuppressLint("WrongConstant") AlarmManager am = (AlarmManager)this.getSystemService("alarm");
        Intent intent = new Intent(MONITOR_LOG_SIZE_ACTION);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
        am.cancel(sender);
        Logc.d("====##mqtt 取消部署日志大小监控任务 " + this.logSizeMoniting);
    }

    private void checkLogSize() {
        if (this.CURR_INSTALL_LOG_NAME != null && !"".equals(this.CURR_INSTALL_LOG_NAME)) {
            String path = this.LOG_PATH_MEMORY_DIR + File.separator + this.CURR_INSTALL_LOG_NAME;
            if (0 == this.CURR_LOG_TYPE) {
                path = this.LOG_PATH_SDCARD_DIR + File.separator + this.CURR_INSTALL_LOG_NAME;
            }

            File file = new File(path);
            if (!file.exists()) {
                return;
            }

            long fileSize = file.length();
            Logc.d("====##mqtt 检查文件大小 ==> The size of the log is too big?size=" + (float)fileSize / 1024.0F + "kb");
            if (fileSize >= 524288L) {
                Logc.i("====##mqtt The log's size is too big!");
                (new SmartLogService.LogCollectorThread()).start();
                this.uploadFile(file);
            }
        }

    }

    private void createLogDir() {
        File file = new File(this.LOG_PATH_MEMORY_DIR);
        boolean mkOk;
        if (!file.isDirectory()) {
            mkOk = file.mkdirs();
            if (!mkOk) {
                mkOk = file.mkdirs();
            }
        }

        if (Environment.getExternalStorageState().equals("mounted")) {
            file = new File(this.LOG_PATH_SDCARD_DIR);
            if (!file.isDirectory()) {
                mkOk = file.mkdirs();
                if (!mkOk) {
                    this.recordLogServiceLog("move file failed,dir is not created succ");
                    return;
                }
            }
        }

    }

    private void moveLogfile() {
        if (Environment.getExternalStorageState().equals("mounted")) {
            File file = new File(this.LOG_PATH_SDCARD_DIR);
            if (!file.isDirectory()) {
                boolean mkOk = file.mkdirs();
                if (!mkOk) {
                    return;
                }
            }

            file = new File(this.LOG_PATH_MEMORY_DIR);
            if (file.isDirectory()) {
                File[] allFiles = file.listFiles();
                File[] var3 = allFiles;
                int var4 = allFiles.length;

                for(int var5 = 0; var5 < var4; ++var5) {
                    File logFile = var3[var5];
                    String fileName = logFile.getName();
                    if (!this.logServiceLogName.equals(fileName)) {
                        boolean isSucc = this.copy(logFile, new File(this.LOG_PATH_SDCARD_DIR + File.separator + fileName));
                        if (isSucc) {
                            logFile.delete();
                        }
                    }
                }
            }

        }
    }

    private void deleteSDcardExpiredLog() {
        File file = new File(this.LOG_PATH_SDCARD_DIR);
        if (file.isDirectory()) {
            File[] allFiles = file.listFiles();
            File[] var3 = allFiles;
            int var4 = allFiles.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                File logFile = var3[var5];
                String fileName = logFile.getName();
                if (!this.logServiceLogName.equals(fileName)) {
                    String createDateInfo = this.getFileNameWithoutExtension(fileName);
                    if (this.canDeleteSDLog(createDateInfo)) {
                        logFile.delete();
                        Log.d("SmartLogService", "delete expired log success,the log path is:" + logFile.getAbsolutePath());
                    }
                }
            }
        }

    }

    public boolean canDeleteSDLog(String createDateStr) {
        boolean canDel = false;
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -7);
        Date expiredDate = calendar.getTime();

        try {
            Date createDate = this.sdf.parse(createDateStr);
            canDel = createDate.before(expiredDate);
        } catch (ParseException var6) {
            Log.e("SmartLogService", var6.getMessage(), var6);
            canDel = false;
        }

        return canDel;
    }

    private void deleteMemoryExpiredLog() {
        File file = new File(this.LOG_PATH_MEMORY_DIR);
        if (file.isDirectory()) {
            File[] allFiles = file.listFiles();
            Arrays.sort(allFiles, new SmartLogService.FileComparator());

            for(int i = 0; i < allFiles.length - 2; ++i) {
                File _file = allFiles[i];
                if (!this.logServiceLogName.equals(_file.getName()) && !_file.getName().equals(this.CURR_INSTALL_LOG_NAME)) {
                    _file.delete();
                    Log.d("SmartLogService", "delete expired log success,the log path is:" + _file.getAbsolutePath());
                }
            }
        }

    }

    private boolean copy(File source, File target) {
        FileInputStream in = null;
        FileOutputStream out = null;

        try {
            boolean var6;
            try {
                if (!target.exists()) {
                    boolean createSucc = target.createNewFile();
                    if (!createSucc) {
                        var6 = false;
                        return var6;
                    }
                }

                in = new FileInputStream(source);
                out = new FileOutputStream(target);
                byte[] buffer = new byte[8192];

                int count;
                while((count = in.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                }

                boolean var7 = true;
                return var7;
            } catch (Exception var18) {
                var18.printStackTrace();
                Log.e("SmartLogService", var18.getMessage(), var18);
                this.recordLogServiceLog("copy file fail");
                var6 = false;
                return var6;
            }
        } finally {
            try {
                if (in != null) {
                    in.close();
                }

                if (out != null) {
                    out.close();
                }
            } catch (IOException var17) {
                var17.printStackTrace();
                Log.e("SmartLogService", var17.getMessage(), var17);
                this.recordLogServiceLog("copy file fail");
                return false;
            }

        }
    }

    private void recordLogServiceLog(String msg) {
        if (this.writer != null) {
            try {
                Date time = new Date();
                this.writer.write(this.myLogSdf.format(time) + " : " + msg);
                this.writer.write("\n");
                this.writer.flush();
            } catch (IOException var3) {
                var3.printStackTrace();
                Log.e("SmartLogService", var3.getMessage(), var3);
            }
        }

    }

    private String getFileNameWithoutExtension(String fileName) {
        return fileName.substring(0, fileName.indexOf("."));
    }

    public void onDestroy() {
        super.onDestroy();
        this.recordLogServiceLog("SmartLogService onDestroy");
        if (this.writer != null) {
            try {
                this.writer.close();
            } catch (IOException var2) {
                var2.printStackTrace();
            }
        }

        if (this.process != null) {
            this.process.destroy();
        }

        this.unregisterReceiver(this.sdStateReceiver);
        this.unregisterReceiver(this.logTaskReceiver);
        Log.d("SmartLogService", "onDestroy... ");
        (new Thread(new Runnable() {
            public void run() {
                SmartLogService.this.uploadAllLog();
            }
        })).start();
    }

    private void uploadAllLog() {
        List<File> files = Utils.getTxTFileList(this.LOG_PATH_SDCARD_DIR);
        Iterator var2 = files.iterator();

        while(var2.hasNext()) {
            File file = (File)var2.next();
            this.uploadAfterDelFile(file);
        }

    }

    private void uploadAfterDelFile(File file) {
        String host = "http://" + MQTT.LOCALHOST + ":8421/v1/api/file";
        Map<String, String> headers = new HashMap();
        headers.put("mqtt-clientid", Utils.getClientId(this));

        try {
            long start = System.currentTimeMillis();
            String ret = SimpleHttpUtils.uploadFile(host, headers, file, "gwkey");
            if (ret != null) {
                long fileSize = file.length();
                float useTime = (float)(System.currentTimeMillis() - start);
                useTime /= 1000.0F;
                boolean isSuc = file.delete();
                Logc.i("=###mqtt 上传[" + file.getName() + " size:" + (float)fileSize / 1024.0F + "kb]耗时" + useTime + "秒,删除" + (isSuc ? "成功" : "失败") + file.getAbsolutePath());
            }
        } catch (Exception var11) {
            var11.printStackTrace();
        }

    }

    private void uploadFile(final File file) {
        (new Thread(new Runnable() {
            public void run() {
                SmartLogService.this.uploadAfterDelFile(file);
            }
        })).start();
    }

    class FileComparator implements Comparator<File> {
        FileComparator() {
        }

        public int compare(File file1, File file2) {
            if (SmartLogService.this.logServiceLogName.equals(file1.getName())) {
                return -1;
            } else if (SmartLogService.this.logServiceLogName.equals(file2.getName())) {
                return 1;
            } else {
                String createInfo1 = SmartLogService.this.getFileNameWithoutExtension(file1.getName());
                String createInfo2 = SmartLogService.this.getFileNameWithoutExtension(file2.getName());

                try {
                    Date create1 = SmartLogService.this.sdf.parse(createInfo1);
                    Date create2 = SmartLogService.this.sdf.parse(createInfo2);
                    return create1.before(create2) ? -1 : 1;
                } catch (ParseException var7) {
                    return 0;
                }
            }
        }
    }

    class LogTaskReceiver extends BroadcastReceiver {
        LogTaskReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (SmartLogService.SWITCH_LOG_FILE_ACTION.equals(action)) {
                (SmartLogService.this.new LogCollectorThread()).start();
            } else if (SmartLogService.MONITOR_LOG_SIZE_ACTION.equals(action)) {
                Logc.d("====##mqtt 监控任务广播 " + SmartLogService.this.logSizeMoniting);
                SmartLogService.this.checkLogSize();
            }

        }
    }

    class SDStateMonitorReceiver extends BroadcastReceiver {
        SDStateMonitorReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.MEDIA_UNMOUNTED".equals(intent.getAction())) {
                if (SmartLogService.this.CURR_LOG_TYPE == 0) {
                    Log.d("SmartLogService", "SDcar is UNMOUNTED");
                    SmartLogService.this.CURR_LOG_TYPE = 1;
                    (SmartLogService.this.new LogCollectorThread()).start();
                }
            } else if (SmartLogService.this.CURR_LOG_TYPE == 1) {
                Log.d("SmartLogService", "SDcar is MOUNTED");
                SmartLogService.this.CURR_LOG_TYPE = 0;
                (SmartLogService.this.new LogCollectorThread()).start();
            }

        }
    }

    class StreamConsumer extends Thread {
        InputStream is;
        List<String> list;

        StreamConsumer(InputStream is) {
            this.is = is;
        }

        StreamConsumer(InputStream iis, List<String> ll) {
            this.is = iis;
            this.list = ll;
        }

        public void run() {
            try {
                InputStreamReader isr = new InputStreamReader(this.is);
                BufferedReader br = new BufferedReader(isr);
                String line = null;

                while((line = br.readLine()) != null) {
                    if (this.list != null) {
                        this.list.add(line);
                    }
                }
            } catch (IOException var4) {
                var4.printStackTrace();
            }

        }
    }

    class ProcessInfo {
        public String user;
        public String pid;
        public String ppid;
        public String name;

        ProcessInfo() {
        }

        public String toString() {
            String str = "user=" + this.user + " pid=" + this.pid + " ppid=" + this.ppid + " name=" + this.name;
            return str;
        }
    }

    class LogCollectorThread extends Thread {
        public LogCollectorThread() {
            super("LogCollectorThread");
            Log.d("SmartLogService", "LogCollectorThread is create");
        }

        public void run() {
            try {
                SmartLogService.this.wakeLock.acquire();
                SmartLogService.this.clearLogCache();
                List<String> orgProcessList = SmartLogService.this.getAllProcess();
                List<SmartLogService.ProcessInfo> processInfoList = SmartLogService.this.getProcessInfoList(orgProcessList);
                SmartLogService.this.killLogcatProc(processInfoList);
                SmartLogService.this.createLogCollector();
                Thread.sleep(1000L);
                SmartLogService.this.handleLog();
                SmartLogService.this.wakeLock.release();
            } catch (Exception var3) {
                var3.printStackTrace();
                SmartLogService.this.recordLogServiceLog(Log.getStackTraceString(var3));
            }

        }
    }
}
