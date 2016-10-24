package it.netgrid.bauer.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import it.netgrid.bauer.ITopicFactory;
import it.netgrid.bauer.Topic;

public class SubstituteTopicFactory implements ITopicFactory {

    boolean postInitialization = false;
    
    final Map<String, SubstituteTopic<?>> topics = new HashMap<String, SubstituteTopic<?>>();
    final LinkedBlockingQueue<SubstituteTopicEvent> eventQueue = new LinkedBlockingQueue<SubstituteTopicEvent>();

    @Override
    synchronized public <E> Topic<E> getTopic(String name) {
    	@SuppressWarnings("unchecked")
		SubstituteTopic<E> topic = (SubstituteTopic<E>)topics.get(name);
    	if(topic == null) {
    		topic = new SubstituteTopic<E>(name, eventQueue, postInitialization);
    		topics.put(name, topic);
    	}
    	
    	return (Topic<E>) topic;
    }
    
    public LinkedBlockingQueue<SubstituteTopicEvent> getEventQueue() {
        return eventQueue;
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
