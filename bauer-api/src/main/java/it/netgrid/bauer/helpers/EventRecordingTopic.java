package it.netgrid.bauer.helpers;

import java.util.Queue;

import it.netgrid.bauer.EventHandler;
import it.netgrid.bauer.Topic;
import it.netgrid.bauer.helpers.SubstituteTopicEvent.Action;

public class EventRecordingTopic<E> implements Topic<E> {
	
    String name;
    SubstituteTopic<E> topic;
    Queue<SubstituteTopicEvent> eventQueue;

    public EventRecordingTopic(SubstituteTopic<E> topic, Queue<SubstituteTopicEvent> eventQueue) {
        this.topic = topic;
        this.name = topic.getName();
        this.eventQueue = eventQueue;
    }

    public String getName() {
        return name;
    }

	@Override
	public void addHandler(EventHandler<E> handler) {
		SubstituteTopicEvent qEvent = new SubstituteTopicEvent(topic, Action.ADD_HANDLER, null, handler);
		this.eventQueue.add(qEvent);
	}

	@Override
	public void post(E event) {
		SubstituteTopicEvent qEvent = new SubstituteTopicEvent(topic, Action.POST, event, null);
		this.eventQueue.add(qEvent);
	}
	
}
