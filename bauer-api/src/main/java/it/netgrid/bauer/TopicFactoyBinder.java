package it.netgrid.bauer;

import java.util.Properties;
import com.google.inject.Module;

public interface TopicFactoyBinder {

    public Module getTopicFactoryAsModule(Properties properties);
	
	public ITopicFactory getTopicFactory();

    public String getTopicFactoryClassStr();
    
}
