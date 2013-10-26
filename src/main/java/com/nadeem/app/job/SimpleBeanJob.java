package com.nadeem.app.job;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

import com.nadeem.app.job.support.SimpleBean;

public class SimpleBeanJob extends QuartzJobBean
{

    private SimpleBean simpleBean;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException
    {
        this.simpleBean.execute();        
    }

    public void setSimpleBean(SimpleBean simpleBean)
    {
        this.simpleBean = simpleBean;
    }
}
