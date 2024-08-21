package it.netgrid.bauer.impl.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.javafaker.Faker;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import it.netgrid.bauer.impl.EventExample;
import it.netgrid.bauer.impl.StreamConfig;
import it.netgrid.bauer.impl.StreamEvent;

public class SimpleStreamMessageFactoryTest {

    @Mock
    private StreamConfig config;

    private SimpleStreamMessageFactory factory;
    private Faker faker;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        faker = new Faker();
        factory = new SimpleStreamMessageFactory(config);
    }

    @Test
    public void testBuildEventWithNullMessage() throws IOException {
        when(config.topicAttribute()).thenReturn(null);
        when(config.payloadAttribute()).thenReturn(null);

        StreamEvent<JsonNode> event = factory.buildEvent(null);

        assertEquals("", event.topic());
        assertEquals(NullNode.instance, event.payload());
    }

    @Test
    public void testBuildEventWithValidMessage() throws IOException {
        String topicAttr = faker.lorem().word();
        String payloadAttr = faker.lorem().word();
        String topicValue = faker.lorem().word();
        String payloadValue = faker.lorem().sentence();

        when(config.topicAttribute()).thenReturn(topicAttr);
        when(config.payloadAttribute()).thenReturn(payloadAttr);

        ObjectMapper om = new ObjectMapper();
        ObjectNode message = om.createObjectNode();
        message.put(topicAttr, topicValue);
        message.put(payloadAttr, payloadValue);

        StreamEvent<JsonNode> event = factory.buildEvent(message);

        assertEquals(topicValue, event.topic());
        assertEquals(payloadValue, event.payload().asText());
    }

    @Test
    public void testBuildTypedEventWithValidMessage() throws IOException {
        ObjectMapper om = new ObjectMapper();
        ObjectNode message = om.createObjectNode();
        String topicAttr = faker.lorem().word();
        String payloadAttr = faker.lorem().word();
        String topicValue = faker.lorem().word();
        String field1Value = faker.lorem().word();
        ObjectNode payloadValue = om.createObjectNode();
        payloadValue.put("field1", field1Value);

        when(config.topicAttribute()).thenReturn(topicAttr);
        when(config.payloadAttribute()).thenReturn(payloadAttr);

        message.put(topicAttr, topicValue);
        message.set(payloadAttr, payloadValue);

        StreamEvent<EventExample> event = factory.buildEvent(message, EventExample.class);

        assertEquals(topicValue, event.topic());
        assertEquals(field1Value, event.payload().getField1());
    }

    @Test
    public void testBuildMessage() throws IOException {
        String topicAttr = faker.lorem().word();
        String payloadAttr = faker.lorem().word();
        String topicValue = faker.lorem().word();
        String payloadValue = faker.lorem().sentence();

        when(config.topicAttribute()).thenReturn(topicAttr);
        when(config.payloadAttribute()).thenReturn(payloadAttr);

        StreamEvent<String> event = new StreamEvent<>(topicValue, payloadValue);

        JsonNode message = factory.buildMessage(event);

        assertEquals(topicValue, message.get(topicAttr).asText());
        assertEquals(payloadValue, message.get(payloadAttr).asText());
    }
}
