package it.netgrid.bauer.helpers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Queue;

import it.netgrid.bauer.EventHandler;
import it.netgrid.bauer.Topic;
import it.netgrid.bauer.TopicFactory;

public class SubstituteTopic<E> implements Topic<E> {
	
	private final NOPTopic<E> NOP_TOPIC;
	private final String name;
    private volatile Topic<E> _delegate;
    private Queue<SubstituteTopicEvent> eventQueue;
    private Boolean delegateEventAware;
    private Method postMethodCache;
    private Method addHandlerMethodCache;
    private Class<EventHandler<E>> handlerClass;

    private EventRecordingTopic<E> eventRecordingTopic;
    private final boolean createdPostInitialization;
	
	public SubstituteTopic(String name, Queue<SubstituteTopicEvent> eventQueue, boolean createdPostInitialization) {
		this.NOP_TOPIC = new NOPTopic<E>();
		this.name = name;
        this.eventQueue = eventQueue;
        this.createdPostInitialization = createdPostInitialization;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addHandler(EventHandler<E> handler) {
		// If handler is null we cannot fetch the class types
		// and "null" is better to be discarded as handler
		if(handler != null) {
			this.handlerClass = (Class<EventHandler<E>>)handler.getClass();
			delegate().addHandler(handler);
		}
	}

    @Override
    public synchronized void removeHandler(EventHandler<E> handler) {
        delegate().removeHandler(handler);
    }

	@Override
	public void post(E event) {
		// If event is null we cannot fetch the data class type
		// and "null" is better to be discarded as event payload
		if(event != null) {
			delegate().post(event);
		}
	}

	@Override
	public String getName() {
		return name;
	}
	
	@SuppressWarnings("unchecked")
	public void replayPost(Object event) {
        if (isDelegateEventAware()) {
            try {
                postMethodCache.invoke(_delegate, (E)event);
            } catch (IllegalAccessException e) {
            } catch (IllegalArgumentException e) {
            } catch (InvocationTargetException e) {
            }
        }
	}
	
	@SuppressWarnings("unchecked")
	public void replayAddHandler(EventHandler<?> handler) {
        if (isDelegateEventAware()) {
            try {
                addHandlerMethodCache.invoke(_delegate, (EventHandler<E>)handler);
            } catch (IllegalAccessException e) {
            } catch (IllegalArgumentException e) {
            } catch (InvocationTargetException e) {
            }
        }
	}
	
	/**
     * Return the delegate logger instance if set. Otherwise, return a {@link NOPLogger}
     * instance.
     */
	Topic<E> delegate() {
        if(_delegate != null) {
            return _delegate;
        }
        if(createdPostInitialization) {
            return NOP_TOPIC;
        } else {
            return getEventRecordingTopic();
        }
    }
	
	private Topic<E> getEventRecordingTopic() {
        if (eventRecordingTopic == null) {
        	eventRecordingTopic = new EventRecordingTopic<E>(this, eventQueue);
        }
        return eventRecordingTopic;
    }
	
	public void updateDelegate() {
		Topic<E> topic = TopicFactory.getTopic(this.getName());
		this._delegate = topic;
	}
	
    public void setDelegate(Topic<E> delegate) {
        this._delegate = delegate;
    }
    
    public boolean isDelegateNull() {
        return _delegate == null;
    }

    public boolean isDelegateNOP() {
        return _delegate instanceof NOPTopic<?>;
    }
    
    public boolean isDelegateEventAware() {
        if (delegateEventAware != null)
            return delegateEventAware;
        
        try {
        	postMethodCache = _delegate.getClass().getMethod("post", Object.class);
        	
        	if(this.handlerClass != null) {
        		addHandlerMethodCache = _delegate.getClass().getMethod("addHandler", this.handlerClass);
        	}
            
        	delegateEventAware = this.postMethodCache != null || this.addHandlerMethodCache != null;
            
        } catch (NoSuchMethodException e) {
            delegateEventAware = Boolean.FALSE;
        }

        return delegateEventAware;
    }
}
