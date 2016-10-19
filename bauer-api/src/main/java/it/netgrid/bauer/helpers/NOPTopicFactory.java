package it.netgrid.bauer.helpers;

import java.util.HashMap;
import java.util.Map;

import it.netgrid.bauer.ITopicFactory;
import it.netgrid.bauer.Topic;

public class NOPTopicFactory implements ITopicFactory {
    
    final Map<String, NOPTopic<?>> topics = new HashMap<String, NOPTopic<?>>();

	@Override
	public <E> Topic<E> getTopic(String name) {
    	@SuppressWarnings("unchecked")
    	NOPTopic<E> topic = (NOPTopic<E>)topics.get(name);
    	if(topic == null) {
    		topic = new NOPTopic<E>();
    		topics.put(name, topic);
    	}
    	
    	return (Topic<E>) topic;
	}

}
