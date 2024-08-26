package it.netgrid.bauer.impl.impl;

import javax.net.ssl.SSLSocketFactory;

import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;

import com.fasterxml.jackson.annotation.JsonProperty;

import it.netgrid.bauer.impl.MqttConfig;
import it.netgrid.bauer.impl.MqttMessageFactory;

public class MqttConfigImpl implements MqttConfig {

    @JsonProperty(MqttConfig.MQTT_CLIENT_ID)
    String clientId;
    @JsonProperty(MqttConfig.MQTT_MESSAGE_CONTENT_TYPE)
    String messageContentType;
    @JsonProperty(MqttConfig.MQTT_URL)
    String url;
    @JsonProperty(MqttConfig.MQTT_USER)
    String user;
    @JsonProperty(MqttConfig.MQTT_PASS)
    String password;
    @JsonProperty(MqttConfig.MQTT_RECONN_MIN_DELAY)
    int reconnectMinDelay;
    @JsonProperty(MqttConfig.MQTT_RECONN_MAX_DELAY)
    int reconnectMaxDelay;
    @JsonProperty(MqttConfig.MQTT_CLEAN_START)
    boolean isCleanStart;
    @JsonProperty(MqttConfig.MQTT_CONN_TIMEOUT)
    int connectionTimeout;
    @JsonProperty(MqttConfig.MQTT_KEEP_ALIVE_INTERVAL)
    int keepAliveInterval;

    public MqttConfigImpl(String clientId, String messageContentType, String url, String user, String password,
            int reconnectMinDelay, int reconnectMaxDelay, boolean isCleanStart, int connectionTimeout,
            int keepAliveInterval) {
        this.clientId = clientId;
        this.messageContentType = messageContentType;
        this.url = url;
        this.user = user;
        this.password = password;
        this.reconnectMinDelay = reconnectMinDelay;
        this.reconnectMaxDelay = reconnectMaxDelay;
        this.isCleanStart = isCleanStart;
        this.connectionTimeout = connectionTimeout;
        this.keepAliveInterval = keepAliveInterval;
    }

    @Override
    public String clientId() {
        return this.clientId;
    }

    @Override
    public String messageContentType() {
        return this.messageContentType;
    }

    @Override
    public String url() {
        return this.url;
    }

    @Override
    public String user() {
        return this.user;
    }

    @Override
    public String password() {
        return this.password;
    }

    @Override
    public int reconnectMinDelay() {
        return this.reconnectMinDelay;
    }

    @Override
    public int reconnectMaxDelay() {
        return this.reconnectMaxDelay;
    }

    @Override
    public boolean isCleanStart() {
        return this.isCleanStart;
    }

    @Override
    public int connectionTimeout() {
        return this.connectionTimeout;
    }

    @Override
    public int keepAliveInterval() {
        return this.keepAliveInterval;
    }

    public MqttConnectionOptions asConnectionOptions() {
        MqttConnectionOptions retval = new MqttConnectionOptions();
        retval.setAutomaticReconnect(true);

        retval.setAutomaticReconnectDelay(this.reconnectMinDelay(), this.reconnectMaxDelay());
        retval.setCleanStart(this.isCleanStart());
        retval.setConnectionTimeout(this.connectionTimeout());
        retval.setKeepAliveInterval(this.keepAliveInterval());
        String username = this.user();
        byte[] password = this.password().getBytes();
        if (username.length() > 0 && password.length > 0) {
            retval.setUserName(username);
            retval.setPassword(password);
        }
        if (this.url().startsWith("ssl")) {
            retval.setSocketFactory(SSLSocketFactory.getDefault());
        }
        return retval;
    }

    public MqttMessageFactory getMessageFactory() {
        if (this.messageContentType() == JSONMqttMessageFactory.MQTT_MESSAGE_CONTENT_TYPE) {
            return new JSONMqttMessageFactory();
        } else {
            return new CBORMqttMessageFactory();
        }
    }
}
