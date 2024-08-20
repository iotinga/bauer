package it.netgrid.bauer.impl;

import javax.net.ssl.SSLSocketFactory;

import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;

import it.netgrid.bauer.impl.impl.CBORMqttMessageFactory;
import it.netgrid.bauer.impl.impl.JSONMqttMessageFactory;

public record MqttConfig(String clientId, String messageContentType, String url, String user, String password, int reconnectMinDelay, int reconnectMaxDelay, boolean isCleanStart, int connectionTimeout, int keepAliveInterval) {

    MqttConnectionOptions asConnectionOptions() {
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

    MqttMessageFactory getMessageFactory() {
        if (this.messageContentType() == JSONMqttMessageFactory.MQTT_MESSAGE_CONTENT_TYPE) {
            return new JSONMqttMessageFactory();
        } else {
            return new CBORMqttMessageFactory();
        }
    }
}
