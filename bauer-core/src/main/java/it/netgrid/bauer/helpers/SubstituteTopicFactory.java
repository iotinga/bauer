package it.netgrid.bauer.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.netgrid.bauer.ITopicFactory;
import it.netgrid.bauer.Topic;

public class SubstituteTopicFactory implements ITopicFactory {

	@Override
	synchronized public <E> Topic<E> getTopic(String name, String username, String password) {
    	@SuppressWarnings("unchecked")
		SubstituteTopic<E> topic = (SubstituteTopic<E>)topics.get(name);
    	if(topic == null) {
    		topic = new SubstituteTopic<E>(name, username, password);
    		topics.put(name, topic);
    	}
    	
    	return (Topic<E>) topic;
	}

    boolean postInitialization = false;
    
    final Map<String, SubstituteTopic<?>> topics = new HashMap<String, SubstituteTopic<?>>();

    @Override
    synchronized public <E> Topic<E> getTopic(String name) {
    	@SuppressWarnings("unchecked")
		SubstituteTopic<E> topic = (SubstituteTopic<E>)topics.get(name);
    	if(topic == null) {
    		topic = new SubstituteTopic<E>(name);
    		topics.put(name, topic);
    	}
    	
    	return (Topic<E>) topic;
    }

    public List<String> getTopicNames() {
        return new ArrayList<String>(topics.keySet());
    }

    public List<SubstituteTopic<?>> getTopics() {
        return new ArrayList<SubstituteTopic<?>>(topics.values());
    }
    
    public void postInitialization() {
    	postInitialization = true;
    }
    
    public void clear() {
    	topics.clear();
    }


}
