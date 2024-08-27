package it.netgrid.bauer.impl;

import java.io.IOException;

import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.common.MqttMessage;

public interface MqttClientManager extends MqttCallback {

    public void safeFirstConnection();
    
    public boolean isFirstConnectionCompleted();

    public void publish(String topic, MqttMessage message) throws IOException;

    public void addConsumer(MqttMessageConsumer consumer) throws IOException;

}
