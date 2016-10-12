package it.netgrid.bauer;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

import it.netgrid.bauer.helpers.NOPTopicFactory;
import it.netgrid.bauer.helpers.SubstituteTopicFactory;
import it.netgrid.bauer.helpers.Util;
import it.netgrid.bauer.impl.StaticTopicBinder;

public final class TopicFactory {
	
    static final int UNINITIALIZED = 0;
    static final int ONGOING_INITIALIZATION = 1;
    static final int FAILED_INITIALIZATION = 2;
    static final int SUCCESSFUL_INITIALIZATION = 3;
    static final int NOP_FALLBACK_INITIALIZATION = 4;

    static volatile int INITIALIZATION_STATE = UNINITIALIZED;
    static SubstituteTopicFactory SUBST_FACTORY = new SubstituteTopicFactory();
    static NOPTopicFactory NOP_FALLBACK_FACTORY = new NOPTopicFactory();

    // Support for detecting mismatched logger names.
    static final String DETECT_TOPIC_NAME_MISMATCH_PROPERTY = "slf4j.detectTopicNameMismatch";
    static final String JAVA_VENDOR_PROPERTY = "java.vendor.url";

    static boolean DETECT_TOPIC_NAME_MISMATCH = Util.safeGetBooleanSystemProperty(DETECT_TOPIC_NAME_MISMATCH_PROPERTY);

    static private final String[] API_COMPATIBILITY_LIST = new String[] { "1.0" };

	private TopicFactory() {}

    static void reset() {
        INITIALIZATION_STATE = UNINITIALIZED;
    }

    private final static void performInitialization() {
        bind();
        if (INITIALIZATION_STATE == SUCCESSFUL_INITIALIZATION) {
            versionSanityCheck();
        }
    }
    
    private static boolean messageContainsOrgSlf4jImplStaticTopicBinder(String msg) {
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
            // release all resources in SUBST_FACTORY
            SUBST_FACTORY.clear();
        } catch (NoClassDefFoundError ncde) {
            String msg = ncde.getMessage();
            if (messageContainsOrgSlf4jImplStaticTopicBinder(msg)) {
                INITIALIZATION_STATE = NOP_FALLBACK_INITIALIZATION;
                Util.report("Failed to load class \"it.netgrid.bauer.impl.StaticTopicBinder\".");
                Util.report("Defaulting to no-operation (NOP) logger implementation");
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
            // given our large user base and SLF4J's commitment to backward
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
            Util.report("Actual binding is of type [" + StaticTopicBinder.getSingleton().getTopicFactoryClassStr() + "]");
        }
    }
	
    public static <E> Topic<E> getTopic(String name) {
        ITopicFactory iTopicFactory = getITopicFactory();
        return iTopicFactory.getTopic(name);
    }	
	
    public static <E> Topic<E> getTopic(Class<E> clazz) {
        Topic<E> logger = getTopic(clazz.getName());
        return logger;
    }
	
    /**
     * Return the {@link ITopicFactory} instance in use.
     * <p/>
     * <p/>
     * ITopicFactory instance is bound with this class at compile time.
     * 
     * @return the ITopicFactory instance in use
     */
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
            // See also http://jira.qos.ch/browse/SLF4J-97
            return SUBST_FACTORY;
        }
        throw new IllegalStateException("Unreachable code");
    }
}
