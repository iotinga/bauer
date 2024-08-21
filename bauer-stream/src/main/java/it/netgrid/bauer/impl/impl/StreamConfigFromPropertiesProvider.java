package it.netgrid.bauer.impl.impl;

import java.util.Properties;

import com.google.inject.Inject;

import it.netgrid.bauer.impl.StreamConfig;
import it.netgrid.bauer.impl.StreamConfigProvider;

public class StreamConfigFromPropertiesProvider implements StreamConfigProvider {

    public static final String STREAM_TOPIC_ATTRIBUTE_PROP = "stream_message_topic_attribute";
    public static final String STREAM_PAYLOAD_ATTRIBUTE_PROP = "stream_message_payload_attribute";
    public static final String STREAM_MESSAGE_BUBBLING_ENABLED_PROP = "stream_stop_bubble";

    public static final String STREAM_TOPIC_ATTRIBUTE_DEFAULT = "topic";
    public static final String STREAM_PAYLOAD_ATTRIBUTE_DEFAULT = "payload";
    public static final String STREAM_MESSAGE_BUBBLING_ENABLED_DEFAULT = "0";

    private StreamConfig config;

    private final Properties p;

    @Inject
    public StreamConfigFromPropertiesProvider(Properties properties) {
        this.p = properties;
    }

    @Override
    public StreamConfig get() {
        if (config != null) {
            String topicAttribute = p.getProperty(STREAM_TOPIC_ATTRIBUTE_PROP,
                    STREAM_TOPIC_ATTRIBUTE_DEFAULT);
            String payloadAttribute = p.getProperty(STREAM_PAYLOAD_ATTRIBUTE_PROP,
                    STREAM_PAYLOAD_ATTRIBUTE_DEFAULT);
            String stopBubble = p.getProperty(STREAM_MESSAGE_BUBBLING_ENABLED_PROP,
                    STREAM_MESSAGE_BUBBLING_ENABLED_DEFAULT);

            this.config = new StreamConfig(topicAttribute, payloadAttribute,
                    Boolean.parseBoolean(stopBubble));
        }

        return config;
    }

}
