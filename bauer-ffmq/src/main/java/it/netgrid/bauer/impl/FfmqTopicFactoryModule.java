package it.netgrid.bauer.impl;

import java.util.Properties;

import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import it.netgrid.bauer.ITopicFactory;

public class FfmqTopicFactoryModule extends AbstractModule {
    private Properties p;

    public FfmqTopicFactoryModule(Properties properties) {
        this.p = properties;
    }

    @Override
    protected void configure() {
        bind(Properties.class).toInstance(this.p);
        bind(ITopicFactory.class).to(FfmqTopicFactory.class).in(Singleton.class);
        bind(FfmqConfigProvider.class).to(FfmqConfigFromPropertiesProvider.class);
    }
}
