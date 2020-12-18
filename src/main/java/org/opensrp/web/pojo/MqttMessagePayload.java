package org.opensrp.web.pojo;

import com.hivemq.client.mqtt.datatypes.MqttQos;

import java.io.Serializable;

public class MqttMessagePayload implements Serializable {
    private String topic;
    private String message;
    private MqttQos qos = MqttQos.AT_LEAST_ONCE;

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MqttQos getQos() {
        return qos;
    }

    public void setQos(MqttQos qos) {
        this.qos = qos;
    }
}
