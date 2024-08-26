package it.netgrid.bauer.impl;

import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;

import it.netgrid.bauer.impl.impl.CBORMqttMessageFactory;

public interface MqttConfig {

    public static final String MQTT_CLIENT_ID = "mqtt_client_id";
    public static final String MQTT_MESSAGE_CONTENT_TYPE = "mqtt_content_type";
    public static final String MQTT_URL = "mqtt_broker_url";
    public static final String MQTT_USER = "mqtt_username";
    public static final String MQTT_PASS = "mqtt_password";
    public static final String MQTT_RECONN_MIN_DELAY = "mqtt_reconn_min_delay";
    public static final String MQTT_RECONN_MAX_DELAY = "mqtt_reconn_max_delay";
    public static final String MQTT_CLEAN_START = "mqtt_clean_start";
    public static final String MQTT_CONN_TIMEOUT = "mqtt_conn_timeout";
    public static final String MQTT_KEEP_ALIVE_INTERVAL = "mqtt_keep_alive_interval";

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

    String clientId();

    String messageContentType();

    String url();

    String user();

    String password();

    int reconnectMinDelay();

    int reconnectMaxDelay();

    boolean isCleanStart();

    int connectionTimeout();

    int keepAliveInterval();

    MqttConnectionOptions asConnectionOptions();

    MqttMessageFactory getMessageFactory();

}
