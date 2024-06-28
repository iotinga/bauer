package it.netgrid.bauer.impl.impl;

import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.junit.Before;
import org.junit.Test;

import io.codearte.jfairy.Fairy;

import static org.junit.Assert.*;

import it.netgrid.bauer.impl.EventExample;

public class JSONMqttMessageFactoryTest {

    private static JSONMqttMessageFactory testee;
    private static EventExample eventExample;
    private static Fairy fairy;

    @Before
    public void setUp() {
        fairy = Fairy.create();
        testee = new JSONMqttMessageFactory();
        eventExample = new EventExample();
        eventExample.setField1(fairy.textProducer().latinSentence());
        eventExample.setField2(fairy.baseProducer().randomInt(Integer.MAX_VALUE));
        eventExample.setField3((float) fairy.baseProducer().randomBetween(0.0f, 100000.0f));
        int[] field4 = new int[fairy.baseProducer().randomInt(20)];
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
