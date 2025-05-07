package it.netgrid.bauer.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import it.netgrid.bauer.EventHandler;
import it.netgrid.bauer.Topic;
import it.netgrid.bauer.helpers.TopicUtils;

public class StreamTopic<E> implements Topic<E>, StreamMessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(StreamTopic.class);

    private final String name;

    private final List<EventHandler<E>> handlers;

    private final StreamManager manager;

    private final StreamMessageFactory messageFactory;

    public StreamTopic(StreamManager streamManager, StreamMessageFactory messageFactory, String name) {
        this.messageFactory = messageFactory;
        this.manager = streamManager;
        this.handlers = new ArrayList<>();
        this.name = name == null ? "" : name;
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
    public synchronized void removeHandler(EventHandler<E> handler) {
        this.handlers.remove(handler);
        if (this.handlers.isEmpty()) {
            log.warn("Should probably do something with the stream manager when there are no handlers");
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

            if (TopicUtils.match(this.name, streamEvent.topic())) {
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
}
