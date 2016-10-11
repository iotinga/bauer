package it.netgrid.bauer;

public interface Topic<E> {

	public void addHandler(EventHandler<E> handler);
	
	public void post(E event);
	
}
