package it.netgrid.bauer.impl.impl;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.inject.Inject;

import it.netgrid.bauer.impl.StreamConfig;
import it.netgrid.bauer.impl.StreamEvent;
import it.netgrid.bauer.impl.StreamMessageFactory;

public class SimpleStreamMessageFactory implements StreamMessageFactory {

    private static final String DEFAULT_TOPIC = "";
    private static final JsonNode DEFAULT_PAYLOAD =  NullNode.instance;

    private final ObjectMapper om;

    private final StreamConfig config;

    @Inject
    public SimpleStreamMessageFactory(StreamConfig config) {
        this.om = new ObjectMapper();
        this.config = config;
    }

    @Override
    public StreamEvent<JsonNode> buildEvent(JsonNode message) throws IOException {

        String topic = DEFAULT_TOPIC;
        JsonNode payload = DEFAULT_PAYLOAD;

        if(message == null || message == NullNode.instance) {
            return new StreamEvent<JsonNode>(topic, payload);
        }

        if(config.topicAttribute() != null) {
            String topicValue = message.get(config.topicAttribute()).asText();
            if(topicValue != null) {
                topic = topicValue;
            }
        }

        if(config.payloadAttribute() != null) {
            JsonNode payloadValue = message.get(config.payloadAttribute());
            if(payloadValue != null) {
                payload = payloadValue;
            }
        }

        return new StreamEvent<JsonNode>(topic, payload);
    }

    @Override
    public <E> StreamEvent<E> buildEvent(JsonNode message, Class<E> eventClass) throws IOException {
        StreamEvent<JsonNode> rawEvent = this.buildEvent(message);
        E payload = this.om.convertValue(rawEvent.payload(), eventClass);
        return new StreamEvent<E>(rawEvent.topic(), payload);
    }

    @Override
    public <E> JsonNode buildMessage(StreamEvent<E> event) throws IOException {
        ObjectNode output = this.om.createObjectNode();
        String topic = DEFAULT_TOPIC;
        JsonNode payload = DEFAULT_PAYLOAD;

        if(event != null) {
            payload = this.om.valueToTree(event.payload());
            topic = event.topic();
        }

        String payloadAttribute = config.payloadAttribute() == null ? null : config.payloadAttribute().trim();
        if(payloadAttribute == null || payloadAttribute.length() < 1) {
            return payload;
        }

        output.set(payloadAttribute, payload);

        String topicAttribute = config.topicAttribute() == null ? null : config.topicAttribute().trim();
        if(topicAttribute != null) {
            output.set(topicAttribute, TextNode.valueOf(topic));
        }

        return output;
    }
    
}
