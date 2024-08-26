package it.netgrid.bauer.impl.impl;

import com.fasterxml.jackson.annotation.JsonProperty;

import it.netgrid.bauer.impl.StreamConfig;

public class StreamConfigImpl implements StreamConfig {

    @JsonProperty(StreamConfig.STREAM_TOPIC_ATTRIBUTE)
    private String topicAttribute;

    @JsonProperty(StreamConfig.STREAM_PAYLOAD_ATTRIBUTE)
    private String payloadAttribute;

    @JsonProperty(StreamConfig.STREAM_MESSAGE_BUBBLING_ENABLED)
    private Boolean isMessageBubblingEnabled;

    public StreamConfigImpl() {}

    public StreamConfigImpl(String topicAttribute, String payloadAttribute, Boolean isMessageBubblindEnabled) {
        this.topicAttribute = topicAttribute;
        this.payloadAttribute = payloadAttribute;
        this.isMessageBubblingEnabled = isMessageBubblindEnabled;
    }

    @Override
    public String topicAttribute() {
        return topicAttribute;
    }

    @Override
    public String payloadAttribute() {
        return payloadAttribute;
    }

    @Override
    public Boolean isMessageBubblingEnabled() {
        return isMessageBubblingEnabled;
    }
}
