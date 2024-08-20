package it.netgrid.bauer.impl;

import java.io.InputStream;
import java.io.OutputStream;

public interface StreamsProvider {

    public InputStream input();
    public OutputStream output();
    
}
