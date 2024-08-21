package it.netgrid.bauer.impl.impl;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.MqttSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.netgrid.bauer.EventHandler;
import it.netgrid.bauer.helpers.TopicUtils;
import it.netgrid.bauer.impl.MqttMessageConsumer;
import it.netgrid.bauer.impl.MqttMessageFactory;
import it.netgrid.bauer.impl.MqttTopic;

public class SimpleMqttMessageConsumer<E> implements MqttMessageConsumer {

    private class Nullable {
        public final E payload;
        public final String topic;

        Nullable(String topic, E payload) {
            this.payload = payload;
            this.topic = topic;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(SimpleMqttMessageConsumer.class);
    private final LinkedBlockingQueue<Nullable> incomingEvents;
    private final EventHandler<E> handler;
    private final String mqttPattern;
    private final boolean sharedSubscription;
    private final MqttMessageFactory factory;
    private final String name;

    public SimpleMqttMessageConsumer(MqttMessageFactory factory, String name, String mqttPattern,
            boolean sharedSubscription, EventHandler<E> handler) {
        this.name = name;
        this.mqttPattern = mqttPattern;
        this.sharedSubscription = sharedSubscription;
        this.factory = factory;
        this.handler = handler;
        this.incomingEvents = new LinkedBlockingQueue<>();
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                Nullable event = incomingEvents.take();
                this.handler.handle(event.topic, event.payload);
            }
        } catch (Exception e) {
            log.warn(String.format("%s handler: %s", mqttPattern, e.getMessage()));
        }
    }

    @Override
    public boolean consume(String topic, MqttMessage message) throws IOException {
        if (this.matches(topic)) {
            try {
                E event = this.factory.getEvent(message, this.handler.getEventClass());
                this.incomingEvents.put(new Nullable(topic, event));
            } catch (Exception e) {
                throw new IOException(e);
            }
            return true;
        }

        return false;
    }

    public String normalizedTopicFrom(String fullTopic) {
        String sharedPrefix = MqttTopic.SHARED_SUBSCRIPTION_PREFIX + MqttTopic.PATH_SEPARATOR;
        if (fullTopic.startsWith(sharedPrefix)) {
            String[] parts = fullTopic.split(MqttTopic.PATH_SEPARATOR, 3);
            return parts[parts.length - 1];
        }

        return fullTopic;
    }

    public boolean matches(String fullTopic) {
        String normalized = this.normalizedTopicFrom(fullTopic);
        return TopicUtils.match(this.mqttPattern, normalized);
    }

    @Override
    public String toString() {
        return String.format("%s->%s[%s]", this.mqttPattern, this.handler.getName(),
                this.handler.getEventClass().getSimpleName());
    }

    @Override
    public MqttSubscription getMqttSubscription() {
        String topic = this.sharedSubscription ? this.name : this.mqttPattern;
        return new MqttSubscription(topic);
    }
}
