package it.netgrid.bauer.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Injector;

import it.netgrid.bauer.EventHandler;
import kafka.consumer.ConsumerIterator;
import kafka.consumer.KafkaStream;
import kafka.message.MessageAndMetadata;
 
public class RunnableKafkaStreamConsumer<E> implements Runnable {
    private KafkaStream<byte[], byte[]> stream;
    private int threadNumber;
    
	private static final int THREAD_SLEEP = 5000;
	
	private static final Logger log = LoggerFactory.getLogger(RunnableKafkaStreamConsumer.class);
	
	private boolean running;
	private final List<EventHandler<E>> eventHandlers;
	
	
 
	@Inject
    public RunnableKafkaStreamConsumer() {
		this.eventHandlers = new ArrayList<>();
    }
    
    public void setStream(KafkaStream<byte[], byte[]> a_stream) {
    	if(running) return;
    	this.stream = a_stream;
    }
    
    public void setThreadNumber(int a_threadNumber) {
    	if(running) return;
    	this.threadNumber = a_threadNumber;
    }
 
	public void run() {
		this.running = true;
		ConsumerIterator<byte[], byte[]> it = stream.iterator();
		while (running && it.hasNext()) {
			MessageAndMetadata<byte[], byte[]> record = it.next();
			MessageHandler handler = this.handlers.get(record.topic());
			String message = null;
			try {
				message = new String(record.message());
			} catch (Exception e) {
				log.error(e.getMessage());
			}

			// Initialize retry message consumption
			int tries = 0;
			boolean messageHandled = false;
			while (this.running && !messageHandled) {
				try {
					messageHandled = handler.consume(message);
					if (!messageHandled) {
						tries++;
						if (tries == this.config.getMessageMaxRetries()) {
							log.error(String.format("T%d: Max tries (%d) expired for message %s", threadNumber, tries, message));
							messageHandled = true;
						} else {
							Thread.sleep(THREAD_SLEEP);
						}
					}
					
				// Interruption during thread sleeping
				} catch (InterruptedException e) {
					this.running = false;

				// Catching all uncaught Handler Exceptions
				} catch (Exception e) {
					log.error(String.format("T%d: Exception: %s Message: %s", threadNumber, e.getLocalizedMessage(), message));
					messageHandled = true;
				}
			}
		}
		log.info(String.format("Shutting down Thread:%d", threadNumber));
	}
    
	public void stop() {
		this.running = false;
	}
	
	public String[] getTopics() {
		String[] retval = new String[this.handlers.keySet().size()];
		this.handlers.keySet().toArray(retval);
		return retval;
	}
	
	public void initHandlers() {
		for(String topic : handlersMap.keySet()) {
			MessageHandler handler = this.injector.getInstance(this.handlersMap.get(topic));
			handlers.put(topic, handler);
		}
	}
}
