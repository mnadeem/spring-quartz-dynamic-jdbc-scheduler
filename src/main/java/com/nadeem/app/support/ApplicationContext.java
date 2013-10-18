package com.nadeem.app.support;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public final class ApplicationContext
{

    private static ClassPathXmlApplicationContext context = null;

    public static final String MAIN_CONTEXT_FILE_LOCATION = "com/nadeem/app/config/boot-app-context.xml";

    private static final String[] CONFIG_LOCATIONS = new String[] {MAIN_CONTEXT_FILE_LOCATION};

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
            context.close();
            context = null;
        }
    }

    private static void initializeContextAsNeeded()
    {
        if (context == null)
        {
            context = new ClassPathXmlApplicationContext(CONFIG_LOCATIONS);
        }
    }
}