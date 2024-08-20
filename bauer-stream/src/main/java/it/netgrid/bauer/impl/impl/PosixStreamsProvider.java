package it.netgrid.bauer.impl.impl;

import java.io.InputStream;
import java.io.OutputStream;

import it.netgrid.bauer.impl.StreamsProvider;

public class PosixStreamsProvider implements StreamsProvider {

    @Override
    public synchronized InputStream input() {
        return System.in;
    }

    @Override
    public synchronized OutputStream output() {
        return System.out;
    }
    
}
