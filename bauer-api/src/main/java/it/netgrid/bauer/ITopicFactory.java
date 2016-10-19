package it.netgrid.bauer;

public interface ITopicFactory {

	public <E> Topic<E> getTopic(String name);
	
}
