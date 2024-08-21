package it.netgrid.bauer.impl;

import com.google.inject.Module;

import java.util.Properties;

import it.netgrid.bauer.ITopicFactory;
import it.netgrid.bauer.TopicFactory;
import it.netgrid.bauer.TopicFactoyBinder;
import it.netgrid.bauer.impl.impl.PosixStreamsProvider;
import it.netgrid.bauer.impl.impl.SimpleStreamMessageFactory;
import it.netgrid.bauer.impl.impl.StreamConfigFromPropertiesProvider;
import it.netgrid.bauer.impl.impl.StreamThreadedManager;

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

    private static final String topicFactoryClassStr = StreamTopicFactory.class.getName();

    private static final StreamConfigProvider config = new StreamConfigFromPropertiesProvider(TopicFactory.getProperties());


    /**
     * The ILoggerFactory instance returned by the {@link #getLoggerFactory}
     * method should always be the same object
     */
    private final ITopicFactory topicFactory;

    private StaticTopicBinder() {
        StreamManager manager = new StreamThreadedManager(config.get(), new PosixStreamsProvider());
    	StreamMessageFactory factory = new SimpleStreamMessageFactory(config.get());
        topicFactory = new StreamTopicFactory(manager, factory);
    }

    public ITopicFactory getTopicFactory() {
        return topicFactory;
    }

    public String getTopicFactoryClassStr() {
        return topicFactoryClassStr;
    }

    @Override
    public Module getTopicFactoryAsModule(Properties properties) {
        return new StreamTopicFactoryModule(properties);
    }
}
