package it.netgrid.bauer;

import java.util.Properties;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Module;

import it.netgrid.bauer.helpers.NOPModule;
import it.netgrid.bauer.helpers.NOPTopicFactory;
import it.netgrid.bauer.helpers.SubstituteTopic;
import it.netgrid.bauer.helpers.SubstituteTopicEvent;
import it.netgrid.bauer.helpers.SubstituteTopicFactory;
import it.netgrid.bauer.helpers.Util;

import it.netgrid.bauer.impl.StaticTopicBinder;

/**
 * see https://github.com/qos-ch/slf4j/tree/v_1.7.21/slf4j-api
 * reference implementation
 * https://github.com/qos-ch/slf4j/tree/v_1.7.21/slf4j-api
 * SLF4J 1.7.21
 */

public final class TopicFactory {

    private static final Logger log = LoggerFactory.getLogger(TopicFactory.class);

    private static String configPropertiesPath = null;
    private static final String DEFAULT_CONFIG_PROPERTIES_NAME = "bauer.properties";
    static final int INIT_RETRIES_TIMEOUT_MILLIS = 2000;
    static final int UNINITIALIZED = 0;
    static final int ONGOING_INITIALIZATION = 1;
    static final int FAILED_INITIALIZATION = 2;
    static final int SUCCESSFUL_INITIALIZATION = 3;
    static final int NOP_FALLBACK_INITIALIZATION = 4;

    static volatile int INITIALIZATION_STATE = UNINITIALIZED;
    static SubstituteTopicFactory SUBST_FACTORY = new SubstituteTopicFactory();
    static NOPTopicFactory NOP_FALLBACK_FACTORY = new NOPTopicFactory();
    static NOPModule NOP_FALLBACK_MODULE = new NOPModule();

    // Support for detecting mismatched logger names.
    static final String DETECT_TOPIC_NAME_MISMATCH_PROPERTY = "bauer.detectTopicNameMismatch";
    static final String JAVA_VENDOR_PROPERTY = "java.vendor.url";

    static boolean DETECT_TOPIC_NAME_MISMATCH = Util.safeGetBooleanSystemProperty(DETECT_TOPIC_NAME_MISMATCH_PROPERTY);

    static private final String[] API_COMPATIBILITY_LIST = new String[] { "1.0" };

    private static Properties properties;

    private static CompletableFuture<Module> FACTORY_MODULE;

    private TopicFactory() {
    }

    static void reset() {
        INITIALIZATION_STATE = UNINITIALIZED;
    }

    public final static void setConfigPropertiesPath(String path) {
        configPropertiesPath = path;
    }

    public final static String getConfigPropertiesPath() {
        if (configPropertiesPath == null) {
            return DEFAULT_CONFIG_PROPERTIES_NAME;
        }

        return configPropertiesPath;
    }

    private final static void performInitialization() {
        bind();
        if (INITIALIZATION_STATE == SUCCESSFUL_INITIALIZATION) {
            versionSanityCheck();
        }
    }

    private static boolean messageContainsOrgBauerjImplStaticTopicBinder(String msg) {
        if (msg == null)
            return false;
        if (msg.contains("it/netgrid/bauer/impl/StaticTopicBinder"))
            return true;
        if (msg.contains("it.netgrid.bauer.impl.StaticTopicBinder"))
            return true;
        return false;
    }

    private final static void bind() {
        try {
            Set<URL> staticTopicBinderPathSet = null;
            // skip check under android, see also
            if (!isAndroid()) {
                staticTopicBinderPathSet = findPossibleStaticTopicBinderPathSet();
                reportMultipleBindingAmbiguity(staticTopicBinderPathSet);
            }
            // the next line does the binding
            StaticTopicBinder.getSingleton();
            INITIALIZATION_STATE = SUCCESSFUL_INITIALIZATION;
            reportActualBinding(staticTopicBinderPathSet);
            fixSubstituteTopics();
            replayEvents();
            // release all resources in SUBST_FACTORY
            SUBST_FACTORY.clear();
        } catch (NoClassDefFoundError ncde) {
            String msg = ncde.getMessage();
            if (messageContainsOrgBauerjImplStaticTopicBinder(msg)) {
                INITIALIZATION_STATE = NOP_FALLBACK_INITIALIZATION;
                Util.report("Failed to load class \"it.netgrid.bauer.impl.StaticTopicBinder\".");
                Util.report("Defaulting to no-operation (NOP) topic implementation");
            } else {
                failedBinding(ncde);
                throw ncde;
            }
        } catch (java.lang.NoSuchMethodError nsme) {
            String msg = nsme.getMessage();
            if (msg != null && msg.contains("it.netgrid.bauer.impl.StaticTopicBinder.getSingleton()")) {
                INITIALIZATION_STATE = FAILED_INITIALIZATION;
            }
            throw nsme;
        } catch (Exception e) {
            failedBinding(e);
            throw new IllegalStateException("Unexpected initialization failure", e);
        }
    }

    private final static void failedBinding(Throwable t) {
        TopicFactory.report("Failed to instantiate Bauer TopicFactory", t);
    }

    private final static void report(String message, Throwable t) {
        System.out.println(String.format("%s: %s", message, t.getMessage()));
    }

    private final static void versionSanityCheck() {
        try {
            String requested = StaticTopicBinder.REQUESTED_API_VERSION;

            boolean match = false;
            for (String aAPI_COMPATIBILITY_LIST : API_COMPATIBILITY_LIST) {
                if (requested.startsWith(aAPI_COMPATIBILITY_LIST)) {
                    match = true;
                }
            }
            if (!match) {
                Util.report("The requested version " + requested + " by your bauer binding is not compatible with "
                        + Arrays.asList(API_COMPATIBILITY_LIST).toString());
            }
        } catch (java.lang.NoSuchFieldError nsfe) {
            // given our large user base and BAUER's commitment to backward
            // compatibility, we cannot cry here. Only for implementations
            // which willingly declare a REQUESTED_API_VERSION field do we
            // emit compatibility warnings.
        } catch (Throwable e) {
            // we should never reach here
            Util.report("Unexpected problem occured during version sanity check", e);
        }
    }

    private static String STATIC_TOPIC_BINDER_PATH = "it/netgrid/bauer/impl/StaticTopicBinder.class";

    static Set<URL> findPossibleStaticTopicBinderPathSet() {
        // use Set instead of list in order to deal with bug #138
        // LinkedHashSet appropriate here because it preserves insertion order
        // during iteration
        Set<URL> staticTopicBinderPathSet = new LinkedHashSet<URL>();
        try {
            ClassLoader loggerFactoryClassLoader = TopicFactory.class.getClassLoader();
            Enumeration<URL> paths;
            if (loggerFactoryClassLoader == null) {
                paths = ClassLoader.getSystemResources(STATIC_TOPIC_BINDER_PATH);
            } else {
                paths = loggerFactoryClassLoader.getResources(STATIC_TOPIC_BINDER_PATH);
            }
            while (paths.hasMoreElements()) {
                URL path = paths.nextElement();
                staticTopicBinderPathSet.add(path);
            }
        } catch (IOException ioe) {
            Util.report("Error getting resources from path", ioe);
        }
        return staticTopicBinderPathSet;
    }

    private static boolean isAmbiguousStaticTopicBinderPathSet(Set<URL> binderPathSet) {
        return binderPathSet.size() > 1;
    }

    private static void reportMultipleBindingAmbiguity(Set<URL> binderPathSet) {
        if (isAmbiguousStaticTopicBinderPathSet(binderPathSet)) {
            Util.report("Class path contains multiple BAUER bindings.");
            for (URL path : binderPathSet) {
                Util.report("Found binding in [" + path + "]");
            }
        }
    }

    private static boolean isAndroid() {
        String vendor = Util.safeGetSystemProperty(JAVA_VENDOR_PROPERTY);
        if (vendor == null)
            return false;
        return vendor.toLowerCase().contains("android");
    }

    private static void reportActualBinding(Set<URL> binderPathSet) {
        // binderPathSet can be null under Android
        if (binderPathSet != null && isAmbiguousStaticTopicBinderPathSet(binderPathSet)) {
            Util.report(
                    "Actual binding is of type [" + StaticTopicBinder.getSingleton().getTopicFactoryClassStr() + "]");
        }
    }

    public static <E> Topic<E> getTopic(String name) {
        ITopicFactory iTopicFactory = getITopicFactory();
        return iTopicFactory.getTopic(name);
    }

    public static ITopicFactory getITopicFactory() {
        if (INITIALIZATION_STATE == UNINITIALIZED) {
            synchronized (TopicFactory.class) {
                if (INITIALIZATION_STATE == UNINITIALIZED) {
                    INITIALIZATION_STATE = ONGOING_INITIALIZATION;
                    performInitialization();
                }
            }
        }
        switch (INITIALIZATION_STATE) {
            case SUCCESSFUL_INITIALIZATION:
                return StaticTopicBinder.getSingleton().getTopicFactory();
            case NOP_FALLBACK_INITIALIZATION:
                return NOP_FALLBACK_FACTORY;
            case FAILED_INITIALIZATION:
                throw new IllegalStateException("Failed Bauer initialization");
            case ONGOING_INITIALIZATION:
                // support re-entrant behavior.
                return SUBST_FACTORY;
        }
        throw new IllegalStateException("Unreachable code");
    }

    public static Module getAsModule(Properties properties) {
        if (FACTORY_MODULE == null) {
            FACTORY_MODULE = new CompletableFuture<>();
            while (!FACTORY_MODULE.isDone()) {
                switch (INITIALIZATION_STATE) {
                    case UNINITIALIZED:
                        INITIALIZATION_STATE = ONGOING_INITIALIZATION;
                        performInitialization();
                        break;
                    case SUCCESSFUL_INITIALIZATION:
                        FACTORY_MODULE
                                .complete(StaticTopicBinder.getSingleton().getTopicFactoryAsModule(properties));
                    case NOP_FALLBACK_INITIALIZATION:
                        FACTORY_MODULE.complete(NOP_FALLBACK_MODULE);
                    case FAILED_INITIALIZATION:
                        FACTORY_MODULE
                                .completeExceptionally(new IllegalStateException("Failed Bauer initialization"));
                    case ONGOING_INITIALIZATION:
                        try {
                            Thread.sleep(INIT_RETRIES_TIMEOUT_MILLIS);
                        } catch (InterruptedException e) {
                            FACTORY_MODULE.completeExceptionally(e);
                        }
                }
            }
        }
        return FACTORY_MODULE.join();
    }

    private static void replayEvents() {
        final LinkedBlockingQueue<SubstituteTopicEvent> queue = SUBST_FACTORY.getEventQueue();
        final int queueSize = queue.size();
        int count = 0;
        final int maxDrain = 128;
        List<SubstituteTopicEvent> eventList = new ArrayList<SubstituteTopicEvent>(maxDrain);
        while (true) {
            int numDrained = queue.drainTo(eventList, maxDrain);
            if (numDrained == 0)
                break;
            for (SubstituteTopicEvent event : eventList) {
                replaySingleEvent(event);
                if (count++ == 0)
                    emitReplayOrSubstituionWarning(event, queueSize);
            }
            eventList.clear();
        }
    }

    private static void emitReplayOrSubstituionWarning(SubstituteTopicEvent event, int queueSize) {
        if (event.getTopic().isDelegateEventAware()) {
            emitReplayWarning(queueSize);
        } else if (event.getTopic().isDelegateNOP()) {
            // nothing to do
        } else {
            emitSubstitutionWarning();
        }
    }

    private static void emitReplayWarning(int eventCount) {
        Util.report("A number (" + eventCount
                + ") of topic calls during the initialization phase have been intercepted and are");
        Util.report("now being replayed. These are subject to the filtering rules of the underlying topics system.");
    }

    private static void emitSubstitutionWarning() {
        Util.report("The following set of substitute topic may have been accessed");
        Util.report("during the initialization phase. Topic calls during this");
        Util.report("phase were not honored. However, subsequent topic calls to these");
        Util.report("topics will work as normally expected.");
    }

    private static void replaySingleEvent(SubstituteTopicEvent event) {
        if (event == null)
            return;

        SubstituteTopic<?> substTopic = event.getTopic();
        String topicName = substTopic.getName();
        if (substTopic.isDelegateNull()) {
            throw new IllegalStateException("Delegate topic cannot be null at this state.");
        }

        if (substTopic.isDelegateNOP()) {
            // nothing to do
        } else if (substTopic.isDelegateEventAware()) {
            switch (event.getAction()) {
                case ADD_HANDLER:
                    substTopic.replayAddHandler(event.getHandler());
                    break;
                case POST:
                    substTopic.replayPost(event.getEvent());
                    break;
            }
        } else {
            Util.report(topicName);
        }
    }

    private static void fixSubstituteTopics() {
        synchronized (SUBST_FACTORY) {
            SUBST_FACTORY.postInitialization();
            for (SubstituteTopic<?> substTopic : SUBST_FACTORY.getTopics()) {
                substTopic.updateDelegate();
            }
        }

    }

    // Configuration
    public static Properties getProperties() {
        if (properties == null) {
            loadProperties();
        }

        return properties;
    }

    private static boolean loadPropertiesAsResource(String propertiesResourceName) {
        if (properties == null) {
            try (InputStream resourceStream = TopicFactory.class.getClassLoader()
                    .getResourceAsStream(propertiesResourceName);) {
                properties = new Properties();
                properties.load(resourceStream);
            } catch (NullPointerException e) {
                log.debug("Unable to load properties");
            } catch (IOException e) {
                log.debug(String.format("Unable to load config resource: %s", propertiesResourceName), e);
            }
        }
        return properties != null;
    }

    private static boolean loadPropertiesFromFile(String filePath) {
        if (properties == null) {
            FileInputStream in = null;
            try {
                in = new FileInputStream(filePath);
            } catch (FileNotFoundException e) {
                log.debug(String.format("Unable to load config file: %s", filePath), e);
            }

            if (in != null) {
                try {
                    properties = new Properties();
                    properties.load(in);
                } catch (IOException e) {
                    log.warn("Invalid properties file format", e);
                } finally {
                    try {
                        in.close();
                    } catch (IOException e) {
                        log.debug("Input stream already closed");
                    }
                }
            }
        }
        return properties != null;
    }

    private static void loadProperties() {
        if (loadPropertiesFromFile(getConfigPropertiesPath()))
            return;

        if (loadPropertiesAsResource(DEFAULT_CONFIG_PROPERTIES_NAME))
            return;

        if (properties == null) {
            log.info(String.format("No %s properties found. Run with defaults.", DEFAULT_CONFIG_PROPERTIES_NAME));
            properties = new Properties();
        }
    }
}
