package it.netgrid.bauer.impl;

import java.util.Properties;

import com.google.inject.Inject;

public class FfmqConfigFromPropertiesProvider implements FfmqConfigProvider {

    public static final String PROVIDER_URL_PROP = "ffmqProvider";
    public static final String MESSAGE_HANDLER_RETRY_RATE_PROP = "messageHandlerRetry";
    public static final String MESSAGE_HANDLER_MAX_RETRY_PROP = "messageHandlerMaxRetry";

    public static final String DEFAULT_PROVIDER_URL = "tcp://localhost:10002";
    public static final String DEFAULT_MESSAGE_HANDLER_RETRY_RATE = "2000";
    public static final String DEFAULT_MESSAGE_HANDLER_MAX_RETRY = "10";

    private static final String TOPIC_USERNAME_NAME_FORMAT = "topic.%s.username";
    private static final String TOPIC_PASSWORD_NAME_FORMAT = "topic.%s.password";

    private FfmqConfig config;
    private final Properties p;

    @Inject
    public FfmqConfigFromPropertiesProvider(Properties properties) {
        this.p = properties;
    }

    @Override
    public FfmqConfig get() {
        if (config != null) {
            String providerUrl = p.getProperty(PROVIDER_URL_PROP, DEFAULT_PROVIDER_URL);
            String retryRate = p.getProperty(MESSAGE_HANDLER_RETRY_RATE_PROP, DEFAULT_MESSAGE_HANDLER_RETRY_RATE);
            String maxRetry = p.getProperty(MESSAGE_HANDLER_MAX_RETRY_PROP, DEFAULT_MESSAGE_HANDLER_MAX_RETRY);
            this.config = new FfmqConfig(providerUrl, Integer.parseInt(retryRate), Integer.parseInt(maxRetry),
                    TOPIC_USERNAME_NAME_FORMAT, TOPIC_PASSWORD_NAME_FORMAT);
        }

        return config;
    }
}
