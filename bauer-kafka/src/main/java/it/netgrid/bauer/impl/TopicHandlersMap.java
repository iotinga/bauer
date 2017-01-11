package it.netgrid.bauer.impl;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.netgrid.bauer.EventHandler;

public class TopicHandlersMap extends HashMap<String, Class<? extends EventHandler<?>>> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2255394631150699731L;
	public static final String GLUE = ",";
	private static final Logger log = LoggerFactory.getLogger(TopicHandlersMap.class);
	
	public void set(String topics, String handlers) {
		String[] tVals = topics.split(GLUE);
		String[] hVals = handlers.split(GLUE);
		
		for(int i = 0; i < tVals.length; i++) {
			if(i < hVals.length) {
				try {
					String handlerClassName = hVals[i];
					@SuppressWarnings("unchecked")
					Class<? extends EventHandler<?>> handler = (Class<? extends EventHandler<?>>) Class.forName(handlerClassName);
					this.put(tVals[i], handler);
				} catch (ClassNotFoundException e) {
					log.warn(String.format("Handler for topic %s skipped: %s", tVals[i], e.getMessage()));
				}
			} else {
				log.warn("Handler for topic " + tVals[i] + " not provided");
			}
		}
	}
}
