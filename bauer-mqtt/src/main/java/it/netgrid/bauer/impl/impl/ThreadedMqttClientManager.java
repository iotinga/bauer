package it.netgrid.bauer.impl.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.function.Consumer;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.MqttPersistenceException;
import org.eclipse.paho.mqttv5.common.MqttSecurityException;
import org.eclipse.paho.mqttv5.common.MqttSubscription;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.netgrid.bauer.impl.MqttClientManager;
import it.netgrid.bauer.impl.MqttMessageConsumer;

public class ThreadedMqttClientManager implements MqttClientManager, Runnable {

    private static final Logger log = LoggerFactory.getLogger(ThreadedMqttClientManager.class);

    private static final ThreadFactory THREAD_FACTORY = new ThreadFactory() {
        private int count = 1;

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(String.format("%d#%s", count++, r));
            return thread;
        }

    };

    private final LinkedBlockingQueue<MqttSubscription> pendingSubscriptions;
    private final List<MqttSubscription> activeSubscriptions;

    private final MqttClient client;
    private final ExecutorService executor;
    private final Map<MqttMessageConsumer, Future<?>> consumers = new HashMap<>();

    private Future<?> subscribeTask;

    private Future<?> connectTask;

    public ThreadedMqttClientManager(MqttClient client) {
        this.client = client;
        this.activeSubscriptions = new ArrayList<>();
        this.pendingSubscriptions = new LinkedBlockingQueue<>();
        this.executor = Executors.newThreadPerTaskExecutor(ThreadedMqttClientManager.THREAD_FACTORY);
    }

    public void publish(String topic, MqttMessage message) throws IOException {
        try {
            this.client.publish(topic, message);
        } catch (MqttPersistenceException e) {
            log.warn("Unable to persist: %s - %s", topic, message.toDebugString());
            throw new IOException(e);
        } catch (MqttException e) {
            log.info("Unable to publish: %s - %s", topic, message.toDebugString());
            throw new IOException(e);
        }
    }

    @Override
    public void addConsumer(MqttMessageConsumer consumer) throws IOException {
        if (this.consumers.containsKey(consumer))
            return;
        this.consumers.put(consumer, null);
        Future<?> future = this.executor.submit(consumer);
        this.consumers.put(consumer, future);
        try {
            this.pendingSubscriptions.put(consumer.getMqttSubscription());
        } catch (InterruptedException e) {
            log.error("Subscription failed %s", consumer.toString());
        }
    }

    @Override
    public void disconnected(MqttDisconnectResponse disconnectResponse) {
        if (this.subscribeTask != null) {
            this.subscribeTask.cancel(true);
            this.subscribeTask = null;
        }
        log.warn("MQTT-Connection: LOST", disconnectResponse);
    }

    @Override
    public void mqttErrorOccurred(MqttException exception) {
        log.error("MQTT-Error: %s", exception.toString(), exception);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        this.consumers.keySet().forEach(new Consumer<MqttMessageConsumer>() {

            @Override
            public void accept(MqttMessageConsumer consumer) {
                try {
                    consumer.consume(topic, message);
                } catch (IOException e) {
                    log.error("Unable to consume from %s by %s", topic, consumer);
                }
            }

        });
    }

    @Override
    public void deliveryComplete(IMqttToken token) {
        log.debug("MQTT-Delivered: %s", token);
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        log.info("MQTT-Connection: %s to %s", reconnect ? "RECONNECTED" : "CONNECTED", serverURI);
        this.subscribeTask = this.executor.submit(this);
    }

    @Override
    public void authPacketArrived(int reasonCode, MqttProperties properties) {
        String message = "UNKNOWN";
        switch (reasonCode) {
            case 0:
                message = "AUTHENTICATED";
                break;
            case 24:
                message = "AUTH-CONTINUE";
                break;
            case 25:
                message = "RE-AUTH";
                break;
        }
        log.info("MQTT-Connection: %s", message);
    }

    @Override
    public void run() {
        boolean restored = false;
        while (!Thread.currentThread().isInterrupted() && this.activeSubscriptions.size() > 0 && !restored) {
            try {
                int activeCount = this.activeSubscriptions.size();
                MqttSubscription[] subscriptions = new MqttSubscription[activeCount];
                for (int i = 0; i < activeCount; i++) {
                    subscriptions[i] = this.activeSubscriptions.get(i);
                }
                ;
                this.client.subscribe(subscriptions);
                restored = true;
                break;
            } catch (MqttException e) {
                log.warn(String.format("Unable to restore subscriptions. Retry in seconds..."));
            }
            try {
                long sleep = (long) (1000 + (Math.random() * 4000));
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                log.info("Shutting down...");
            }
        }
        while (!Thread.currentThread().isInterrupted()) {
            this.runSubscribeOnce();
        }
    }

    public void runSubscribeOnce() {
        MqttSubscription subscription = null;
        try {
            subscription = pendingSubscriptions.take();
            MqttSubscription[] subscriptions = new MqttSubscription[1];
            subscriptions[0] = subscription;
            this.client.subscribe(subscriptions);
            this.activeSubscriptions.add(subscription);
            subscription = null;
        } catch (InterruptedException e) {
            log.warn(String.format(e.getMessage()));
        } catch (MqttException e) {
            log.warn(String.format("Unable to subscribe to %s: %s", subscription.getTopic(), e.getMessage()));
        } finally {
            if (subscription != null) {
                this.pendingSubscriptions.add(subscription);
            }
        }
    }

    public int pendingSubscriptions() {
        return this.pendingSubscriptions.size();
    }

    @Override
    public boolean connectCompleted() {
        return this.connectTask.isDone();
    }

    @Override
    public void connect(final MqttConnectionOptions options) throws IOException {
        this.connectTask = this.executor.submit(new Runnable() {

            @Override
            public void run() {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        client.connect(options);
                        break;
                    } catch (MqttSecurityException e) {
                        log.error("Security error: %s", e.getMessage());
                    } catch (MqttException e) {
                        log.warn(String.format("Unable to connect. Retry in seconds..."));
                    }
                    try {
                        long sleep = (long) (options.getAutomaticReconnectMinDelay() * 1000
                                + (Math.random() * options.getAutomaticReconnectMaxDelay() * 1000));
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        log.info("Shutting down...");
                    }
                }
            }

        });
    }

}
