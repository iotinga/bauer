package it.netgrid.bauer.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.javafaker.Faker;

import it.netgrid.bauer.impl.impl.CBORMqttMessageFactory;
import it.netgrid.bauer.impl.impl.JSONMqttMessageFactory;
import it.netgrid.bauer.impl.impl.MqttConfigImpl;

import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;

public class MqttConfigTest {

    private Faker faker;
    private MqttConfigImpl config;

    @BeforeEach
    public void setUp() {
        this.faker = new Faker();

    }

    @Test
    public void buildConnectionOptionsTest() {
        config = new MqttConfigImpl(faker.lorem().word(), faker.lorem().word(), faker.lorem().word(),
                faker.lorem().word(), faker.lorem().word(), faker.random().nextInt(0, 1000),
                faker.random().nextInt(0, 1000), faker.random().nextBoolean(), faker.random().nextInt(0, 1000),
                faker.random().nextInt(0, 1000));
        MqttConnectionOptions result = config.asConnectionOptions();
        assertEquals(result.isAutomaticReconnect(), true);
        assertEquals(result.getAutomaticReconnectMinDelay(), config.reconnectMinDelay());
        assertEquals(result.getAutomaticReconnectMaxDelay(), config.reconnectMaxDelay());
        assertEquals(result.isCleanStart(), config.isCleanStart());
        assertEquals(result.getConnectionTimeout(), config.connectionTimeout());
        assertEquals(result.getKeepAliveInterval(), config.keepAliveInterval());
        assertEquals(result.getUserName(), config.user());
        assertEquals(new String(result.getPassword()), config.password());
        assertNull(result.getSocketFactory());
    }

    @Test
    public void getMessageFactoryCBORAsDefaultTest() {
        config = new MqttConfigImpl(faker.lorem().word(), null, faker.lorem().word(),
                faker.lorem().word(), faker.lorem().word(), faker.random().nextInt(0, 1000),
                faker.random().nextInt(0, 1000), faker.random().nextBoolean(), faker.random().nextInt(0, 1000),
                faker.random().nextInt(0, 1000));
        MqttMessageFactory result = config.getMessageFactory();
        assertEquals(CBORMqttMessageFactory.class, result.getClass());
    }

    @Test
    public void getMessageFactoryCBOROnUnknownTest() {
        config = new MqttConfigImpl(faker.lorem().word(), faker.lorem().word(), faker.lorem().word(),
                faker.lorem().word(), faker.lorem().word(), faker.random().nextInt(0, 1000),
                faker.random().nextInt(0, 1000), faker.random().nextBoolean(), faker.random().nextInt(0, 1000),
                faker.random().nextInt(0, 1000));
        MqttMessageFactory result = config.getMessageFactory();
        assertEquals(CBORMqttMessageFactory.class, result.getClass());
    }

    @Test
    public void getMessageFactoryJSONAsRequiredTest() {
        config = new MqttConfigImpl(faker.lorem().word(), JSONMqttMessageFactory.MQTT_MESSAGE_CONTENT_TYPE, faker.lorem().word(),
                faker.lorem().word(), faker.lorem().word(), faker.random().nextInt(0, 1000),
                faker.random().nextInt(0, 1000), faker.random().nextBoolean(), faker.random().nextInt(0, 1000),
                faker.random().nextInt(0, 1000));
        
        MqttMessageFactory result = config.getMessageFactory();
        assertEquals(JSONMqttMessageFactory.class, result.getClass());
    }
}
