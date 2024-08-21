package it.netgrid.bauer.impl;

import com.google.inject.Module;

import java.util.Properties;

import it.netgrid.bauer.ITopicFactory;
import it.netgrid.bauer.TopicFactoyBinder;
import it.netgrid.bauer.helpers.SubstituteTopicFactory;

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

    // to avoid constant folding by the compiler, this field must *not* be final
    public static String REQUESTED_API_VERSION = "1.0"; // !final

    private static final String topicFactoryClassStr = SubstituteTopicFactory.class.getName();

    /**
     * The ILoggerFactory instance returned by the {@link #getLoggerFactory}
     * method should always be the same object
     */
    private final ITopicFactory topicFactory;

    private StaticTopicBinder() {
        throw new UnsupportedOperationException("This code should have never made it into bauer-api.jar");    }

    public ITopicFactory getTopicFactory() {
        throw new UnsupportedOperationException("This code should never make it into bauer-api.jar");    }

    public String getTopicFactoryClassStr() {
        throw new UnsupportedOperationException("This code should never make it into bauer-api.jar");    }

    @Override
    public Module getTopicFactoryAsModule(Properties properties) {
        throw new UnsupportedOperationException("Unimplemented method 'getTopicFactoryAsModule'");
    }
}
