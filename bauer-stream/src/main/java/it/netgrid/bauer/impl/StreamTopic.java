package it.netgrid.bauer.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import it.netgrid.bauer.EventHandler;
import it.netgrid.bauer.Topic;

public class StreamTopic<E> implements Topic<E>, StreamMessageConsumer {

    private static final String TOPIC_GLUE = "/";
    private static final String TOPIC_WILD_STEP = "+";
    private static final String TOPIC_WILD_TAIL = "#";

    private static final Logger log = LoggerFactory.getLogger(StreamTopic.class);

    private final String name;

    private final List<EventHandler<E>> handlers;

    private final StreamManager manager;

    private final StreamMessageFactory messageFactory;

    private final String[] patternLevels;

    public StreamTopic(StreamManager streamManager, StreamMessageFactory messageFactory, String name) {
        this.messageFactory = messageFactory;
        this.manager = streamManager;
        this.handlers = new ArrayList<>();
        this.name = name;
        this.patternLevels = name.split(TOPIC_GLUE);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public synchronized void addHandler(EventHandler<E> handler) {
        this.handlers.add(handler);
        if (this.handlers.size() == 1) {
            this.manager.addMessageConsumer(this);
        }
    }

    @Override
    public void post(E event) {
        try {
            StreamEvent<E> streamEvent = new StreamEvent<E>(name, event);
            JsonNode message = this.messageFactory.buildMessage(streamEvent);
            this.manager.postMessage(message);
        } catch (IOException e) {
            log.warn(String.format("cannot serialize on %s: %s", this.name, e.getMessage()));
        }

    }

    @Override
    public boolean consume(JsonNode message) {

        try {
            StreamEvent<JsonNode> streamEvent = this.messageFactory.buildEvent(message);

            if (this.match(streamEvent.topic())) {
                for (EventHandler<E> handler : this.handlers) {
                    try {
                        StreamEvent<E> event = this.messageFactory.buildEvent(message, handler.getEventClass());
                        handler.handle(event.topic(), event.payload());
                    } catch (Exception e) {
                        log.warn(String.format("error on %s %s: %s", streamEvent.topic(), handler.getName(),
                                streamEvent.payload().toString()));
                    }
                }
            }
        } catch (IOException e) {
            log.warn(String.format("invalid content: %s", message.toString()));
        }
        return true;
    }

    public boolean match(String topic) {
        if(topic == null) return false;
        
        String[] topicLevels = topic.split(TOPIC_GLUE);

        int index = 0;

        while (index < patternLevels.length) {
            String patternPart = patternLevels[index];

            if (patternPart.equals(TOPIC_WILD_TAIL)) {
                return index == patternLevels.length - 1;
            }

            if (patternPart.equals(TOPIC_WILD_STEP)) {
                index++;
                if (index >= topicLevels.length && index < patternLevels.length) {
                    return false;
                }
                continue;
            }

            if (index >= topicLevels.length || !patternPart.equals(topicLevels[index])) {
                return false;
            }

            index++;
        }

        return index == topicLevels.length && index == patternLevels.length;
    }

}
