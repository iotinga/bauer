package it.netgrid.bauer.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.netgrid.bauer.EventHandler;
import it.netgrid.bauer.Topic;
import it.netgrid.bauer.impl.impl.SimpleMqttMessageConsumer;

public class MqttTopic<E> implements Topic<E> {

    private static final Logger log = LoggerFactory.getLogger(MqttTopic.class);

    public static final String SHARED_SUBSCRIPTION_PREFIX = "$share";
    public static final String RETAIN_MESSAGES_PREFIX = "$retain";
    public static final String PATH_SEPARATOR = "/";

    private final String name;
    private final MqttClientManager manager;
    private final MqttMessageFactory factory;
    private final Map<EventHandler<E>, MqttMessageConsumer> handlers;
    private final String mqttPattern;

    private final boolean retains;
    private final boolean sharedSubscription;

    public MqttTopic(MqttMessageFactory messageFactory, MqttClientManager manager, String name, String mqttPattern,
            boolean sharedSubscription, boolean retains) {
        this.name = name;
        this.factory = messageFactory;
        this.manager = manager;
        this.handlers = new HashMap<>();
        this.sharedSubscription = sharedSubscription;
        this.retains = retains;
        this.mqttPattern = mqttPattern;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void addHandler(EventHandler<E> handler) {
        if (!handlers.containsKey(handler)) {
            MqttMessageConsumer consumer = new SimpleMqttMessageConsumer<E>(this.factory, this.name, this.mqttPattern,
                    this.sharedSubscription, handler);
            this.handlers.put(handler, consumer);
            try {
                this.manager.addConsumer(consumer);
                log.info(String.format("%s: %s registered", this.name, handler.getName()));
            } catch (IOException e) {
                this.handlers.remove(handler);
                log.error(String.format("Unable to register %s: %s", this.name, handler.getName()));
            }
        } else {
            log.debug(String.format("%s: %s already registered", this.name, handler.getName()));
        }
    }

    @Override
    public void post(E event) {
        MqttMessage message;
        try {
            message = this.factory.getMqttMessage(event, retains);
            this.manager.publish(this.mqttPattern, message);
        } catch (IOException e) {
            log.error(String.format("Unable to post %s %s", this.mqttPattern, e));
        }
    }

}
