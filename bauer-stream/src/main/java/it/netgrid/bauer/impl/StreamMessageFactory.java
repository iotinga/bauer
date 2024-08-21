package it.netgrid.bauer.impl;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;

public interface StreamMessageFactory {
    public StreamEvent<JsonNode> buildEvent(JsonNode message) throws IOException;
    public <E> StreamEvent<E> buildEvent(JsonNode message, Class<E> eventClass) throws IOException;
    public <E> JsonNode buildMessage(StreamEvent<E> event) throws IOException;
}
