package it.netgrid.bauer.impl.impl;

import com.fasterxml.jackson.annotation.JsonProperty;

import it.netgrid.bauer.impl.FfmqConfig;

public class FfmqConfigImpl implements FfmqConfig {

    @JsonProperty(FfmqConfig.FFMQ_PROVIDER_URL)
    String providerUrl;
    @JsonProperty(FfmqConfig.FFMQ_MESSAGE_HANDLER_RETRY_RATE)
    int messageHandlerRetryRate;
    @JsonProperty(FfmqConfig.FFMQ_MESSAGE_HANDLER_MAX_RETRY)
    int messageHandlerMaxRetry;
    @JsonProperty(FfmqConfig.FFMQ_TOPIC_USERNAME_NAME_FORMAT)
    String topicUsernameFormat;
    @JsonProperty(FfmqConfig.FFMQ_TOPIC_PASSWORD_NAME_FORMAT)
    String topicPasswordFormat;

    public FfmqConfigImpl(String providerUrl, int messageHandlerRetryRate, int messageHandlerMaxRetry,
            String topicUsernameFormat, String topicPasswordFormat) {
    }

    @Override
    public String providerUrl() {
        return this.providerUrl;
    }

    @Override
    public int messageHandlerRetryRate() {
        return this.messageHandlerRetryRate;
    }

    @Override
    public int messageHandlerMaxRetry() {
        return this.messageHandlerMaxRetry;
    }

    @Override
    public String topicUsernameFormat() {
        return this.topicUsernameFormat;
    }

    @Override
    public String topicPasswordFormat() {
        return this.topicPasswordFormat;
    }
}
