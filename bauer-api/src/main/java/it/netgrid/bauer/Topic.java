package it.netgrid.bauer;

public interface Topic<E> {
	
	public String getName();

	public void addHandler(EventHandler<E> handler);

	public void removeHandler(EventHandler<E> handler);
	
	public void post(E event);
}
