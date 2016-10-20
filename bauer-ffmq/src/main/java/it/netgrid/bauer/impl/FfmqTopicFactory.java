package it.netgrid.bauer.impl;

import java.util.Hashtable;

import javax.jms.JMSException;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.netgrid.bauer.EventHandler;
import it.netgrid.bauer.ITopicFactory;
import it.netgrid.bauer.Topic;
import it.netgrid.bauer.TopicFactory;
import net.timewalker.ffmq3.FFMQConstants;

public class FfmqTopicFactory implements ITopicFactory {
	
	private static final Logger log = LoggerFactory.getLogger(FfmqTopicFactory.class);
	
	private static Gson gson;
	
	static {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.setFieldNamingStrategy(new GsonNamingStrategy()); 
		gson = gsonBuilder.create();
	}
	
	public static final String PROVIDER_URL_PROP = "ffmqProvider";
	public static final String MESSAGE_HANDLER_RETRY_RATE_PROP = "messageHandlerRetry";
	public static final String MESSAGE_HANDLER_MAX_RETRY_PROP = "messageHandlerMaxRetry";
	
	public static final String DEFAULT_PROVIDER_URL = "tcp://localhost:10002";
	public static final String DEFAULT_MESSAGE_HANDLER_RETRY_RATE = "2000";
	public static final String DEFAULT_MESSAGE_HANDLER_MAX_RETRY = "10";
	
	private static final String TOPIC_USERNAME_NAME_FORMAT = "topic.%s.username";
	private static final String TOPIC_PASSWORD_NAME_FORMAT = "topic.%s.password";
	
	private Context context;
	private TopicConnectionFactory connFactory;
	

	@Override
	public <E> Topic<E> getTopic(String name) {
		return new FfmqTopic<E>(name, this);
	}

	private Context getContext() {
		if(context == null) {
			// Create and initialize a JNDI context
			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, FFMQConstants.JNDI_CONTEXT_FACTORY);
			env.put(Context.PROVIDER_URL, TopicFactory.getProperties().getProperty(FfmqTopicFactory.PROVIDER_URL_PROP, DEFAULT_PROVIDER_URL));
	
			try {
				context = new InitialContext(env);
			} catch (NamingException e) {
				log.error(e.getMessage(), e);
				context = null;
			}
		}
		
		return context;
		
	}
	
	private TopicConnectionFactory getConnFactory() {
		if(this.connFactory == null) {
			try {
				connFactory = (TopicConnectionFactory)this.getContext().lookup(FFMQConstants.JNDI_TOPIC_CONNECTION_FACTORY_NAME);
			} catch (NamingException e) {
				log.error(e.getMessage(), e);
				connFactory = null;
			}
		}
		
		return this.connFactory;
	}
	
	public MessageProducer getMessageProducer(TopicSession session, String topicName) {
		try {
			javax.jms.Topic topic = session.createTopic(topicName);
			return session.createProducer(topic);
		} catch (JMSException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}
	
	public TopicSubscriber getTopicSubscriber(TopicSession session, String topicName, String subscriberName) {
		try {
			javax.jms.Topic topic = session.createTopic(topicName);
			return session.createDurableSubscriber(topic, subscriberName);
		} catch (JMSException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}
	
	public TopicSession buildTopicConsumerSession(String topicName, String subscriberName) {
		TopicConnection conn;
		try {
			conn = this.initTopicConsumerConnection(topicName, subscriberName);
			return conn.createTopicSession(false, Session.CLIENT_ACKNOWLEDGE);
		} catch (JMSException e) {
			log.error(e.getMessage(), e);
			return null;
		}
	}
	
	public TopicSession buildTopicProducerSession(String topicName) {
		TopicConnection conn;
		try {
			conn = this.initTopicProducerConnection(topicName);
			return conn.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		} catch (JMSException e) {
			log.error(e.getMessage(), e);
			return null;
		}		
	}
	
	private TopicConnection initTopicProducerConnection(String topicName) throws JMSException {
		TopicConnection retval;
		if(this.needsCredentials(topicName)) {
			retval = this.getConnFactory().createTopicConnection(this.getUsername(topicName), this.getPassword(topicName));
		} else {
			retval = this.getConnFactory().createTopicConnection();
		}
		return retval;
	}
	
	private TopicConnection initTopicConsumerConnection(String topicName, String clientID) throws JMSException {
		TopicConnection retval;
		if(this.needsCredentials(topicName)) {
			retval = this.getConnFactory().createTopicConnection(this.getUsername(topicName), this.getPassword(topicName));
		} else {
			retval = this.getConnFactory().createTopicConnection();
		}
		retval.setClientID(clientID);
		retval.start();
		return retval;
	}
	
	private boolean needsCredentials(String topicName) {
		return this.getUsername(topicName) != null &&
			this.getPassword(topicName) != null;
		
	}
	
	public int getHandlerMaxRetry() {
		return Integer.parseInt(TopicFactory.getProperties().getProperty(MESSAGE_HANDLER_MAX_RETRY_PROP, DEFAULT_MESSAGE_HANDLER_MAX_RETRY));
	}
	
	public int getHandlerRetryRate() {
		return Integer.parseInt(TopicFactory.getProperties().getProperty(MESSAGE_HANDLER_RETRY_RATE_PROP, DEFAULT_MESSAGE_HANDLER_RETRY_RATE));
	}
	
	private String getUsername(String topicName) {
		return TopicFactory.getProperties().getProperty(this.getUsernamePropertyName(topicName));
	}
	
	private String getPassword(String topicName) { 
		return TopicFactory.getProperties().getProperty(this.getPasswordPropertyName(topicName));
	}
	
	private String getUsernamePropertyName(String topicName) {
		return String.format(TOPIC_USERNAME_NAME_FORMAT, topicName);
	}
	
	private String getPasswordPropertyName(String topicName) {
		return String.format(TOPIC_PASSWORD_NAME_FORMAT, topicName);
	}
	
	public <E> E getEvent(TextMessage message, Class<E> eventClass) throws JMSException {
		if (message == null) return null;
		String json = message.getText();
		if (json == null || json.trim().equals("")) return null;
		try {
			return gson.fromJson(json, eventClass);
		} catch (Exception e) {
			log.error(String.format("%s: can not parse event", json));
			try {
				message.acknowledge();
			} catch (JMSException e1) {
				log.error("Can not ACK message");
			}
			return null;
		}
	}
	
	public TextMessage buildMessage(Session session, Object payload) {
		String json = gson.toJson(payload);
		try {
			return session.createTextMessage(json);
		} catch (JMSException e) {
			log.error(String.format("Unable to create message: %s", json));
		}
		
		return null;
	}
	
	public <E> MessageListener buildMessageListener(final EventHandler<E> handler) {
		return new FFmqMessageListener<E>(this, handler);
	}
}
