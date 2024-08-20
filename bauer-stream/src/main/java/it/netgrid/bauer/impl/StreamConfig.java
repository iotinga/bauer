package it.netgrid.bauer.impl;

public record StreamConfig (String topicAttribute, String payloadAttribute, Boolean isMessageBubblingEnabled) {}
