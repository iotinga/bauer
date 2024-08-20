package it.netgrid.bauer.impl.impl;

import java.util.Properties;

import it.netgrid.bauer.TopicFactory;
import it.netgrid.bauer.impl.StreamConfig;
import it.netgrid.bauer.impl.StreamConfigProvider;

public class StreamConfigFromPropertiesProvider implements StreamConfigProvider {

        public static final String STREAM_TOPIC_ATTRIBUTE_PROP = "stream_message_topic_attribute";
        public static final String STREAM_PAYLOAD_ATTRIBUTE_PROP = "stream_message_payload_attribute";
        public static final String STREAM_STOP_BUBBLE_PROP = "stream_stop_bubble";

        public static final String STREAM_TOPIC_ATTRIBUTE_DEFAULT = "topic";
        public static final String STREAM_PAYLOAD_ATTRIBUTE_DEFAULT = "payload";
        public static final String STREAM_STOP_BUBBLE_DEFAULT = "1";

        private StreamConfig config;

        @Override
        public StreamConfig get() {
                return this.get(TopicFactory.getProperties());
        }

        public StreamConfig get(Properties p) {
                if (config != null) {
                        String topicAttribute = p.getProperty(STREAM_TOPIC_ATTRIBUTE_PROP,
                                        STREAM_TOPIC_ATTRIBUTE_DEFAULT);
                        String payloadAttribute = p.getProperty(STREAM_PAYLOAD_ATTRIBUTE_PROP,
                                        STREAM_PAYLOAD_ATTRIBUTE_DEFAULT);
                        String stopBubble = p.getProperty(STREAM_STOP_BUBBLE_PROP, STREAM_STOP_BUBBLE_DEFAULT);

                        this.config = new StreamConfig(topicAttribute, payloadAttribute,
                                        Boolean.parseBoolean(stopBubble));
                }

                return config;
        }

}
