package it.netgrid.bauer.core;

import java.util.Hashtable;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.junit.jupiter.api.Test;

import net.timewalker.ffmq4.FFMQConstants;

public class ConsumerTest {

	@Test
	public void testConsumerConnection() {
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
			conn.setClientID("test_subscriber");
			conn.start();
			TopicSession session = conn.createTopicSession(false, Session.CLIENT_ACKNOWLEDGE);
			
			Topic topic = session.createTopic("TEST_persistent");
			TopicSubscriber subscriber = session.createDurableSubscriber(topic, "test_subscriber");
			
			subscriber.setMessageListener(new MessageListener() {
				
				@Override
				public void onMessage(Message m) {
					try {
						TextMessage message = (TextMessage)m;
						System.out.println(message.getText());
						message.acknowledge();
					} catch (JMSException e) {
						// NOP
					}
					return;
				}
				
			});
			
			boolean run = true;
			while(run) {
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					run = false;
				}
			}
			return;
			
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
