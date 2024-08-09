package it.netgrid.bauer.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.github.javafaker.Faker;

import it.netgrid.bauer.Topic;

public class MqttTopicFactoryTest {
    
    @Mock
    private MqttClientManager manager;
    @Mock
    private MqttMessageFactory messageFactory;

    private MqttTopicFactory testee;

    private Faker faker;

    @BeforeEach
    public void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        this.faker = new Faker();
        this.testee = new MqttTopicFactory(manager, messageFactory);
    }

    @Test
    public void getTopicBuildNoNull() {
        String topicName = this.faker.lorem().word();
        Topic<?> result = this.testee.getTopic(topicName);
        assertNotNull(result);
    }

    @Test
    public void getTopicBuildOnlyOneTopicByName() {
        String topicName = this.faker.lorem().word();
        Topic<?> result1 = this.testee.getTopic(topicName);
        Topic<?> result2 = this.testee.getTopic(topicName);
        assertEquals(result1, result2);
        assertEquals(this.testee.activeTopics(), 1);
    }

}
