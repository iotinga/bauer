package it.netgrid.bauer.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.netgrid.bauer.ITopicFactory;
import it.netgrid.bauer.Topic;

public class MqttTopicFactory implements ITopicFactory {

    private static final Logger log = LoggerFactory.getLogger(MqttTopicFactory.class);

    private final MqttClientManager manager;
    private final MqttMessageFactory messageFactory;

    private final Map<String, MqttTopic<?>> topics;

    public MqttTopicFactory(MqttClientManager manager, MqttMessageFactory messageFactory) {
        this.topics = new HashMap<>();
        this.messageFactory = messageFactory;
        this.manager = manager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> Topic<E> getTopic(String name) {
        if (!topics.containsKey(name)) {
            MqttTopic<E> topic = new MqttTopic<E>(this.messageFactory, this.manager, name, this.mqttPatternFrom(name), this.isSharedTopic(name),
                    this.isRetainedTopic(name));
            topics.put(name, topic);
        } else {
            log.debug("Topic %s already exists", name);
        }

        return (Topic<E>) topics.get(name);
    }

    public String mqttPatternFrom(String name) {
        String retainPrefix = MqttTopic.RETAIN_MESSAGES_PREFIX + MqttTopic.PATH_SEPARATOR;
        if (name.startsWith(retainPrefix)) {
            return name.substring(retainPrefix.length());
        }

        String sharedPrefix = MqttTopic.SHARED_SUBSCRIPTION_PREFIX + MqttTopic.PATH_SEPARATOR;
        if (name.startsWith(sharedPrefix)) {
            String[] parts = name.split(MqttTopic.PATH_SEPARATOR, 3);
            return parts[parts.length - 1];
        }

        return name;
    }

    public boolean isRetainedTopic(String name) {
        return name != null && name.startsWith(MqttTopic.RETAIN_MESSAGES_PREFIX + MqttTopic.PATH_SEPARATOR);
    }

    public boolean isSharedTopic(String name) {
        return name != null && name.startsWith(MqttTopic.SHARED_SUBSCRIPTION_PREFIX + MqttTopic.PATH_SEPARATOR);
    }

    public int activeTopics() {
        return this.topics.keySet().size();
    }

}
