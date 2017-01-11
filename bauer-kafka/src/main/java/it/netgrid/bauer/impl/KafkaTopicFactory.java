package it.netgrid.bauer.impl;

import java.io.StringWriter;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.netgrid.bauer.ITopicFactory;
import it.netgrid.bauer.Topic;
import it.netgrid.bauer.TopicFactory;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

public class KafkaTopicFactory implements ITopicFactory {
	
	private static final Logger log = LoggerFactory.getLogger(KafkaTopicFactory.class);
	
	private static final String DEBUG_PROPERTY_KEY = "producer.debug";
	private static final String DEFAULT_DEBUG_PROPERTY = "1";
	
	private ProducerConfig producerConfig;
	private Producer<String, String> producer;
	
	private static Gson gson;
	
	static {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setFieldNamingStrategy(new GsonNamingStrategy()); 
		gson = gsonBuilder.create();
	}

	@Override
	public <E> Topic<E> getTopic(String name) {
		return new KafkaTopic<E>(name, this);
	}
	
	public Producer<String, String> getProducer() {
		if(producer == null) {
			producer = new Producer<String, String>(getProducerConfig());
		}
		
		return producer;
	}
	
	public void eventTrigger(String topic, Object payload) {
		String serializedPayload = gson.toJson(payload);

		if(isDebug()) {
			log.info(String.format("%s#%s", topic, serializedPayload));
		} else {
			try {
				KeyedMessage<String, String> message = new KeyedMessage<String, String>(topic, serializedPayload);
				getProducer().send(message);
			} catch (kafka.common.FailedToSendMessageException e) {
				log.error(String.format("Not sent %s %s", topic, serializedPayload));
			}
		}
	}

	public ProducerConfig getProducerConfig() {
		if(producerConfig == null) {
			Properties props = TopicFactory.getProperties();
			producerConfig = new ProducerConfig(props);
		}
		
		return producerConfig;
	}
	
	public boolean isDebug() {
		return Integer.parseInt(TopicFactory.getProperties().getProperty(DEBUG_PROPERTY_KEY, DEFAULT_DEBUG_PROPERTY)) == 1;
	}

}
