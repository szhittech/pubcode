//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.het.websocket.log;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import com.fsix.mqtt.MQ;
import com.fsix.mqtt.bean.MQBean;
import com.fsix.mqtt.bean.MqttConnBean;
import com.fsix.mqtt.observer.EventManager;
import com.fsix.mqtt.observer.INotify;
import com.het.log.Logc;
import com.het.websocket.bean.ApiResult;
import com.het.websocket.bean.MqHostBean;
import com.het.websocket.log.LogTool.ILogNotify;
import com.het.websocket.ob.LogManager;
import com.het.websocket.ob.MQEvent;
import com.het.websocket.util.GsonUtil;
import com.het.websocket.util.SysUtil;
import com.het.websocket.util.Utils;
import okhttp3.WebSocket;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.json.JSONException;
import org.json.JSONObject;

public class MqTigger implements IMqttActionListener, INotify<MQBean>, ILogNotify {
    private String mqttClientId;
    private String modelDetail;
    private MqttConnBean tempConnBean;
    private MQ mq;
    private Context context;
    private boolean logctrl = false;
    private final String TOPIC_CLIENT = "client";
    private final String TOPIC_LOG = "log";
    private final String UPDATE = "update";
    final int CMD_DETAIL = 0;
    final int CMD_REBOOT = 1;
    final int CMD_LOGCTRL = 2;
    final int CMD_SW_HOST = 3;
    final int CMD_WRITE_LOG = 4;
    final int CMD_UPLOAD_LOG = 5;
    private LogTool logTool;
    private WebSocket webSocket;

    public MqTigger(MQ mq, Context context) {
        this.mq = mq;
        this.context = context;
    }

    public void setWebSocket(WebSocket webSocket) {
        this.webSocket = webSocket;
    }

    public boolean isLogctrl() {
        return this.logctrl;
    }

    public void startLog() {
        this.logTool = new LogTool(this);
        this.startLiveLog();
    }

    private void mqttonSucess() {
        if (this.mq != null) {
            this.mqttClientId = Utils.getClientId(this.context);
            this.publishDeviceInfo();
            this.mq.subscribe(this.mqttClientId + "/" + "client", 1);
            this.mq.subscribe("update", 1);
            Logc.i("####mqtt#########subscribe# " + this.mqttClientId + "/" + "client");
            EventManager.getInstance().registerObserver(this);
        }

    }

    private void publishDeviceInfo() {
        if (this.mq != null) {
            this.logctrl = true;
            if (this.modelDetail != null) {
                ApiResult<String> result = new ApiResult();
                result.setType(0);
                result.setData(this.modelDetail);
                String json = GsonUtil.getInstance().toJson(result);
                this.mq.publish(json.getBytes());
            } else {
                (new Thread(new Runnable() {
                    public void run() {
                        MqTigger.this.modelDetail = SysUtil.getPhoneAllInfo(MqTigger.this.context);
                        Logc.i("####mqtt#########publishDeviceInfo# ");
                        ApiResult<String> result = new ApiResult();
                        result.setType(0);
                        result.setData(MqTigger.this.modelDetail);
                        String json = GsonUtil.getInstance().toJson(result);
                        MqTigger.this.mq.publish(json.getBytes());
                    }
                })).start();
            }

        }
    }

    private void processServerMessage(MQBean eventData) {
        String topic = eventData.getTopic();
        String data = eventData.getMessage().toString();
        if (topic != null) {
            if (topic.equals(this.mqttClientId + "/" + "client")) {
                try {
                    ApiResult result = this.parseApiResult(data);
                    if (result != null) {
                        if (result.getType() == 0) {
                            this.publishDeviceInfo();
                        } else if (result.getType() == 1) {
                            Logc.i("####mqtt#########reboot# ");
                            (new Handler(Looper.getMainLooper())).postDelayed(new Runnable() {
                                public void run() {
                                    Utils.reboot(MqTigger.this.context, 2000);
                                }
                            }, 3000L);
                        } else if (result.getType() == 3) {
                            this.processSwMqServer(result.getData());
                        } else if (result.getType() == 2) {
                            String msg = result.getData().toString();
                            if (!TextUtils.isEmpty(msg)) {
                                if (msg.equals("off")) {
                                    this.logctrl = false;
                                } else {
                                    this.logctrl = true;
                                }
                            }
                        } else if (result.getType() == 4) {
                            this.processWriteBufLog();
                        } else if (result.getType() == 5) {
                            this.processUploadBufLog();
                        }
                    }
                } catch (JSONException var6) {
                    var6.printStackTrace();
                }
            } else if (topic.equals("update")) {
                this.update(data);
            }
        }

    }

    private void processSwMqServer(Object msg) {
        if (msg != null) {
            MqHostBean mqHostBean = (MqHostBean)GsonUtil.getInstance().toObject(msg.toString(), MqHostBean.class);
            System.out.println(msg);
            if (mqHostBean != null) {
                this.tempConnBean = new MqttConnBean();
            }
        }
    }

    private void processWriteBufLog() {
        Logc.i("####mqtt######启动SmartLogService# ");
        this.context.startService(new Intent(this.context, SmartLogService.class));
        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                MqTigger.this.startLiveLog();
            }
        }, 1000L);
    }

    private void update(String json) {
        Logc.i("####mqtt######update# ");
        MQEvent.getInstance().notify(0, json);
    }

    private void processUploadBufLog() {
        Logc.i("####mqtt######停止SmartLogService# ");
        this.context.stopService(new Intent(this.context, SmartLogService.class));
    }

    private ApiResult parseApiResult(String json) throws JSONException {
        if (TextUtils.isEmpty(json)) {
            return null;
        } else {
            JSONObject jsonObject = new JSONObject(json);
            ApiResult<Object> apiResult = new ApiResult();
            if (jsonObject.has("type")) {
                apiResult.setType(jsonObject.getInt("type"));
            }

            if (jsonObject.has("data")) {
                apiResult.setData(jsonObject.getString("data"));
            }

            return apiResult;
        }
    }

    private void startLiveLog() {
        if (this.logTool != null) {
            this.logTool.startLiveLogThread();
        }

    }

    public void stopLiveLog() {
        if (this.logTool != null) {
            this.logTool.stopLiveLogThread();
        }

    }

    public void notify(String text) {
        if (this.webSocket != null) {
            ApiResult<String> api = new ApiResult();
            api.setData(text);
            String json = GsonUtil.getInstance().toJson(api);
            this.webSocket.send(json);
        }

        LogManager.getInstance().post(text);
        if (this.mq != null && this.logctrl) {
            this.mq.publish(text.getBytes());
        }

    }

    public void onSuccess(IMqttToken asyncActionToken) {
        Logc.i("####mqtt连接成功 " + asyncActionToken.toString());
        this.mqttonSucess();
    }

    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
        Logc.i("####mqtt连接失败11 " + exception.getMessage());
    }

    public void onNotify(MQBean eventData) {
        Logc.i(this.logctrl + "####mqtt onNotify " + eventData.toString());
        if (eventData != null && eventData.getCode() == 0) {
            this.processServerMessage(eventData);
        }

    }
}
