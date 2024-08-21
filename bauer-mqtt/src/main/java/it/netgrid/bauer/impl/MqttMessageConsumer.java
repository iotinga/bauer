package it.netgrid.bauer.impl;

import java.io.IOException;

import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.MqttSubscription;

public interface MqttMessageConsumer extends Runnable {
    public boolean consume(String topic, MqttMessage message) throws IOException;
    public MqttSubscription getMqttSubscription();
}
