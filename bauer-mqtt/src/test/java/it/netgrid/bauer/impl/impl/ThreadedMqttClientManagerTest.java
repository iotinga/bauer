package it.netgrid.bauer.impl.impl;

import java.io.IOException;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.MqttSubscription;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.devskiller.jfairy.Fairy;

import it.netgrid.bauer.EventHandler;
import it.netgrid.bauer.impl.EventExample;
import it.netgrid.bauer.impl.MqttMessageConsumer;
import it.netgrid.bauer.impl.MqttMessageFactory;
import it.netgrid.bauer.impl.MqttTopic;

public class ThreadedMqttClientManagerTest {
    @Mock
    private MqttMessageFactory messageFactory;
    @Mock
    private EventHandler<EventExample> eventHandler;
    @Mock
    private MqttMessageConsumer consumer;
    @Mock
    private MqttMessageConsumer sharedConsumer;
    @Mock
    private MqttMessageConsumer retainedConsumer;
    @Mock
    private MqttClient client;

    private Fairy fairy;
    private String topic;
    private String sharedTopic;
    private String retainedTopic;
    private MqttSubscription subscription;
    private MqttSubscription sharedSubscription;
    private MqttSubscription retainedSubscription;
    private MqttMessage emptyMessage;
    private ThreadedMqttClientManager testee;
    
    @BeforeEach
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        this.fairy = Fairy.create();
        this.topic = this.fairy.textProducer().latinWord();
        this.retainedTopic = String.format("%s%s%s%s%s", MqttTopic.RETAIN_MESSAGES_PREFIX, MqttTopic.PATH_SEPARATOR, this.fairy.textProducer().latinWord(), MqttTopic.PATH_SEPARATOR, this.topic);
        this.sharedTopic = String.format("%s%s%s%s%s", MqttTopic.SHARED_SUBSCRIPTION_PREFIX, MqttTopic.PATH_SEPARATOR, this.fairy.textProducer().latinWord(), MqttTopic.PATH_SEPARATOR, this.topic);
        emptyMessage = new MqttMessage();
        this.testee = new ThreadedMqttClientManager(client);
        this.subscription = new MqttSubscription(this.topic);
        this.sharedSubscription = new MqttSubscription(this.sharedTopic);
        this.retainedSubscription = new MqttSubscription(this.topic);

        when(consumer.getMqttSubscription()).thenReturn(this.subscription);
        when(sharedConsumer.getMqttSubscription()).thenReturn(this.sharedSubscription);
        when(retainedConsumer.getMqttSubscription()).thenReturn(this.retainedSubscription);
        when(consumer.consume(any(), any())).thenReturn(true);
        when(sharedConsumer.consume(any(), any())).thenReturn(true);
    }

    @Test
    public void addConsumerEnqueueSubscriptionTest() throws IOException, MqttException {
        this.testee.addConsumer(consumer);
        Assertions.assertEquals(this.testee.pendingSubscriptions(), 1);
    }

    @Test
    public void addConsumerMultipleTimesEnqueuesOnlyOneSubscriptionTest() throws IOException, MqttException {
        this.testee.addConsumer(consumer);
        this.testee.addConsumer(consumer);
        this.testee.addConsumer(consumer);
        Assertions.assertEquals(this.testee.pendingSubscriptions(), 1);
    }

    @Test
    public void addConsumerTriggersSubscriptionTest() throws IOException, MqttException {
        this.testee.addConsumer(consumer);
        this.testee.runSubscribeOnce();
        verify(client, times(1)).subscribe(any());
    }

    @Test
    public void messageArrivedSingleConsumerTest() throws Exception {
        this.testee.addConsumer(consumer);
        this.testee.messageArrived(retainedTopic, emptyMessage);
        verify(consumer, times(1)).consume(retainedTopic, emptyMessage);
    }

    @Test
    public void messageArrivedMultiConsumerTest() throws Exception {
        this.testee.addConsumer(consumer);
        this.testee.addConsumer(sharedConsumer);
        this.testee.addConsumer(retainedConsumer);
        this.testee.messageArrived(retainedTopic, emptyMessage);
        verify(consumer, times(1)).consume(retainedTopic, emptyMessage);
        verify(sharedConsumer, times(1)).consume(retainedTopic, emptyMessage);
        verify(retainedConsumer, times(1)).consume(retainedTopic, emptyMessage);
    }
}
