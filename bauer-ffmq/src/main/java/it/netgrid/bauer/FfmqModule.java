package it.netgrid.bauer;

import it.netgrid.bauer.impl.FfmqTopicFactoryModule;

public class FfmqModule extends FfmqTopicFactoryModule {

    public FfmqModule() {
        super(TopicFactory.getProperties());
    }

}