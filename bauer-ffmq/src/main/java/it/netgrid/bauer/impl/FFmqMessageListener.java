package it.netgrid.bauer.impl;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.netgrid.bauer.EventHandler;

public class FFmqMessageListener<E> implements MessageListener {

	private static final Logger log = LoggerFactory.getLogger(FFmqMessageListener.class);
	private final EventHandler<E> handler;
	private final FfmqTopicFactory factory;
	private final String topic;

	public FFmqMessageListener(FfmqTopicFactory factory, String topic, EventHandler<E> handler) {
		this.factory = factory;
		this.handler = handler;
		this.topic = topic;
	}

	@Override
	public void onMessage(Message message) {
		TextMessage tm = (TextMessage) message;
		E event = null;
		try {
			event = this.factory.getEvent(tm, handler.getEventClass());
		} catch (JMSException e) {
			log.error("Can not fetch text from message");
			try {
				message.acknowledge();
			} catch (JMSException e1) {
				log.error("Can not ACK message");
			}
		}

		if (event == null) {
			try {
				message.acknowledge();
			} catch (JMSException e1) {
				log.error("Can not ACK message");
			}
			return;
		}

		int tryCount = 0;
		boolean done = false;
		while (!done && tryCount < this.factory.getHandlerMaxRetry()) {
			done = this.handle(this.topic, event);
			if (done) {
				try {
					message.acknowledge();
				} catch (JMSException e1) {
					log.error("Can not ACK message");
				}
			} else {
				tryCount++;
				try {
					Thread.sleep(this.factory.getHandlerRetryRate());
				} catch (InterruptedException e) {
					log.info("Sleep interrupted");
				}
			}
		}

		if (done) {
			log.info(String.format("%s handled message", this.handler.getName()));
		} else {
			log.error(String.format("%s max retries reached", this.handler.getName()));
		}
	}

	synchronized private boolean handle(String topic, E event) {
		try {
			return handler.handle(topic, event);
		} catch (Exception e) {
			log.warn(e.getMessage(), e);
			return false;
		}
	}
}
