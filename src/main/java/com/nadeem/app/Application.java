package com.nadeem.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
            waitForTermination();
        }
        catch (Exception e)
        {
            LOGGER.error(e.getMessage());
        }
        finally
        {
            ApplicationContext.shutdown();
            SHUTDOWN_STALLER.unstall(); // Allow the shutdown thread to resume, after which the VM will terminate.
        }
    }

    private static void waitForTermination()
    {
        Runtime.getRuntime().addShutdownHook(new Thread()
        {
            @Override
            public void run()
            {
                Application.quit();
                SHUTDOWN_STALLER.stall(); // Don't finish executing this shutdown thread until the
                                            //application has finished shutting down and tells it to.
            }
        });
        APPLICATION_STALLER.stall(); // Pause the main() thread. The shutdown hook registered above
                                    //will resume execution when it's time to shutdown.
    }

    public static void quit()
    {
        APPLICATION_STALLER.unstall();
    }

    public static String getMainContextFileLocation()
    {
        return ApplicationContext.MAIN_CONTEXT_FILE_LOCATION;
    }

    private Application()
    {
        throw new AssertionError("no instances of this class ever!");
    }
}
