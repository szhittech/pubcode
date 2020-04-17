//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.het.websocket;

import android.content.Context;
import com.fsix.mqtt.MQ;
import com.fsix.mqtt.bean.MqttConnBean;
import com.het.log.Logc;
import com.het.websocket.log.MqTigger;
import com.het.websocket.util.Utils;
import com.het.websocket.util.WSConst.MQTT;

public class MqClient {
    private MqttConnBean mqttConnBean;
    private MQ mq;
    private String mqttClientId;
    private final String TOPIC_LOG = "log";
    private final String UPDATE = "update";
    private final String TOPIC_CLIENT = "client";
    private static MqClient instance = null;
    private Context context;
    private MqTigger mqTigger;

    public MqClient() {
    }

    public static MqClient getInstance() {
        if (instance == null) {
            Class var0 = MqClient.class;
            synchronized(MqClient.class) {
                if (instance == null) {
                    instance = new MqClient();
                }
            }
        }

        return instance;
    }

    public MQ getMq() {
        return this.mq;
    }

    public Context getContext() {
        return this.context;
    }

    public void start(Context context) {
        this.context = context;
        if (this.mq == null) {
            this.mq = new MQ();
            String model = Utils.getClientId(context);
            this.mqttConnBean = new MqttConnBean();
            this.mqttConnBean.setBrokerUrl(MQTT.HOST);
            this.mqttConnBean.setClientId(model);
            this.mqttConnBean.setUserName(MQTT.USERNAME);
            this.mqttConnBean.setPassword(MQTT.PASSWORD);
            this.mqttConnBean.setQos(MQTT.QOS);
            this.mqttConnBean.setRetain(MQTT.RETAIN);
            this.mqttClientId = model;
            this.mqttConnBean.setTopic("log/" + this.mqttClientId);
            this.mqTigger = new MqTigger(this.mq, context);
            this.mqTigger.startLog();
        }

        Logc.i("####mqtt mqconnect " + this.mq.toString());
        this.mq.start(context, this.mqttConnBean, this.mqTigger);
    }

    public MqTigger getMqTigger() {
        return this.mqTigger;
    }

    public void stop() {
        if (this.mqTigger != null) {
            this.mqTigger.stopLiveLog();
        }

        if (this.mq != null) {
            this.mq.stop();
        }

    }
}
