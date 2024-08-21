package it.netgrid.bauer;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

public class BauerModule extends AbstractModule {
    
    public BauerModule() {}

    @Override 
    protected void configure() {}

    @Provides
    @Singleton
    public ITopicFactory buildTopicFactory() {
        return TopicFactory.getITopicFactory();
    }
}
