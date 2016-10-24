package it.netgrid.bauer.helpers;

import it.netgrid.bauer.EventHandler;

public class SubstituteTopicEvent {

	public enum Action {
		POST,
		ADD_HANDLER
	}
	
	private final SubstituteTopic<?> topic;
	private final Action action;
	private final Object event;
	private final EventHandler<?> handler;
	private final Class<?> eventClass;
	
	public SubstituteTopicEvent(SubstituteTopic<?> topic, Action action, Object event, EventHandler<?> handler) {
		this.topic = topic;
		this.action = action;
		this.event = event;
		this.handler = handler;
		this.eventClass = (event != null) ? event.getClass() : null;
	}

	public Object getEvent() {
		return event;
	}

	public EventHandler<?> getHandler() {
		return handler;
	}

	public Action getAction() {
		return action;
	}

	public SubstituteTopic<?> getTopic() {
		return topic;
	}
	
	public Class<?> getEventClass() {
		return this.eventClass;
	}
}
