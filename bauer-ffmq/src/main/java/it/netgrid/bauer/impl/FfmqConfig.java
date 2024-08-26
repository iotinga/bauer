package it.netgrid.bauer.impl;

public interface FfmqConfig {

    public static final String FFMQ_PROVIDER_URL = "ffmqProvider";
    public static final String FFMQ_MESSAGE_HANDLER_RETRY_RATE = "messageHandlerRetry";
    public static final String FFMQ_MESSAGE_HANDLER_MAX_RETRY = "messageHandlerMaxRetry";
    public static final String FFMQ_TOPIC_USERNAME_NAME_FORMAT = "messageHandlerMaxRetry";
    public static final String FFMQ_TOPIC_PASSWORD_NAME_FORMAT = "messageHandlerMaxRetry";

    public static final String FFMQ_PROVIDER_URL_DEFAULT = "tcp://localhost:10002";
    public static final String FFMQ_MESSAGE_HANDLER_RETRY_RATE_DEFAULT = "2000";
    public static final String FFMQ_MESSAGE_HANDLER_MAX_RETRY_DEFAULT = "10";

    public static final String FFMQ_TOPIC_USERNAME_NAME_FORMAT_DEFAULT = "topic.%s.username";
    public static final String FFMQ_TOPIC_PASSWORD_NAME_FORMAT_DEFAULT = "topic.%s.password";

    String providerUrl();
    int messageHandlerRetryRate();
    int messageHandlerMaxRetry();
    String topicUsernameFormat();
    String topicPasswordFormat();
}
