package it.netgrid.bauer.helpers;

import it.netgrid.bauer.EventHandler;
import it.netgrid.bauer.Topic;

public class NOPTopic<E> implements Topic<E> {

	@Override
	public String getName() {
		return "NOP";
	}

	@Override
	public void addHandler(EventHandler<E> handler) {
		// NOP
	}

	@Override
	public void post(E event) {
		// NOP
	}

}
