package it.netgrid.bauer.impl;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.devskiller.jfairy.Fairy;

import it.netgrid.bauer.Topic;

@TestInstance(Lifecycle.PER_CLASS)
public class MqttTopicFactoryTest {
    
    @Mock
    private MqttClientManager manager;
    @Mock
    private MqttMessageFactory messageFactory;

    private MqttTopicFactory testee;

    private Fairy fairy;

    @BeforeEach
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        this.fairy = Fairy.create();
        this.testee = new MqttTopicFactory(manager, messageFactory);
    }

    @Test
    public void getTopicBuildNoNull() {
        String topicName = this.fairy.textProducer().latinWord();
        Topic<?> result = this.testee.getTopic(topicName);
        Assertions.assertNotNull(result);
    }

    @Test
    public void getTopicBuildOnlyOneTopicByName() {
        String topicName = this.fairy.textProducer().latinWord();
        Topic<?> result1 = this.testee.getTopic(topicName);
        Topic<?> result2 = this.testee.getTopic(topicName);
        Assertions.assertEquals(result1, result2);
        Assertions.assertEquals(this.testee.activeTopics(), 1);
    }

}
