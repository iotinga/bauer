package it.netgrid.bauer.helpers;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class StreamTopicTest {

    private static final String PATTERN_FULL = "a/b/c/d";
    private static final String PATTERN_SIMPLE = "a/b/#";
    private static final String PATTERN_INNER = "a/+/c/d";
    private static final String PATTERN_MULTI = "+/b/+/d";
    private static final String PATTERN_MIXED = "+/b/+/#";

    private static final String TOPIC_SHORT = "a/b/c/d";
    private static final String TOPIC_FULL = "a/b/c/d/e/f";
    private static final String TOPIC_SHORT_ALTERNATE = "a/2/c/d";
    private static final String TOPIC_ALTERNATE = "1/b/3/d/4/f";

    @BeforeEach
    public void setUp() {}

    @Test
    public void mqttPatternFullMatchTest() throws IOException {
        boolean result = TopicUtils.match(PATTERN_FULL, TOPIC_SHORT);
        assertTrue(result);
    }

    @Test
    public void mqttPatternFullNoMatchTest() throws IOException {
        boolean result = TopicUtils.match(PATTERN_FULL, TOPIC_ALTERNATE);
        assertFalse(result);
    }

    @Test
    public void mqttPatternSimpleMatchTest() throws IOException {
        boolean result = TopicUtils.match(PATTERN_SIMPLE, TOPIC_FULL);
        assertTrue(result);
    }

    @Test
    public void mqttPatternSimpleNoMatchTest() throws IOException {
        boolean result = TopicUtils.match(PATTERN_SIMPLE, TOPIC_ALTERNATE);
        assertFalse(result);
    }

    @Test
    public void mqttPatternInnerMatchTest() throws IOException {
        boolean result = TopicUtils.match(PATTERN_INNER, TOPIC_SHORT_ALTERNATE);
        assertTrue(result);
    }

    @Test
    public void mqttPatternInnerNoMatchTest() throws IOException {
        boolean result = TopicUtils.match(PATTERN_INNER, TOPIC_ALTERNATE);
        assertFalse(result);
    }

    @Test
    public void mqttPatternMultiMatchTest() throws IOException {
        boolean result = TopicUtils.match(PATTERN_MULTI, TOPIC_SHORT);
        assertTrue(result);
    }

    @Test
    public void mqttPatternMultiNoMatchTest() throws IOException {
        boolean result = TopicUtils.match(PATTERN_MULTI, TOPIC_FULL);
        assertFalse(result);
    }

    @Test
    public void mqttPatternMixedMatchTest() throws IOException {
        boolean result = TopicUtils.match(PATTERN_MIXED, TOPIC_ALTERNATE);
        assertTrue(result);
    }

    @Test
    public void mqttPatternMixedNoMatchTest() throws IOException {
        boolean result = TopicUtils.match(PATTERN_MIXED, TOPIC_SHORT_ALTERNATE);
        assertFalse(result);
    }
}