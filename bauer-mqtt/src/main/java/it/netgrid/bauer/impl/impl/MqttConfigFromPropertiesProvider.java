package it.netgrid.bauer.impl.impl;

import java.util.Properties;

import it.netgrid.bauer.TopicFactory;
import it.netgrid.bauer.impl.MqttConfig;
import it.netgrid.bauer.impl.MqttConfigProvider;

public class MqttConfigFromPropertiesProvider implements MqttConfigProvider {

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

        private MqttConfig config;

        @Override
        public MqttConfig get() {
                return this.get(TopicFactory.getProperties());
        }

        public MqttConfig get(Properties p) {
                if (config != null) {
                        String clientId = p.getProperty(MQTT_CLIENT_ID_PROP, MQTT_CLIENT_ID_DEFAULT);
                        String contentType = p.getProperty(MQTT_MESSAGE_CONTENT_TYPE_PROP,
                                        MQTT_MESSAGE_CONTENT_TYPE_DEFAULT);
                        String url = p.getProperty(MQTT_URL_PROP, MQTT_URL_DEFAULT);
                        String user = p.getProperty(MQTT_USER_PROP, MQTT_USER_DEFAULT);
                        String pass = p.getProperty(MQTT_PASS_PROP, MQTT_PASS_DEFAULT);
                        String reconnMinDelay = p.getProperty(MQTT_RECONN_MIN_DELAY_PROP,
                                        MQTT_RECONN_MIN_DELAY_DEFAULT);
                        String reconnMaxDelay = p.getProperty(MQTT_RECONN_MAX_DELAY_PROP,
                                        MQTT_RECONN_MAX_DELAY_DEFAULT);
                        String cleanStart = p.getProperty(MQTT_CLEAN_START_PROP, MQTT_CLEAN_START_DEFAULT);
                        String connectionTimeout = p.getProperty(MQTT_CONN_TIMEOUT_PROP, MQTT_CONN_TIMEOUT_DEFAULT);
                        String keepAliveInterval = p.getProperty(MQTT_KEEP_ALIVE_INTERVAL_PROP,
                                        MQTT_KEEP_ALIVE_INTERVAL_DEFAULT);
                        this.config = new MqttConfig(clientId, contentType, url, user, pass,
                                        Integer.parseInt(reconnMinDelay),
                                        Integer.parseInt(reconnMaxDelay), Boolean.parseBoolean(cleanStart),
                                        Integer.parseInt(connectionTimeout), Integer.parseInt(keepAliveInterval));
                }

                return config;
        }

}
