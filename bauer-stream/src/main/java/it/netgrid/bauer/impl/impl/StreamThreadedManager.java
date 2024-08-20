package it.netgrid.bauer.impl.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORParser;

import it.netgrid.bauer.impl.StreamConfig;
import it.netgrid.bauer.impl.StreamMessageConsumer;
import it.netgrid.bauer.impl.StreamManager;
import it.netgrid.bauer.impl.StreamsProvider;

public class StreamThreadedManager implements StreamManager {

    private static final Logger log = LoggerFactory.getLogger(StreamThreadedManager.class);

    private final List<StreamMessageConsumer> consumers;
    private final StreamsProvider streams;
    private final ExecutorService executor;
    private final ObjectMapper om;
    private final CBORFactory cf;
    private final StreamConfig config;

    private Future<Integer> parser;

    public StreamThreadedManager(StreamConfig config, StreamsProvider provider) {
        this.config = config;
        this.cf = new CBORFactory();
        this.om = new ObjectMapper(this.cf);
        this.executor = Executors.newFixedThreadPool(2);
        this.consumers = new ArrayList<>();
        this.streams = provider;
    }

    @Override
    public synchronized void addMessageConsumer(StreamMessageConsumer consumer) {
        this.consumers.add(consumer);

        if (this.consumers.size() == 1) {
            this.start();
        }
    }

    @Override
    public synchronized void postMessage(JsonNode message) {
        if (message != null)
            this.executor.submit(() -> {
                byte[] payload = message.toString().getBytes();
                try {
                    this.streams.output().write(payload);
                    this.streams.output().flush();
                } catch (IOException e) {
                    log.warn("unable to post: %s");
                }
            });
    }

    public synchronized void start() {
        if (this.parser != null || !this.parser.isDone()) {
            return;
        }

        this.parser = this.executor.submit(() -> this.runCborMessageFetch());
    }

    public synchronized void trigger(JsonNode message) {
        if (message != null) {
            for (StreamMessageConsumer consumer : this.consumers) {
                consumer.consume(message);
            }
            if (this.config.isMessageBubblingEnabled()) {
                this.postMessage(message);
                log.debug(String.format("bubbling"));
            }
        }
    }

    public Integer runCborMessageFetch() {
        try {
            CBORParser parser = this.cf.createParser(this.streams.input());

            // as available() returned value behaviour depends on stream implementation,
            // calling available on a closed stream throws an IOException.
            // available() != -1 is supposed to be always true in any return value behaviour
            while (this.streams.input().available() != -1) {
                if (parser.nextToken() != null) {
                    try {
                        JsonNode message = this.om.readTree(parser);
                        this.trigger(message);
                    } catch (JsonParseException e) {
                        log.warn(String.format("parsing failed: %s", e.getMessage()));
                    }
                }
            }
        } catch (IOException e) {
            log.warn(String.format("read failed: %s", e.getMessage()));
        }
        return 0;
    }

}
