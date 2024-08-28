package it.netgrid.bauer.impl;

import java.util.Properties;

import com.google.inject.Inject;

import it.netgrid.bauer.impl.impl.FfmqConfigImpl;

public class FfmqConfigFromPropertiesProvider implements FfmqConfigProvider {

    private FfmqConfig config;
    private final Properties p;

    @Inject
    public FfmqConfigFromPropertiesProvider(Properties properties) {
        this.p = properties;
    }

    @Override
    public FfmqConfig config() {
        if (config == null) {
            String providerUrl = p.getProperty(FfmqConfig.FFMQ_PROVIDER_URL, FfmqConfig.FFMQ_PROVIDER_URL_DEFAULT);
            String retryRate = p.getProperty(FfmqConfig.FFMQ_MESSAGE_HANDLER_RETRY_RATE, FfmqConfig.FFMQ_MESSAGE_HANDLER_RETRY_RATE_DEFAULT);
            String maxRetry = p.getProperty(FfmqConfig.FFMQ_MESSAGE_HANDLER_MAX_RETRY, FfmqConfig.FFMQ_MESSAGE_HANDLER_MAX_RETRY_DEFAULT);
            String topicUsernameFormat = p.getProperty(FfmqConfig.FFMQ_TOPIC_USERNAME_NAME_FORMAT, FfmqConfig.FFMQ_TOPIC_USERNAME_NAME_FORMAT_DEFAULT);
            String topicPasswordFormat = p.getProperty(FfmqConfig.FFMQ_TOPIC_PASSWORD_NAME_FORMAT, FfmqConfig.FFMQ_TOPIC_PASSWORD_NAME_FORMAT_DEFAULT);
            this.config = new FfmqConfigImpl(providerUrl, Integer.parseInt(retryRate), Integer.parseInt(maxRetry),
            topicUsernameFormat, topicPasswordFormat);
        }

        return config;
    }
}
