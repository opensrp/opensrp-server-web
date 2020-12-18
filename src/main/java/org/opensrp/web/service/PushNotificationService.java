package org.opensrp.web.service;

import org.opensrp.web.pojo.MqttMessagePayload;

public interface PushNotificationService {
    void setUp();

    void publish(MqttMessagePayload publishProperties);

    void disconnect();

    void subscribeByTopicAndQos(String topic, int qos);
}
