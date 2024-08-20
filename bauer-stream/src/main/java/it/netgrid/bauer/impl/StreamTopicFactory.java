package it.netgrid.bauer.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;

import it.netgrid.bauer.ITopicFactory;
import it.netgrid.bauer.Topic;

public class StreamTopicFactory implements ITopicFactory {

    private static final Logger log = LoggerFactory.getLogger(StreamTopicFactory.class);

    private final StreamManager manager;
    private final StreamMessageFactory messageFactory;
    private final Map<String, StreamTopic<?>> topics;

    @Inject
    public StreamTopicFactory(StreamManager manager, StreamMessageFactory messageFactory) {
        this.topics = new HashMap<>();
        this.messageFactory = messageFactory;
        this.manager = manager;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> Topic<E> getTopic(String name) {
        if(!topics.containsKey(name)) {
            StreamTopic<E> topic = new StreamTopic<>(manager, messageFactory, name);
            topics.put(name, topic);
        } else {
            log.debug(String.format("Topic %s already exists", name));
        }

        return (Topic<E>) topics.get(name);
    }

}
