package it.netgrid.bauer.impl;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.netgrid.bauer.EventHandler;
import it.netgrid.bauer.Topic;

public class KafkaTopic<E> implements Topic<E> {
	
	private static final Logger log = LoggerFactory.getLogger(KafkaTopic.class);
	
	private final String name;
	private final KafkaTopicFactory factory;
	private final Map<String, ?> subscribers;
	
	public KafkaTopic(String name, KafkaTopicFactory factory) {
		this.name = name;
		this.factory = factory;
		this.subscribers = new HashMap<String, Object>();
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void addHandler(EventHandler<E> handler) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void post(E event) {
		if(event == null) return;
		this.factory.eventTrigger(this.name, event);
	}

}
