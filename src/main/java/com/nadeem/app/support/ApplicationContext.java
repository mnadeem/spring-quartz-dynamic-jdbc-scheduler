package com.nadeem.app.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public final class ApplicationContext
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationContext.class);

    private static ClassPathXmlApplicationContext context = null;

    public static final String MAIN_CONTEXT_FILE    = "com/nadeem/app/config/boot-app-context.xml";
    private static final String[] CONFIG_LOCATIONS  = new String[] {MAIN_CONTEXT_FILE};

    private ApplicationContext()
    {

    }

    @SuppressWarnings("unchecked")
    public static <T> T getBean(final String beanName)
    {
        initializeContextAsNeeded();
        return (T) context.getBean(beanName);
    }

    public static void start()
    {
        initializeContextAsNeeded();
    }

    /** Shutdown the context(s) when done. Closes connections, destroys pools, etc. */
    public static void shutdown()
    {
        if (context != null)
        {
            LOGGER.debug("Destroying application context...");
            context.close();
            context = null;
            LOGGER.debug("Application context Destroyed.");
        }
    }

    private static void initializeContextAsNeeded()
    {
        if (context == null)
        {
            LOGGER.debug("Initializing Application Context.");
            context = new ClassPathXmlApplicationContext(CONFIG_LOCATIONS);
            LOGGER.debug("Application Context Inilized");
        }
    }
}
