package it.netgrid.bauer.impl.impl;

import java.util.Properties;

import com.google.inject.Inject;

import it.netgrid.bauer.impl.StreamConfig;
import it.netgrid.bauer.impl.StreamConfigProvider;

public class StreamConfigFromPropertiesProvider implements StreamConfigProvider {

    private StreamConfig config;

    private final Properties p;

    @Inject
    public StreamConfigFromPropertiesProvider(Properties properties) {
        this.p = properties;
    }

    @Override
    public StreamConfig config() {
        if (config != null) {
            String topicAttribute = p.getProperty(StreamConfig.STREAM_TOPIC_ATTRIBUTE,
                    StreamConfig.STREAM_TOPIC_ATTRIBUTE_DEFAULT);
            String payloadAttribute = p.getProperty(StreamConfig.STREAM_PAYLOAD_ATTRIBUTE,
                    StreamConfig.STREAM_PAYLOAD_ATTRIBUTE_DEFAULT);
            String stopBubble = p.getProperty(StreamConfig.STREAM_MESSAGE_BUBBLING_ENABLED,
                    StreamConfig.STREAM_MESSAGE_BUBBLING_ENABLED_DEFAULT);

            this.config = new StreamConfigImpl(topicAttribute, payloadAttribute,
                    Boolean.parseBoolean(stopBubble));
        }

        return config;
    }

}
