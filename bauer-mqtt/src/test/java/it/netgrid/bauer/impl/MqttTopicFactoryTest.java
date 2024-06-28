package it.netgrid.bauer.impl;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.codearte.jfairy.Fairy;

import it.netgrid.bauer.Topic;

public class MqttTopicFactoryTest {
    
    @Mock
    private MqttClientManager manager;
    @Mock
    private MqttMessageFactory messageFactory;

    private MqttTopicFactory testee;

    private Fairy fairy;

    @Before
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        this.fairy = Fairy.create();
        this.testee = new MqttTopicFactory(manager, messageFactory);
    }

    @Test
    public void getTopicBuildNoNull() {
        String topicName = this.fairy.textProducer().latinWord();
        Topic<?> result = this.testee.getTopic(topicName);
        assertNotNull(result);
    }

    @Test
    public void getTopicBuildOnlyOneTopicByName() {
        String topicName = this.fairy.textProducer().latinWord();
        Topic<?> result1 = this.testee.getTopic(topicName);
        Topic<?> result2 = this.testee.getTopic(topicName);
        assertEquals(result1, result2);
        assertEquals(this.testee.activeTopics(), 1);
    }

}
