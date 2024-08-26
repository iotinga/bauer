package it.netgrid.bauer.impl;

import java.io.IOException;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Module;

import java.util.Properties;

import it.netgrid.bauer.ITopicFactory;
import it.netgrid.bauer.TopicFactory;
import it.netgrid.bauer.TopicFactoyBinder;
import it.netgrid.bauer.impl.impl.MqttConfigFromPropertiesProvider;
import it.netgrid.bauer.impl.impl.ThreadedMqttClientManager;

public class StaticTopicBinder implements TopicFactoyBinder {

    private static final Logger log = LoggerFactory.getLogger(StaticTopicBinder.class);

    private static final StaticTopicBinder SINGLETON = new StaticTopicBinder();

    /**
     * Return the singleton of this class.
     * 
     * @return the StaticLoggerBinder singleton
     */
    public static final StaticTopicBinder getSingleton() {
        return SINGLETON;
    }

    /**
     * Declare the version of the BAUER API this implementation is compiled against.
     * The value of this field is modified with each major release.
     */
    // to avoid constant folding by the compiler, this field must *not* be final
    public static String REQUESTED_API_VERSION = "1.0"; // !final

    private static final String topicFactoryClassStr = MqttTopicFactory.class.getName();

    private static final MqttConfigProvider cp = new MqttConfigFromPropertiesProvider(TopicFactory.getProperties());

    /**
     * The ILoggerFactory instance returned by the {@link #getLoggerFactory}
     * method should always be the same object
     */
    private ITopicFactory topicFactory;

    private StaticTopicBinder() {
        try {
            // Build client and manager for mqtt connection handling
            MqttClient client = new MqttClient(cp.config().url(), cp.config().clientId());
            MqttClientManager mqttClientManager = new ThreadedMqttClientManager(client);

            // Build topic factory
            MqttMessageFactory messageFactory = cp.config().getMessageFactory();
            topicFactory = new MqttTopicFactory(mqttClientManager, messageFactory);

            // Open MQTT connection
            mqttClientManager.connect(cp.config().asConnectionOptions());
        } catch (MqttException e) {
            log.error(String.format("Unable to init MQTT Client: %s", e.getMessage()));
        } catch (IOException e) {
            log.error(String.format("Unable to connect: %s", e.getMessage()));
        }
    }

    public ITopicFactory getTopicFactory() {
        return topicFactory;
    }

    public String getTopicFactoryClassStr() {
        return topicFactoryClassStr;
    }

    @Override
    public Module getTopicFactoryAsModule(Properties properties) {
        return new MqttTopicFactoryModule(properties);
    }

}
