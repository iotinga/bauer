package it.netgrid.bauer.helpers;

import java.util.ArrayList;
import java.util.List;

import it.netgrid.bauer.EventHandler;
import it.netgrid.bauer.Topic;

public class SubstituteTopic<E> implements Topic<E> {
	
	private final List<EventHandler<E>> handlers = new ArrayList<>();
	private final String username;
	private final String password;
	private final String name;
	
	public SubstituteTopic(String name) {
		this.name = name;
		this.username = null;
		this.password = null;
	}
	
	public SubstituteTopic(String name, String username, String password) {
		this.name = name;
		this.username = username;
		this.password = password;
	}

	@Override
	synchronized public void addHandler(EventHandler<E> handler) {
		handlers.add(handler);
	}

	@Override
	synchronized public void post(E event) {
		for(EventHandler<E> handler : handlers) {
			try {
				handler.handle(event);
			} catch (Exception e) {
				// NOTHING TO DO
			}
		}
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

}
