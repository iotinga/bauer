package it.netgrid.bauer.impl;

import java.util.Properties;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import it.netgrid.bauer.ITopicFactory;
import it.netgrid.bauer.impl.impl.MqttConfigFromPropertiesProvider;
import it.netgrid.bauer.impl.impl.ThreadedMqttClientManager;

public class MqttTopicFactoryModule extends AbstractModule {

    private Properties p;

    public MqttTopicFactoryModule(Properties properties) {
        this.p = properties;
    }

    @Override
    protected void configure() {
        bind(Properties.class).toInstance(this.p);
        bind(ITopicFactory.class).to(MqttTopicFactory.class).in(Singleton.class);
        bind(MqttConfigProvider.class).to(MqttConfigFromPropertiesProvider.class);
        bind(MqttClientManager.class).to(ThreadedMqttClientManager.class).in(Singleton.class);
    }

    @Provides
    public MqttConfig buildStreamConfig(MqttConfigProvider config) {
        return config.config();
    }

    @Provides
    public MqttMessageFactory buildMessageFactory(MqttConfigProvider config) {
        return config.config().getMessageFactory();
    }

    @Provides
    @Singleton
    public MqttClient buildMqttClient(MqttConfigProvider config) throws MqttException {
        return new MqttClient(config.config().url(), config.config().clientId());
    }
}
