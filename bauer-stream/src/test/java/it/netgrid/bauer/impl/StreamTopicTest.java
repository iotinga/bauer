package it.netgrid.bauer.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.javafaker.Faker;
import it.netgrid.bauer.EventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;

public class StreamTopicTest {

    private EventExample eventExample;
    private StreamManager streamManager;
    private StreamMessageFactory messageFactory;
    private EventHandler<EventExample> handler;
    private Faker faker;
    private String topicName;

    @BeforeEach
    public void setUp() {
        faker = new Faker();
        eventExample = new EventExample();
        eventExample.setField1(faker.lorem().word());
        streamManager = mock(StreamManager.class);
        messageFactory = mock(StreamMessageFactory.class);
        handler = mock(EventHandler.class);
        topicName = faker.lorem().word();
    }

    @Test
    public void testAddHandler() {
        StreamTopic<EventExample> streamTopic = new StreamTopic<>(streamManager, messageFactory, topicName);
        streamTopic.addHandler(handler);
        verify(streamManager, times(1)).addMessageConsumer(streamTopic);
    }

    @Test
    public void testPost() throws IOException {
        StreamTopic<EventExample> streamTopic = new StreamTopic<>(streamManager, messageFactory, topicName);
        JsonNode jsonNode = mock(JsonNode.class);
        when(messageFactory.buildMessage(any())).thenReturn(jsonNode);

        streamTopic.post(eventExample);

        ArgumentCaptor<JsonNode> captor = ArgumentCaptor.forClass(JsonNode.class);
        verify(streamManager, times(1)).postMessage(captor.capture());
        assert captor.getValue() == jsonNode;
    }

    @Test
    public void testConsume() throws Exception {
        StreamTopic<EventExample> streamTopic = new StreamTopic<>(streamManager, messageFactory, topicName);

        JsonNode message = mock(JsonNode.class);
        StreamEvent<JsonNode> streamEvent = new StreamEvent<JsonNode>(streamTopic.getName(), message);
        StreamEvent<EventExample> streamEventExample = new StreamEvent<EventExample>(streamTopic.getName(),
                eventExample);
        when(messageFactory.buildEvent(message)).thenReturn(streamEvent);
        when(messageFactory.buildEvent(message, EventExample.class)).thenReturn(streamEventExample);

        when(handler.getEventClass()).thenReturn(EventExample.class);
        streamTopic.addHandler(handler);

        streamTopic.consume(message);

        verify(handler, times(1)).handle(streamTopic.getName(), eventExample);
    }
}