package it.netgrid.bauer.impl;

import java.util.Properties;

import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.netgrid.bauer.impl.impl.CBORMqttMessageFactory;
import it.netgrid.bauer.impl.impl.JSONMqttMessageFactory;

public class StaticTopicBinderTest {
    private static Properties properties;

    @BeforeEach
    public static void setUp() {
        StaticTopicBinderTest.properties = new Properties();
        StaticTopicBinderTest.properties.setProperty(StaticTopicBinder.MQTT_CLIENT_ID_PROP,
                StaticTopicBinder.MQTT_CLIENT_ID_DEFAULT);
        StaticTopicBinderTest.properties.setProperty(StaticTopicBinder.MQTT_MESSAGE_CONTENT_TYPE_PROP,
                StaticTopicBinder.MQTT_MESSAGE_CONTENT_TYPE_DEFAULT);
        StaticTopicBinderTest.properties.setProperty(StaticTopicBinder.MQTT_URL_PROP,
                StaticTopicBinder.MQTT_URL_DEFAULT);
        StaticTopicBinderTest.properties.setProperty(StaticTopicBinder.MQTT_USER_PROP,
                StaticTopicBinder.MQTT_USER_DEFAULT);
        StaticTopicBinderTest.properties.setProperty(StaticTopicBinder.MQTT_PASS_PROP,
                StaticTopicBinder.MQTT_PASS_DEFAULT);
        StaticTopicBinderTest.properties.setProperty(StaticTopicBinder.MQTT_RECONN_MIN_DELAY_PROP,
                StaticTopicBinder.MQTT_RECONN_MIN_DELAY_DEFAULT);
        StaticTopicBinderTest.properties.setProperty(StaticTopicBinder.MQTT_RECONN_MAX_DELAY_PROP,
                StaticTopicBinder.MQTT_RECONN_MAX_DELAY_DEFAULT);
        StaticTopicBinderTest.properties.setProperty(StaticTopicBinder.MQTT_CLEAN_START_PROP,
                StaticTopicBinder.MQTT_CLEAN_START_DEFAULT);
        StaticTopicBinderTest.properties.setProperty(StaticTopicBinder.MQTT_CONN_TIMEOUT_PROP,
                StaticTopicBinder.MQTT_CONN_TIMEOUT_DEFAULT);
        StaticTopicBinderTest.properties.setProperty(StaticTopicBinder.MQTT_KEEP_ALIVE_INTERVAL_PROP,
                StaticTopicBinder.MQTT_KEEP_ALIVE_INTERVAL_DEFAULT);
    }

    @Test
    public void buildConnectionOptionsTest() {
        MqttConnectionOptions result = StaticTopicBinder.buildConnectionOptions(StaticTopicBinderTest.properties);
        Assertions.assertEquals(result.isAutomaticReconnect(), true);
        Assertions.assertEquals(result.getAutomaticReconnectMinDelay(),
                Integer.parseInt(StaticTopicBinder.MQTT_RECONN_MIN_DELAY_DEFAULT));
        Assertions.assertEquals(result.getAutomaticReconnectMaxDelay(),
                Integer.parseInt(StaticTopicBinder.MQTT_RECONN_MAX_DELAY_DEFAULT));
        Assertions.assertEquals(result.isCleanStart(),
                Boolean.parseBoolean(StaticTopicBinder.MQTT_CLEAN_START_DEFAULT));
        Assertions.assertEquals(result.getConnectionTimeout(),
                Integer.parseInt(StaticTopicBinder.MQTT_CONN_TIMEOUT_DEFAULT));
        Assertions.assertEquals(result.getKeepAliveInterval(),
                Integer.parseInt(StaticTopicBinder.MQTT_KEEP_ALIVE_INTERVAL_DEFAULT));
        Assertions.assertEquals(result.getUserName(), StaticTopicBinder.MQTT_USER_DEFAULT);
        Assertions.assertEquals(new String(result.getPassword()), StaticTopicBinder.MQTT_PASS_DEFAULT);
        Assertions.assertNotNull(result.getSocketFactory());
    }

    @Test
    public void getMessageFactoryCBORAsDefaultTest() {
        StaticTopicBinderTest.properties.remove(StaticTopicBinder.MQTT_MESSAGE_CONTENT_TYPE_PROP);
        MqttMessageFactory result = StaticTopicBinder.getMessageFactory(StaticTopicBinderTest.properties);
        Assertions.assertInstanceOf(CBORMqttMessageFactory.class, result);
    }

    @Test
    public void getMessageFactoryCBOROnUnknownTest() {
        StaticTopicBinderTest.properties.setProperty(StaticTopicBinder.MQTT_MESSAGE_CONTENT_TYPE_PROP, "mq9ten0qem");
        MqttMessageFactory result = StaticTopicBinder.getMessageFactory(StaticTopicBinderTest.properties);
        Assertions.assertInstanceOf(CBORMqttMessageFactory.class, result);
    }

    @Test
    public void getMessageFactoryJSONAsRequiredTest() {
        StaticTopicBinderTest.properties.setProperty(StaticTopicBinder.MQTT_MESSAGE_CONTENT_TYPE_PROP, JSONMqttMessageFactory.MQTT_MESSAGE_CONTENT_TYPE);
        MqttMessageFactory result = StaticTopicBinder.getMessageFactory(StaticTopicBinderTest.properties);
        Assertions.assertInstanceOf(JSONMqttMessageFactory.class, result);
    }
}
