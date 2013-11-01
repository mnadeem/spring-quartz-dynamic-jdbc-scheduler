package com.nadeem.app;

import java.util.Arrays;

import org.joda.time.Minutes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nadeem.app.scheduler.BaseDynamicScheduler;
import com.nadeem.app.scheduler.BaseDynamicScheduler.InvocationDetail;
import com.nadeem.app.support.ApplicationContext;
import com.nadeem.app.support.ThreadStaller;

public final class Application
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    private static final ThreadStaller APPLICATION_STALLER  = new ThreadStaller();
    private static final ThreadStaller SHUTDOWN_STALLER     = new ThreadStaller();

    public static void main(final String[] args)
    {
        try
        {
            ApplicationContext.start();
            doBusiness();
            waitForTermination();
        }
        catch (Exception e)
        {
            LOGGER.error("An exception occurred while starting Application {}, Got message {}", ApplicationContext.MAIN_CONTEXT_FILE, e.getMessage());
        }
        finally
        {
            ApplicationContext.shutdown();
            // Allow the shutdown thread to resume, after which the VM will terminate.
            SHUTDOWN_STALLER.unstall();
        }
    }

    private static void doBusiness()
    {
        BaseDynamicScheduler scheduler = ApplicationContext.getBean("dynamicScheduler");
        String jobName = "test";
        String group = "Dynamicgroup";
        scheduler.scheduleWithInterval(jobName, group, Minutes.ONE.toStandardDuration(), newInvocationDetail());
    }

    private static InvocationDetail newInvocationDetail()
    {
        return new InvocationDetail("targetBean", "sayHello", Arrays.asList("Nadeem"));
    }

    private static void waitForTermination()
    {
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                Application.quit();
                // Don't finish executing this shutdown thread until the application has finished shutting down and tells it to.
                SHUTDOWN_STALLER.stall();
            }
        });
        // Pause the main() thread. The shutdown hook registered above will resume execution when it's time to shutdown.
        APPLICATION_STALLER.stall();
    }

    public static void quit()
    {
        APPLICATION_STALLER.unstall();
    }

    public static String getMainContextFileLocation()
    {
        return ApplicationContext.MAIN_CONTEXT_FILE;
    }

    private Application()
    {
        throw new AssertionError("no instances of this class ever!");
    }
}
