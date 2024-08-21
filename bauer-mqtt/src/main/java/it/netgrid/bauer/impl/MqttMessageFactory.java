package it.netgrid.bauer.impl;

import java.io.IOException;

import org.eclipse.paho.mqttv5.common.MqttMessage;

public interface MqttMessageFactory {
    public <E> E getEvent(MqttMessage message, Class<E> eventClass) throws IOException;
    public <E> MqttMessage getMqttMessage(E event) throws IOException;
    public <E> MqttMessage getMqttMessage(E event, boolean retain) throws IOException;
}
