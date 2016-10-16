package it.netgrid.bauer;

public interface EventHandler<E> {
	
	public String getName();
	
	public Class<E> getEventClass();

	public boolean handle(E event) throws Exception;
	
}
