package it.netgrid.bauer.impl.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.javafaker.Faker;

import it.netgrid.bauer.impl.EventExample;

public class JSONMqttMessageFactoryTest {

    private static JSONMqttMessageFactory testee;
    private static EventExample eventExample;
    private Faker faker;

    @BeforeEach
    public void setUp() {
        testee = new JSONMqttMessageFactory();
        faker = new Faker();
        eventExample = new EventExample();
        eventExample.setField1(faker.lorem().sentence());
        eventExample.setField2(faker.random().nextInt(100));
        eventExample.setField3((float) faker.random().nextInt(0, 100000) / 100);
        int[] field4 = new int[faker.random().nextInt(100)];
        eventExample.setField4(field4);
        eventExample.setField5(new ArrayList<>());
        eventExample.setField6(null);
    }

    @Test
    public void getEventNullOnNullMessageTest() throws IOException {
        Object result = testee.getEvent(null, Object.class);
        assertEquals(result, null);
    }

    @Test
    public void getEventNullOnEmptyMessageTest() throws IOException {
        MqttMessage message = new MqttMessage();
        Object result = testee.getEvent(message, Object.class);
        assertEquals(result, null);
    }

    @Test
    public void getMqttMessageEmptyOnNullEventTest() throws IOException {
        MqttMessage result = testee.getMqttMessage(null, true);
        assertNotNull(result);
        assertEquals(result.getPayload().length, 0);
    }

    @Test
    public void getMqttMessageRetainTest() throws IOException {
        MqttMessage result = testee.getMqttMessage(null, true);
        assertNotNull(result);
        assertEquals(result.isRetained(), true);
    }

    @Test
    public void getMqttMessageNoRetainTest() throws IOException {
        MqttMessage result = testee.getMqttMessage(null, false);
        assertNotNull(result);
        assertEquals(result.isRetained(), false);
    }

    @Test
    public void getMqttMessageNoRetainAsDefaultTest() throws IOException {
        MqttMessage result = testee.getMqttMessage(eventExample);
        assertNotNull(result);
        assertEquals(result.isRetained(), false);
    }

    @Test
    public void getMqttMessagePayloadOnNotNullEventTest() throws IOException {
        MqttMessage result = testee.getMqttMessage(eventExample);
        assertNotNull(result);
        assertTrue(result.getPayload().length > 0);
    }

    @Test
    public void getEventPreserveStringsTest() throws IOException {
        MqttMessage message = testee.getMqttMessage(eventExample);
        EventExample event = testee.getEvent(message, EventExample.class);
        assertEquals(event.getField1(),eventExample.getField1());
    }
}
