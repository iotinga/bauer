package it.netgrid.bauer.impl.impl;

import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.MqttSubscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.github.javafaker.Faker;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

import java.io.IOException;

import it.netgrid.bauer.EventHandler;
import it.netgrid.bauer.impl.EventExample;
import it.netgrid.bauer.impl.MqttMessageFactory;
import it.netgrid.bauer.impl.MqttTopic;
 
public class SimpleMqttMessageConsumerTest {

    private static final String MQTT_PATTERN_FULL = "a/b/c/d";
    private static final String MQTT_PATTERN_SIMPLE = "a/b/#";
    private static final String MQTT_PATTERN_INNER = "a/+/c/d";
    private static final String MQTT_PATTERN_MULTI = "+/b/+/d";
    private static final String MQTT_PATTERN_MIXED = "+/b/+/#";

    private static final String MQTT_TOPIC_SHORT = "a/b/c/d";
    private static final String MQTT_TOPIC_FULL = "a/b/c/d/e/f";
    private static final String MQTT_TOPIC_SHORT_ALTERNATE = "a/2/c/d";
    private static final String MQTT_TOPIC_ALTERNATE = "1/b/3/d/4/f";

    @Mock
    private MqttMessageFactory messageFactory;
    @Mock
    private EventHandler<EventExample> eventHandler;

    private Faker faker;
    private String topic;
    private String sharedTopic;
    private String retainedTopic;
    private MqttMessage emptyMessage;

    private SimpleMqttMessageConsumer<EventExample> testee;
    
    @BeforeEach
    public void setUp() throws IOException {
        faker = new Faker();
        topic = this.faker.lorem().word();
        MockitoAnnotations.openMocks(this);
        this.retainedTopic = String.format("%s%s%s%s%s", MqttTopic.RETAIN_MESSAGES_PREFIX, MqttTopic.PATH_SEPARATOR, this.faker.lorem().word(), MqttTopic.PATH_SEPARATOR, this.topic);
        this.sharedTopic = String.format("%s%s%s%s%s", MqttTopic.SHARED_SUBSCRIPTION_PREFIX, MqttTopic.PATH_SEPARATOR, this.faker.lorem().word(), MqttTopic.PATH_SEPARATOR, this.topic);
        emptyMessage = new MqttMessage();
        when(eventHandler.getEventClass()).thenReturn(EventExample.class);
        when(messageFactory.getMqttMessage(any(), anyBoolean())).thenReturn(emptyMessage);
        when(messageFactory.getEvent(any(), any())).thenReturn(null);
    }
    @Test
    public void mqttPatternFullMatchTest() throws IOException {
        this.testee = new SimpleMqttMessageConsumer<>(messageFactory, MQTT_PATTERN_FULL,MQTT_PATTERN_FULL, false, eventHandler);
        boolean result = this.testee.consume(MQTT_TOPIC_SHORT, null);
        verify(messageFactory, times(1)).getEvent(null, EventExample.class);
        assertTrue(result);
    }
    @Test
    public void mqttPatternFullNoMatchTest() throws IOException {
        this.testee = new SimpleMqttMessageConsumer<>(messageFactory, MQTT_PATTERN_FULL, MQTT_PATTERN_FULL, false, eventHandler);
        boolean result = this.testee.consume(MQTT_TOPIC_ALTERNATE, null);
        verify(messageFactory, never()).getEvent(any(), any());
        assertFalse(result);
    }
    @Test
    public void mqttPatternSimpleMatchTest() throws IOException {
        this.testee = new SimpleMqttMessageConsumer<>(messageFactory, MQTT_PATTERN_SIMPLE,MQTT_PATTERN_SIMPLE, false, eventHandler);
        boolean result = this.testee.consume(MQTT_TOPIC_FULL, null);
        verify(messageFactory, times(1)).getEvent(null, EventExample.class);
        assertTrue(result);
    }
    @Test
    public void mqttPatternSimpleNoMatchTest() throws IOException {
        this.testee = new SimpleMqttMessageConsumer<>(messageFactory, MQTT_PATTERN_SIMPLE,MQTT_PATTERN_SIMPLE, false, eventHandler);
        boolean result = this.testee.consume(MQTT_TOPIC_ALTERNATE, null);
        verify(messageFactory, never()).getEvent(any(), any());
        assertFalse(result);
    }
    @Test
    public void mqttPatternInnerMatchTest() throws IOException {
        this.testee = new SimpleMqttMessageConsumer<>(messageFactory, MQTT_PATTERN_INNER, MQTT_PATTERN_INNER, false, eventHandler);
        boolean result = this.testee.consume(MQTT_TOPIC_SHORT_ALTERNATE, null);
        verify(messageFactory, times(1)).getEvent(null, EventExample.class);
        assertTrue(result);
    }
    @Test
    public void mqttPatternInnerNoMatchTest() throws IOException {
        this.testee = new SimpleMqttMessageConsumer<>(messageFactory, MQTT_PATTERN_INNER,MQTT_PATTERN_INNER, false, eventHandler);
        boolean result = this.testee.consume(MQTT_TOPIC_ALTERNATE, null);
        verify(messageFactory, never()).getEvent(any(), any());
        assertFalse(result);
    }
    @Test
    public void mqttPatternMultiMatchTest() throws IOException {
        this.testee = new SimpleMqttMessageConsumer<>(messageFactory, MQTT_PATTERN_MULTI,MQTT_PATTERN_MULTI, false, eventHandler);
        boolean result = this.testee.consume(MQTT_TOPIC_SHORT, null);
        verify(messageFactory, times(1)).getEvent(null, EventExample.class);
        assertTrue(result);
    }
    @Test
    public void mqttPatternMultiNoMatchTest() throws IOException {
        this.testee = new SimpleMqttMessageConsumer<>(messageFactory, MQTT_PATTERN_MULTI,MQTT_PATTERN_MULTI, false, eventHandler);
        boolean result = this.testee.consume(MQTT_TOPIC_FULL, null);
        verify(messageFactory, never()).getEvent(any(), any());
        assertFalse(result);
    }
    @Test
    public void mqttPatternMixedMatchTest() throws IOException {
        this.testee = new SimpleMqttMessageConsumer<>(messageFactory, MQTT_PATTERN_MIXED,MQTT_PATTERN_MIXED, false, eventHandler);
        boolean result = this.testee.consume(MQTT_TOPIC_ALTERNATE, null);
        verify(messageFactory, times(1)).getEvent(null, EventExample.class);
        assertTrue(result);
    }
    @Test
    public void mqttPatternMixedNoMatchTest() throws IOException {
        this.testee = new SimpleMqttMessageConsumer<>(messageFactory, MQTT_PATTERN_MIXED,MQTT_PATTERN_MIXED, false, eventHandler);
        boolean result = this.testee.consume(MQTT_TOPIC_SHORT_ALTERNATE, null);
        verify(messageFactory, never()).getEvent(any(), any());
        assertFalse(result);
    }

    @Test
    public void getMqttSubscriptionsSimple() throws IOException {
        String fakeName = this.faker.lorem().word();
        this.testee = new SimpleMqttMessageConsumer<>(messageFactory, fakeName, this.topic, false, eventHandler);
        MqttSubscription result = this.testee.getMqttSubscription();
        assertEquals(result.getTopic(), this.topic);
    }

    @Test
    public void getMqttSubscriptionsShared() throws IOException {
        this.testee = new SimpleMqttMessageConsumer<>(messageFactory, this.sharedTopic, this.topic, true, eventHandler);
        MqttSubscription result = this.testee.getMqttSubscription();
        assertEquals(result.getTopic(), this.sharedTopic);
    }

    @Test
    public void getMqttSubscriptionsRetained() throws IOException {
        this.testee = new SimpleMqttMessageConsumer<>(messageFactory, this.retainedTopic, this.topic, false, eventHandler);
        MqttSubscription result = this.testee.getMqttSubscription();
        assertEquals(result.getTopic(), this.topic);
    }
}
