package it.netgrid.bauer.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.javafaker.Faker;
import it.netgrid.bauer.EventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;

public class StreamTopicTest {

    private static final String MQTT_PATTERN_FULL = "a/b/c/d";
    private static final String MQTT_PATTERN_SIMPLE = "a/b/#";
    private static final String MQTT_PATTERN_INNER = "a/+/c/d";
    private static final String MQTT_PATTERN_MULTI = "+/b/+/d";
    private static final String MQTT_PATTERN_MIXED = "+/b/+/#";

    private static final String MQTT_TOPIC_SHORT = "a/b/c/d";
    private static final String MQTT_TOPIC_FULL = "a/b/c/d/e/f";
    private static final String MQTT_TOPIC_SHORT_ALTERNATE = "a/2/c/d";
    private static final String MQTT_TOPIC_ALTERNATE = "1/b/3/d/4/f";

    private EventExample eventExample;
    private StreamManager streamManager;
    private StreamMessageFactory messageFactory;
    private EventHandler<EventExample> handler;
    private Faker faker;
    private String topicName;

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        eventExample = new EventExample();
        eventExample.setField1(faker.lorem().word());
        streamManager = mock(StreamManager.class);
        messageFactory = mock(StreamMessageFactory.class);
        handler = mock(EventHandler.class);
        topicName = faker.lorem().word();
    }

    @Test
    public void testAddHandler() {
        StreamTopic<EventExample> streamTopic = new StreamTopic<>(streamManager, messageFactory, topicName);
        streamTopic.addHandler(handler);
        verify(streamManager, times(1)).addMessageConsumer(streamTopic);
    }

    @Test
    public void testPost() throws IOException {
        StreamTopic<EventExample> streamTopic = new StreamTopic<>(streamManager, messageFactory, topicName);
        JsonNode jsonNode = mock(JsonNode.class);
        when(messageFactory.buildMessage(any())).thenReturn(jsonNode);

        streamTopic.post(eventExample);

        ArgumentCaptor<JsonNode> captor = ArgumentCaptor.forClass(JsonNode.class);
        verify(streamManager, times(1)).postMessage(captor.capture());
        assert captor.getValue() == jsonNode;
    }

    @Test
    public void testConsume() throws Exception {
        StreamTopic<EventExample> streamTopic = new StreamTopic<>(streamManager, messageFactory, topicName);

        JsonNode message = mock(JsonNode.class);
        StreamEvent<JsonNode> streamEvent = new StreamEvent<JsonNode>(streamTopic.getName(), message);
        StreamEvent<EventExample> streamEventExample = new StreamEvent<EventExample>(streamTopic.getName(),
                eventExample);
        when(messageFactory.buildEvent(message)).thenReturn(streamEvent);
        when(messageFactory.buildEvent(message, EventExample.class)).thenReturn(streamEventExample);

        when(handler.getEventClass()).thenReturn(EventExample.class);
        streamTopic.addHandler(handler);

        streamTopic.consume(message);

        verify(handler, times(1)).handle(streamTopic.getName(), eventExample);
    }

    @Test
    public void mqttPatternFullMatchTest() throws IOException {
        StreamTopic<EventExample> streamTopic = new StreamTopic<>(streamManager, messageFactory, MQTT_PATTERN_FULL);
        boolean result = streamTopic.match(MQTT_TOPIC_SHORT);
        assertTrue(result);
    }

    @Test
    public void mqttPatternFullNoMatchTest() throws IOException {
        StreamTopic<EventExample> streamTopic = new StreamTopic<>(streamManager, messageFactory, MQTT_PATTERN_FULL);
        boolean result = streamTopic.match(MQTT_TOPIC_ALTERNATE);
        assertFalse(result);
    }

    @Test
    public void mqttPatternSimpleMatchTest() throws IOException {
        StreamTopic<EventExample> streamTopic = new StreamTopic<>(streamManager, messageFactory, MQTT_PATTERN_SIMPLE);
        boolean result = streamTopic.match(MQTT_TOPIC_FULL);
        assertTrue(result);
    }

    @Test
    public void mqttPatternSimpleNoMatchTest() throws IOException {
        StreamTopic<EventExample> streamTopic = new StreamTopic<>(streamManager, messageFactory, MQTT_PATTERN_SIMPLE);
        boolean result = streamTopic.match(MQTT_TOPIC_ALTERNATE);
        assertFalse(result);
    }

    @Test
    public void mqttPatternInnerMatchTest() throws IOException {
        StreamTopic<EventExample> streamTopic = new StreamTopic<>(streamManager, messageFactory, MQTT_PATTERN_INNER);
        boolean result = streamTopic.match(MQTT_TOPIC_SHORT_ALTERNATE);
        assertTrue(result);
    }

    @Test
    public void mqttPatternInnerNoMatchTest() throws IOException {
        StreamTopic<EventExample> streamTopic = new StreamTopic<>(streamManager, messageFactory, MQTT_PATTERN_INNER);
        boolean result = streamTopic.match(MQTT_TOPIC_ALTERNATE);
        assertFalse(result);
    }

    @Test
    public void mqttPatternMultiMatchTest() throws IOException {
        StreamTopic<EventExample> streamTopic = new StreamTopic<>(streamManager, messageFactory, MQTT_PATTERN_MULTI);
        boolean result = streamTopic.match(MQTT_TOPIC_SHORT);
        assertTrue(result);
    }

    @Test
    public void mqttPatternMultiNoMatchTest() throws IOException {
        StreamTopic<EventExample> streamTopic = new StreamTopic<>(streamManager, messageFactory, MQTT_PATTERN_MULTI);
        boolean result = streamTopic.match(MQTT_TOPIC_FULL);
        assertFalse(result);
    }

    @Test
    public void mqttPatternMixedMatchTest() throws IOException {
        StreamTopic<EventExample> streamTopic = new StreamTopic<>(streamManager, messageFactory, MQTT_PATTERN_MIXED);
        boolean result = streamTopic.match(MQTT_TOPIC_ALTERNATE);
        assertTrue(result);
    }

    @Test
    public void mqttPatternMixedNoMatchTest() throws IOException {
        StreamTopic<EventExample> streamTopic = new StreamTopic<>(streamManager, messageFactory, MQTT_PATTERN_MIXED);
        boolean result = streamTopic.match(MQTT_TOPIC_SHORT_ALTERNATE);
        assertFalse(result);
    }
}