package it.netgrid.bauer.impl;

import java.util.Properties;

import javax.net.ssl.SSLSocketFactory;

import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttSecurityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.netgrid.bauer.ITopicFactory;
import it.netgrid.bauer.TopicFactory;
import it.netgrid.bauer.TopicFactoyBinder;
import it.netgrid.bauer.impl.impl.CBORMqttMessageFactory;
import it.netgrid.bauer.impl.impl.JSONMqttMessageFactory;
import it.netgrid.bauer.impl.impl.ThreadedMqttClientManager;

public class StaticTopicBinder implements TopicFactoyBinder {

    private static final Logger log = LoggerFactory.getLogger(StaticTopicBinder.class);

    public static final String MQTT_CLIENT_ID_PROP = "mqtt_client_id";
    public static final String MQTT_MESSAGE_CONTENT_TYPE_PROP = "mqtt_content_type";
    public static final String MQTT_URL_PROP = "mqtt_broker_url";
    public static final String MQTT_USER_PROP = "mqtt_username";
    public static final String MQTT_PASS_PROP = "mqtt_password";
    public static final String MQTT_RECONN_MIN_DELAY_PROP = "mqtt_reconn_min_delay";
    public static final String MQTT_RECONN_MAX_DELAY_PROP = "mqtt_reconn_max_delay";
    public static final String MQTT_CLEAN_START_PROP = "mqtt_clean_start";
    public static final String MQTT_CONN_TIMEOUT_PROP = "mqtt_conn_timeout";
    public static final String MQTT_KEEP_ALIVE_INTERVAL_PROP = "mqtt_keep_alive_interval";

    public static final String MQTT_CLIENT_ID_DEFAULT = "bauer";
    public static final String MQTT_MESSAGE_CONTENT_TYPE_DEFAULT = CBORMqttMessageFactory.MQTT_MESSAGE_CONTENT_TYPE;
    public static final String MQTT_URL_DEFAULT = "tcp://localhost:1883";
    public static final String MQTT_USER_DEFAULT = "";
    public static final String MQTT_PASS_DEFAULT = "";
    public static final String MQTT_RECONN_MIN_DELAY_DEFAULT = "1";
    public static final String MQTT_RECONN_MAX_DELAY_DEFAULT = "10";
    public static final String MQTT_CLEAN_START_DEFAULT = "1";
    public static final String MQTT_CONN_TIMEOUT_DEFAULT = "15";
    public static final String MQTT_KEEP_ALIVE_INTERVAL_DEFAULT = "10";

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

    /**
     * The ILoggerFactory instance returned by the {@link #getLoggerFactory}
     * method should always be the same object
     */
    private ITopicFactory topicFactory;

    private StaticTopicBinder() {
        Properties p = TopicFactory.getProperties();
        try {
            // Build client and manager for mqtt connection handling
            MqttClient client = new MqttClient(p.getProperty(MQTT_URL_PROP, MQTT_URL_DEFAULT),
                    p.getProperty(MQTT_CLIENT_ID_PROP, MQTT_CLIENT_ID_DEFAULT));

            MqttClientManager mqttClientManager = new ThreadedMqttClientManager(client);
            client.setCallback(mqttClientManager);

            // Build topic factory
            MqttMessageFactory messageFactory = StaticTopicBinder.getMessageFactory(p);
            topicFactory = new MqttTopicFactory(mqttClientManager, messageFactory);

            // Open MQTT connection
            MqttConnectionOptions options = StaticTopicBinder.buildConnectionOptions(p);
            client.connect(options);
        } catch (MqttSecurityException e) {
            log.error("Security error: %s", e.getMessage());
        } catch (MqttException e) {
            log.error("Unable to init MQTT Client: %s", e.getMessage());
        }
    }

    public ITopicFactory getTopicFactory() {
        return topicFactory;
    }

    public String getTopicFactoryClassStr() {
        return topicFactoryClassStr;
    }

    public static MqttMessageFactory getMessageFactory(Properties p) {
        String contentType = p.getProperty(MQTT_MESSAGE_CONTENT_TYPE_PROP, MQTT_MESSAGE_CONTENT_TYPE_DEFAULT);
        if(contentType == JSONMqttMessageFactory.MQTT_MESSAGE_CONTENT_TYPE) {
            return new JSONMqttMessageFactory();
        } else {
            return new CBORMqttMessageFactory();
        }
    }

    public static MqttConnectionOptions buildConnectionOptions(Properties p) {
        String uri = p.getProperty(MQTT_URL_PROP, MQTT_URL_DEFAULT);
        MqttConnectionOptions retval = new MqttConnectionOptions();
        retval.setAutomaticReconnect(true);
        retval.setAutomaticReconnectDelay(
                Integer.parseInt(p.getProperty(MQTT_RECONN_MIN_DELAY_PROP, MQTT_RECONN_MIN_DELAY_DEFAULT)),
                Integer.parseInt(p.getProperty(MQTT_RECONN_MAX_DELAY_PROP, MQTT_RECONN_MAX_DELAY_DEFAULT)));
        retval.setCleanStart(Boolean.parseBoolean(p.getProperty(MQTT_CLEAN_START_PROP, MQTT_CLEAN_START_DEFAULT)));
        retval.setConnectionTimeout(
                Integer.parseInt(p.getProperty(MQTT_CONN_TIMEOUT_PROP, MQTT_CONN_TIMEOUT_DEFAULT)));
        retval.setKeepAliveInterval(
                Integer.parseInt(p.getProperty(MQTT_KEEP_ALIVE_INTERVAL_PROP, MQTT_KEEP_ALIVE_INTERVAL_DEFAULT)));
        String username = p.getProperty(MQTT_USER_PROP, MQTT_USER_DEFAULT);
        byte[] password = p.getProperty(MQTT_PASS_PROP, MQTT_PASS_DEFAULT).getBytes();
        if (username.length() > 0 && password.length > 0) {
            retval.setUserName(username);
            retval.setPassword(password);
        }
        if(uri.startsWith("ssl")) {
            retval.setSocketFactory(SSLSocketFactory.getDefault());
        }
        return retval;
    }

}
