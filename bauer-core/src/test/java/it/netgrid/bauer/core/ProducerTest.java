package it.netgrid.bauer.core;

import java.util.Hashtable;

import javax.jms.DeliveryMode;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.junit.Test;

import net.timewalker.ffmq3.FFMQConstants;

public class ProducerTest implements ExceptionListener {
	
	@Test
	public void testProducerConnection() {
		// Create and initialize a JNDI context
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, FFMQConstants.JNDI_CONTEXT_FACTORY);
		env.put(Context.PROVIDER_URL, "tcp://localhost:10002");
		Context context;
		try {
			context = new InitialContext(env);

			// Lookup a connection factory in the context
			TopicConnectionFactory connFactory = (TopicConnectionFactory)context.lookup(FFMQConstants.JNDI_TOPIC_CONNECTION_FACTORY_NAME);

			// Obtain a JMS connection from the factory
			TopicConnection conn = connFactory.createTopicConnection("admin","admin");
			conn.setExceptionListener(this);
			conn.start();
			
			TopicSession session = conn.createTopicSession(true, Session.AUTO_ACKNOWLEDGE);
			Topic topic = (Topic) session.createTopic("TEST_persistent");
			
			MessageProducer producer = session.createProducer(topic);
			producer.setDeliveryMode(DeliveryMode.PERSISTENT);
			producer.setTimeToLive(5000);
			
			boolean run = true;
			int count = 0;
			while(run) {
				TextMessage message = session.createTextMessage(String.format("Hello %d", ++count));
				producer.send(message);
				session.commit();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					run = false;
				}
			}
			
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onException(JMSException arg0) {
		return;
	}
}
