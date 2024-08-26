package it.netgrid.bauer;

import it.netgrid.bauer.impl.MqttTopicFactoryModule;

public class MqttModule extends MqttTopicFactoryModule {
    public MqttModule() {
        super(TopicFactory.getProperties());
    }
}
