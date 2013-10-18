package com.nadeem.app.support;

/**
 * Class to easily suspend (and resume) the execution of a thread in a multithreaded application.
 */
public class ThreadStaller
{
    private boolean stalled = false;
    private Object mutex;

    public ThreadStaller()
    {
        this.mutex = this;
    }

    public ThreadStaller(Object mutex)
    {
        this.mutex = mutex;
    }

    /**
     * Suspend the execution of the current thread.
     * 
     * @throws InterruptedException
     */
    public void stall()
    {
        synchronized (this.mutex)
        {
            stalled = true;
            while (stalled)
            {
                try
                {
                    this.mutex.wait();
                }
                catch (InterruptedException e)
                {
                    unstall();
                }
            }
        }
    }

    /**
     * Called from another thread, unstall() resumes the execution of the thread suspended by calling stall().
     */
    public void unstall()
    {
        synchronized (this.mutex)
        {
            this.stalled = false;
            this.mutex.notifyAll();
        }
    }
}
