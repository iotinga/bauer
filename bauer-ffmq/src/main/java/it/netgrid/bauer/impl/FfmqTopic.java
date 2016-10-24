package it.netgrid.bauer.impl;

import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.TextMessage;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.netgrid.bauer.EventHandler;
import it.netgrid.bauer.Topic;

public class FfmqTopic<E> implements Topic<E> {
	
	private static final Logger log = LoggerFactory.getLogger(FfmqTopic.class);
	
	private final String name;
	private final FfmqTopicFactory factory;
	
	private final Map<String, TopicSubscriber> subscribers;
	private MessageProducer producer;
	private TopicSession producerSession;
	
	public FfmqTopic(String name, FfmqTopicFactory factory) {
		this.name = name;
		this.factory = factory;
		this.subscribers = new HashMap<String, TopicSubscriber>();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void addHandler(EventHandler<E> handler) {
		if( ! subscribers.containsKey(handler.getName())) {
			TopicSubscriber subscriber = this.getSubscriber(handler.getName());
			if(subscriber == null) {
				log.error(String.format("%s: %s Unable to add handler", this.name, handler.getName()));
				return;
			}
			try {
				subscriber.setMessageListener(this.factory.<E>buildMessageListener(handler));
			} catch (JMSException e) {
				log.error(String.format("%s: %s Unable to add handler", this.name, handler.getName()));
			}
		} else {
			log.debug(String.format("%s: %s already registered", this.name, handler.getName()));
		}
	}

	@Override
	public void post(E event) {
		if(event == null) return;
		
		TextMessage message = this.factory.buildMessage(this.getProducerSession(), event);
		
		if(message == null) return;
		
		this.post(message);
	}
	
	private void post(TextMessage message) {
		try {
			this.getProducer().send(message);
			this.getProducerSession().commit();
		} catch (JMSException e) {
			try {
				log.warn(String.format("%s:%s", this.name, message.getText()));
			} catch (JMSException e1) {
				log.error(String.format("%s:???", this.name));
			}
		}
	}

	private TopicSubscriber getSubscriber(String subscriberName) {
		if( ! this.subscribers.containsKey(subscriberName)) {
			TopicSession session = this.factory.buildTopicConsumerSession(this.name, subscriberName);
			TopicSubscriber subscriber = this.factory.getTopicSubscriber(session, this.name, subscriberName);
			this.subscribers.put(subscriberName, subscriber);
			return subscriber;
		}
		
		return this.subscribers.get(subscriberName);
	}
	
	private TopicSession getProducerSession() {
		if(this.producerSession == null) {
			this.producerSession = this.factory.buildTopicProducerSession(this.name);
		}
		
		return this.producerSession;
	}
	
	private MessageProducer getProducer() {
		if(this.producer == null) {
			this.producer = this.factory.getMessageProducer(this.getProducerSession(), this.name);
		}
		
		return this.producer;
	}
}
