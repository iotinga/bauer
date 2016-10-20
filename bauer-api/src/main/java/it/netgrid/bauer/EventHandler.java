package it.netgrid.bauer;

public interface EventHandler<E> {
	
	public String getName();
	
	public Class<E> getEventClass();

	/**
	 * Handle return value as:
	 * - FALSE: retry (a temporary error is occurred)
	 * - TRUE: go ahead (success or permanent error is occurred)
	 * @param event
	 * @return
	 * @throws Exception
	 */
	public boolean handle(E event) throws Exception;
	
}
