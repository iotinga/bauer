package it.netgrid.bauer.impl.impl;

import java.io.IOException;

import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import it.netgrid.bauer.impl.MqttMessageFactory;

public class JSONMqttMessageFactory implements MqttMessageFactory {
    public static final byte[] EMPTY_MQTT_PAYLOAD = new byte[0];
    public static final int DEFAULT_MQTT_QOS = 1;
    public static final String MQTT_MESSAGE_CONTENT_TYPE = "application/json";

    private static final Logger log = LoggerFactory.getLogger(JSONMqttMessageFactory.class);

    private final JsonMapper jm;
    private final ObjectMapper om;
    private final MqttProperties mqttProperties;

    public JSONMqttMessageFactory() {
        this.jm = new JsonMapper();
        this.om = new ObjectMapper(new JsonFactory()).findAndRegisterModules();
        this.mqttProperties = new MqttProperties();
        this.mqttProperties.setContentType(MQTT_MESSAGE_CONTENT_TYPE);
        this.mqttProperties.setPayloadFormat(true);
    }

    @Override
    public <E> MqttMessage getMqttMessage(E event) throws IOException {
        return this.getMqttMessage(event, false);
    }

    @Override
    public <E> MqttMessage getMqttMessage(E event, boolean retain) throws IOException {
        try {
            byte[] jsonData = event == null ? JSONMqttMessageFactory.EMPTY_MQTT_PAYLOAD : this.om.writeValueAsBytes(event);
            MqttMessage message = new MqttMessage(jsonData, DEFAULT_MQTT_QOS, retain, mqttProperties);
            return message;
        } catch (JsonProcessingException e) {
            log.error(String.format("%s: can not encode event", event));
            throw new IOException(e);
        }
    }

    @Override
    public <E> E getEvent(MqttMessage message, Class<E> eventClass) throws IOException {
        if (message == null)
            return null;
        if (message.getPayload().length < 1)
            return null;
        try {
            return this.jm.readValue(message.getPayload(), eventClass);
        } catch (Exception e) {
            log.error(String.format("%s: can not parse event", message));
            throw new IOException(e);
        }
    }
}
