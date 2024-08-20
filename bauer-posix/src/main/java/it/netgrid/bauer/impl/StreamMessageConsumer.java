package it.netgrid.bauer.impl;

import com.fasterxml.jackson.databind.JsonNode;

public interface StreamMessageConsumer {
    public boolean consume(JsonNode message);
}
