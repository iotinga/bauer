package it.netgrid.bauer.helpers;

import com.google.inject.AbstractModule;

import it.netgrid.bauer.ITopicFactory;

public class NOPModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ITopicFactory.class).to(NOPTopicFactory.class);
    }
}
