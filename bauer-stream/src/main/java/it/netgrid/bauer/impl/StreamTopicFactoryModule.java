package it.netgrid.bauer.impl;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import java.util.Properties;

import it.netgrid.bauer.ITopicFactory;
import it.netgrid.bauer.impl.impl.PosixStreamsProvider;
import it.netgrid.bauer.impl.impl.SimpleStreamMessageFactory;
import it.netgrid.bauer.impl.impl.StreamConfigFromPropertiesProvider;
import it.netgrid.bauer.impl.impl.StreamThreadedManager;

public class StreamTopicFactoryModule extends AbstractModule {

    private Properties p;

    public StreamTopicFactoryModule(Properties properties) {
        this.p = properties;
    }

    @Override
    protected void configure() {
        bind(Properties.class).toInstance(this.p);
        bind(ITopicFactory.class).to(StreamTopicFactory.class).in(Singleton.class);
        bind(StreamManager.class).to(StreamThreadedManager.class);
        bind(StreamMessageFactory.class).to(SimpleStreamMessageFactory.class);
        bind(StreamConfigProvider.class).to(StreamConfigFromPropertiesProvider.class);
        
        // change the following bind to change streams 
        bind(StreamsProvider.class).to(PosixStreamsProvider.class);
    }

    @Provides
    public StreamConfig buildStreamConfig(StreamConfigProvider provider) {
        return provider.config();
    }
}
