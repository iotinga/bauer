package it.netgrid.bauer.impl.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.netgrid.bauer.impl.StreamsProvider;

public class InMemoryStreamsProvider implements StreamsProvider {

    private static final Logger log = LoggerFactory.getLogger(InMemoryStreamsProvider.class);

    private final PipedInputStream input;
    private final PipedOutputStream output;

    public InMemoryStreamsProvider() {
        this.output = new PipedOutputStream();
        PipedInputStream inputStream = null;
        try {
            inputStream = new PipedInputStream(this.output);
        } catch (IOException e) {
            log.error(String.format("broken pipe: %s", e.getMessage()));
        }

        this.input = inputStream == null ? new PipedInputStream() : inputStream;
    }

    @Override
    public InputStream input() {
        return this.input;
    }

    @Override
    public OutputStream output() {
        return this.output;
    }
    
}
