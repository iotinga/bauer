package it.netgrid.bauer.impl.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.github.javafaker.Faker;
import it.netgrid.bauer.impl.StreamConfig;
import it.netgrid.bauer.impl.StreamMessageConsumer;
import it.netgrid.bauer.impl.StreamsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamThreadedManagerTest {

    @Mock
    private StreamsProvider streamsProvider;

    @Mock
    private InputStream inputStream;

    @Mock
    private OutputStream outputStream;

    @Mock
    private StreamMessageConsumer consumer;

    @Mock
    private StreamConfig config;

    private StreamThreadedManager manager;
    private Faker faker;
    private ObjectMapper om;
    private CBORFactory cf;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(streamsProvider.input()).thenReturn(inputStream);
        when(streamsProvider.output()).thenReturn(outputStream);
        manager = new StreamThreadedManager(config, streamsProvider);
        faker = new Faker();
        cf = new CBORFactory();
        om = new ObjectMapper(cf);
    }

    @Test
    public void testAddMessageConsumer() {
        manager.addMessageConsumer(consumer);
        verifyNoMoreInteractions(consumer);
    }

    @Test
    public void testUnsafePostMessage() throws Exception {
        ObjectNode fakeNode = JsonNodeFactory.instance.objectNode();
        String fakeKey = faker.lorem().word();
        String fakeValue = faker.name().fullName();
        fakeNode.put(fakeKey, fakeValue);
        OutputStream outputStream = mock(OutputStream.class);

        when(streamsProvider.output()).thenReturn(outputStream);

        manager.unsafePostMessage(fakeNode);

        ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
        verify(outputStream).write(captor.capture());
        verify(outputStream).flush();

        JsonNode writtenMessage = this.om.readTree(captor.getValue());
        assert writtenMessage.get(fakeKey).asText().equals(fakeValue);
    }

    @Test
    public void testTrigger() {
        JsonNode message = mock(JsonNode.class);
        when(config.isMessageBubblingEnabled()).thenReturn(true);

        manager.addMessageConsumer(consumer);
        manager.trigger(message);

        verify(consumer).consume(message);
        verify(config).isMessageBubblingEnabled();
    }

    @Test
    public void testRunCborMessageFetch() throws IOException {
        ObjectNode fakeNode = JsonNodeFactory.instance.objectNode();
        fakeNode.put(faker.lorem().word(), faker.name().fullName());
        byte[] fakeData = this.om.writeValueAsBytes(fakeNode);
        when(inputStream.available()).thenReturn(0).thenReturn(-1);
        when(inputStream.read(any(byte[].class), anyInt(), anyInt())).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(fakeData, 0, buffer, 0, fakeData.length);
            return fakeData.length;
        }).thenReturn(-1);

        manager.unsafeAddMessageConsumer(consumer);
        manager.runCborMessageFetch();

        verify(consumer, times(1)).consume(any(JsonNode.class));
    }

    @Test
    public void testRunCborMessageFetchWithTwoSubsequentMessages() throws IOException {
        ObjectNode fakeNode1 = JsonNodeFactory.instance.objectNode();
        fakeNode1.put(faker.lorem().word(), faker.name().fullName());
        byte[] fakeData1 = this.om.writeValueAsBytes(fakeNode1);

        ObjectNode fakeNode2 = JsonNodeFactory.instance.objectNode();
        fakeNode2.put(faker.lorem().word(), faker.name().fullName());
        byte[] fakeData2 = this.om.writeValueAsBytes(fakeNode2);

        when(inputStream.available()).thenReturn(0).thenReturn(0).thenReturn(-1);
        when(inputStream.read(any(byte[].class), anyInt(), anyInt())).thenReturn(-1).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            int off = invocation.getArgument(1);
            System.arraycopy(fakeData1, 0, buffer, off, fakeData1.length);
            return fakeData1.length;
        }).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            int off = invocation.getArgument(1);
            System.arraycopy(fakeData2, 0, buffer, off, fakeData2.length);
            return fakeData2.length;
        }).thenReturn(-1);

        manager.unsafeAddMessageConsumer(consumer);
        manager.runCborMessageFetch();

        verify(consumer,  times(2)).consume(any(JsonNode.class));
    }

    @Test
    public void testRunCborMessageFetchWithTwoResidentMessages() throws IOException {
        ObjectNode fakeNode1 = JsonNodeFactory.instance.objectNode();
        fakeNode1.put(faker.lorem().word(), faker.name().fullName());
        byte[] fakeData1 = this.om.writeValueAsBytes(fakeNode1);

        ObjectNode fakeNode2 = JsonNodeFactory.instance.objectNode();
        fakeNode2.put(faker.lorem().word(), faker.name().fullName());
        byte[] fakeData2 = this.om.writeValueAsBytes(fakeNode2);

        when(inputStream.available()).thenReturn(0).thenReturn(0).thenReturn(-1);
        when(inputStream.read(any(byte[].class), anyInt(), anyInt())).thenAnswer(invocation -> {
            byte[] buffer = invocation.getArgument(0);
            System.arraycopy(fakeData1, 0, buffer, 0, fakeData1.length);
            System.arraycopy(fakeData2, 0, buffer, fakeData1.length, fakeData2.length);
            return fakeData1.length + fakeData2.length;
        }).thenReturn(-1);

        manager.unsafeAddMessageConsumer(consumer);
        manager.runCborMessageFetch();

        verify(consumer,  times(2)).consume(any(JsonNode.class));
    }
}