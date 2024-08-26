package it.netgrid.bauer.impl;

public interface StreamConfig {
    
    public static final String STREAM_TOPIC_ATTRIBUTE = "stream_message_topic_attribute";
    public static final String STREAM_PAYLOAD_ATTRIBUTE = "stream_message_payload_attribute";
    public static final String STREAM_MESSAGE_BUBBLING_ENABLED = "stream_stop_bubble";
    
    public static final String STREAM_TOPIC_ATTRIBUTE_DEFAULT = "topic";
    public static final String STREAM_PAYLOAD_ATTRIBUTE_DEFAULT = "payload";
    public static final String STREAM_MESSAGE_BUBBLING_ENABLED_DEFAULT = "0";
    
    String topicAttribute();
    String payloadAttribute();
    Boolean isMessageBubblingEnabled();
}
