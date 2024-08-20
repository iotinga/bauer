package it.netgrid.bauer.impl;

import com.fasterxml.jackson.databind.JsonNode;

public interface StreamManager {

    public void addMessageConsumer(StreamMessageConsumer consumer);
    public void postMessage(JsonNode message);

}
