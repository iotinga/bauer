package it.netgrid.bauer.impl;

public record FfmqConfig(String providerUrl, int messageHandlerRetryRate, int messageHandlerMaxRetry, String topicUsernameFormat, String topicPasswordFormat) {}
