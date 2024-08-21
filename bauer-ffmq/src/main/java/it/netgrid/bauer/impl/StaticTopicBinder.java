package it.netgrid.bauer.impl;

import com.google.inject.Module;

import java.util.Properties;

import it.netgrid.bauer.ITopicFactory;
import it.netgrid.bauer.TopicFactory;
import it.netgrid.bauer.TopicFactoyBinder;

public class StaticTopicBinder implements TopicFactoyBinder {
	
	private static final StaticTopicBinder SINGLETON = new StaticTopicBinder();

    /**
     * Return the singleton of this class.
     * 
     * @return the StaticLoggerBinder singleton
     */
    public static final StaticTopicBinder getSingleton() {
        return SINGLETON;
    }

    /**
     * Declare the version of the BAUER API this implementation is compiled against. 
     * The value of this field is modified with each major release. 
     */
    // to avoid constant folding by the compiler, this field must *not* be final
    public static String REQUESTED_API_VERSION = "1.0"; // !final

    private static final String topicFactoryClassStr = FfmqTopicFactory.class.getName();
 
    /**
     * The ILoggerFactory instance returned by the {@link #getLoggerFactory}
     * method should always be the same object
     */
    private final ITopicFactory topicFactory;

    private StaticTopicBinder() {
        FfmqConfigFromPropertiesProvider provider = new FfmqConfigFromPropertiesProvider(TopicFactory.getProperties());
    	topicFactory = new FfmqTopicFactory(provider);
    }

    public ITopicFactory getTopicFactory() {
        return topicFactory;
    }

    public String getTopicFactoryClassStr() {
        return topicFactoryClassStr;
    }

    @Override
    public Module getTopicFactoryAsModule(Properties properties) {
        return new FfmqTopicFactoryModule(properties);
    }
}
