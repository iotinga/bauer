package it.netgrid.bauer;

import it.netgrid.bauer.impl.StreamTopicFactoryModule;

public class StreamModule extends StreamTopicFactoryModule {
    
    public StreamModule() {
        super(TopicFactory.getProperties());
    }
}
