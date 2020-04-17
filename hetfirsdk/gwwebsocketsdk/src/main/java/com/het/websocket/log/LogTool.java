//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.het.websocket.log;

import com.het.log.Logc;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class LogTool {
    public static String TAG = "";
    private Thread thread;
    static Thread cacheThread;
    static Thread clsCaThread;
    private LogTool.ILogNotify logNotify;

    public LogTool(LogTool.ILogNotify notify) {
        this.logNotify = notify;
    }

    public void startLiveLogThread() {
        if (this.thread != null) {
            this.thread.interrupt();
        }

        this.thread = new Thread(new Runnable() {
            public void run() {
                BufferedReader bufferedReader = null;
                Process process = null;

                try {
                    String cmd = "logcat -v time";
                    Logc.i("+===*********************startLiveLogThread " + cmd);
                    process = Runtime.getRuntime().exec(cmd);
                    bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    String line;
                    while((line = bufferedReader.readLine()) != null) {
                        if (line != null && line.contains(LogTool.TAG) && LogTool.this.logNotify != null) {
                            LogTool.this.logNotify.notify(line);
                        }
                    }
                } catch (IOException var13) {
                    var13.printStackTrace();
                    Logc.i("+===*********************2" + var13.getMessage());
                } finally {
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException var12) {
                            var12.printStackTrace();
                        }
                    }

                    if (process != null) {
                        process.destroy();
                    }

                    Logc.i("+===*****************finally*****3");
                }

            }
        });
        this.thread.start();
    }

    public void stopLiveLogThread() {
        if (this.thread != null) {
            this.thread.interrupt();
        }

    }

    public static void getBufferLog(final LogTool.ILogNotify notify) {
        if (cacheThread == null) {
            cacheThread = new Thread(new Runnable() {
                public void run() {
                    BufferedReader bufferedReader = null;
                    Process process = null;

                    try {
                        String cmd = "logcat -d time";
                        process = Runtime.getRuntime().exec(cmd);

                        String line;
                        for(bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream())); (line = bufferedReader.readLine()) != null; Thread.sleep(1L)) {
                            if (line != null && line.contains(LogTool.TAG) && notify != null) {
                                notify.notify(line);
                            }
                        }
                    } catch (Exception var13) {
                        var13.printStackTrace();
                    } finally {
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException var12) {
                                var12.printStackTrace();
                            }
                        }

                        if (process != null) {
                            process.destroy();
                        }

                        LogTool.cacheThread = null;
                    }

                }
            });
            cacheThread.start();
        }
    }

    public static void clearBufferLog(final LogTool.ILogNotify notify) {
        if (clsCaThread == null) {
            clsCaThread = new Thread(new Runnable() {
                public void run() {
                    BufferedReader bufferedReader = null;
                    StringBuffer sb = new StringBuffer();
                    Process process = null;

                    try {
                        String cmd = "logcat -c time";
                        process = Runtime.getRuntime().exec(cmd);

                        String line;
                        for(bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream())); (line = bufferedReader.readLine()) != null; Thread.sleep(1L)) {
                            if (line != null && line.contains(LogTool.TAG)) {
                                sb.append(line);
                            }
                        }

                        if (notify != null) {
                            notify.notify("############清空缓冲区==============================\r\n" + sb.toString());
                        }
                    } catch (Exception var14) {
                        var14.printStackTrace();
                    } finally {
                        if (bufferedReader != null) {
                            try {
                                bufferedReader.close();
                            } catch (IOException var13) {
                                var13.printStackTrace();
                            }
                        }

                        if (process != null) {
                            process.destroy();
                        }

                        LogTool.clsCaThread = null;
                    }

                }
            });
            clsCaThread.start();
        }
    }

    public interface ILogNotify {
        void notify(String var1);
    }
}
