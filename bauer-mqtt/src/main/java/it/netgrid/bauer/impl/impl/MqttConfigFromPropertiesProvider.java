package it.netgrid.bauer.impl.impl;

import java.util.Properties;

import com.google.inject.Inject;

import it.netgrid.bauer.impl.MqttConfig;
import it.netgrid.bauer.impl.MqttConfigProvider;

public class MqttConfigFromPropertiesProvider implements MqttConfigProvider {

    private MqttConfig config;

    private final Properties p;

    @Inject
    public MqttConfigFromPropertiesProvider(Properties properties) {
        this.p = properties;
    }

    @Override
    public MqttConfig config() {
        if (config != null) {
            String clientId = p.getProperty(MqttConfig.MQTT_CLIENT_ID,
                    MqttConfig.MQTT_CLIENT_ID_DEFAULT);
            String contentType = p.getProperty(MqttConfig.MQTT_MESSAGE_CONTENT_TYPE,
                    MqttConfig.MQTT_MESSAGE_CONTENT_TYPE_DEFAULT);
            String url = p.getProperty(MqttConfig.MQTT_URL, MqttConfig.MQTT_URL_DEFAULT);
            String user = p.getProperty(MqttConfig.MQTT_USER, MqttConfig.MQTT_USER_DEFAULT);
            String pass = p.getProperty(MqttConfig.MQTT_PASS, MqttConfig.MQTT_PASS_DEFAULT);
            String reconnMinDelay = p.getProperty(MqttConfig.MQTT_RECONN_MIN_DELAY,
                    MqttConfig.MQTT_RECONN_MIN_DELAY_DEFAULT);
            String reconnMaxDelay = p.getProperty(MqttConfig.MQTT_RECONN_MAX_DELAY,
                    MqttConfig.MQTT_RECONN_MAX_DELAY_DEFAULT);
            String cleanStart = p.getProperty(MqttConfig.MQTT_CLEAN_START,
                    MqttConfig.MQTT_CLEAN_START_DEFAULT);
            String connectionTimeout = p.getProperty(MqttConfig.MQTT_CONN_TIMEOUT,
                    MqttConfig.MQTT_CONN_TIMEOUT_DEFAULT);
            String keepAliveInterval = p.getProperty(MqttConfig.MQTT_KEEP_ALIVE_INTERVAL,
                    MqttConfig.MQTT_KEEP_ALIVE_INTERVAL_DEFAULT);
            this.config = new MqttConfigImpl(clientId, contentType, url, user, pass,
                    Integer.parseInt(reconnMinDelay),
                    Integer.parseInt(reconnMaxDelay), Boolean.parseBoolean(cleanStart),
                    Integer.parseInt(connectionTimeout), Integer.parseInt(keepAliveInterval));
        }

        return config;
    }

}
