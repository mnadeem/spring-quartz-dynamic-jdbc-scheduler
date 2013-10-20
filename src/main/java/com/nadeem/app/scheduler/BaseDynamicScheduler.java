package com.nadeem.app.scheduler;

import java.util.Date;

import org.joda.time.Duration;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.support.ArgumentConvertingMethodInvoker;
import org.springframework.util.Assert;
import org.springframework.util.MethodInvoker;

public class BaseDynamicScheduler implements InitializingBean
{
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseDynamicScheduler.class);

    private static final String TARGET_BEAN     = "targetBean";
    private static final String ARGUMENTS_KEY   = "arguments";
    private static final String METHOD_NAME_KEY = "method";

    private Scheduler scheduler;
    private Object targetBean;
    private String targetMethod;

    public BaseDynamicScheduler(final Scheduler newScheduler)
    {
        this.scheduler = newScheduler;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        Assert.notNull(this.scheduler, "Scheduler must be set.");
        Assert.notNull(this.targetBean, "Bean should not be null.");
        Assert.hasText(this.targetMethod, "Method name should not be blank.");
    }

    public void scheduleInvocation(final String jobName, final String group, final Date when, final Object[] args)
    {
        SimpleTrigger trigger = new SimpleTrigger(jobName, group, when);
        trigger.setJobName(jobName);
        trigger.setJobGroup(group);
        doSchedule(createJobDetail(args, jobName, group), trigger);
    }

    public void scheduleWithInterval(final String jobName, final String group, final Duration repeateInterval, final Object[] args)
    {
        SimpleTrigger trigger = new SimpleTrigger(jobName, group, new Date());
        trigger.setRepeatInterval(repeateInterval.getStandardSeconds());
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        trigger.setJobName(jobName);
        trigger.setJobGroup(group);
        doSchedule(createJobDetail(args, jobName, group), trigger);
    }

    private JobDetail createJobDetail(final Object[] args, final String jobName, final String group)
    {
        JobDetail detail = new JobDetail(jobName, group, MethodInvocatingScheduledJob.class);
        setJobArguments(args, detail);
        setJobToAutoDelete(detail);
        return detail;
    }

    private void setJobArguments(final Object[] args, final JobDetail detail)
    {
        detail.getJobDataMap().put(TARGET_BEAN, this.targetBean);
        detail.getJobDataMap().put(METHOD_NAME_KEY, this.targetMethod);
        detail.getJobDataMap().put(ARGUMENTS_KEY, args);
    }

    private void setJobToAutoDelete(final JobDetail detail)
    {
        detail.setDurability(false);
    }
    private void doSchedule(final JobDetail job, final Trigger trigger)
    {
        if (isJobExists(job))
        {
            rescheduleJob(job, trigger);
        }
        else
        {
            scheduleJob(job, trigger);
        }
    }

    private boolean isJobExists(final JobDetail job)
    {
        try
        {
            return this.scheduler.getJobDetail(job.getName(), job.getGroup()) != null;
        }
        catch (SchedulerException e)
        {
            throw new IllegalStateException("Failed to schedule the Job.");
        }
    }

    private void scheduleJob(final JobDetail job, final Trigger trigger)
    {
        try
        {
            this.scheduler.scheduleJob(job, trigger);
        }
        catch (SchedulerException e)
        {
            throw new IllegalStateException("Failed to schedule the Job.", e);
        }
    }

    private void rescheduleJob(final JobDetail job, final Trigger trigger)
    {
        try
        {
            this.scheduler.rescheduleJob(trigger.getName(), job.getGroup(), trigger);
        }
        catch (SchedulerException e)
        {
            throw new IllegalStateException("Failed to schedule the Job.", e);
        }
    }

    public void deleteJob(final String jobName, final String group)
    {
        try
        {
            this.scheduler.unscheduleJob(jobName, group);
            this.scheduler.deleteJob(jobName, group);
            this.scheduler.removeTriggerListener(jobName);
        }
        catch (SchedulerException e)
        {
            throw new IllegalStateException("Failed to schedule the Job.", e);
        }
    }

    public static class MethodInvocatingScheduledJob implements Job
    {
        @Override
        public void execute(final JobExecutionContext context) throws JobExecutionException
        {
            try
            {
                JobDataMap data = jobData(context);
                invokeMethod(targetBean(data), method(data), arguments(data));
            }
            catch (Exception e)
            {
                LOGGER.error(e.getMessage());
                throw new JobExecutionException(e);
            }
        }

        private JobDataMap jobData(final JobExecutionContext context)
        {
            return context.getJobDetail().getJobDataMap();
        }

        private Object targetBean(final JobDataMap data) throws Exception
        {
            return data.get(TARGET_BEAN);
        }

        private String method(final JobDataMap data)
        {
            return data.getString(METHOD_NAME_KEY);
        }

        private Object[] arguments(final JobDataMap data)
        {
            return (Object[]) data.get(ARGUMENTS_KEY);
        }

        private void invokeMethod(final Object target, final String method, final Object[] args) throws Exception
        {
            MethodInvoker inv = new ArgumentConvertingMethodInvoker();

            inv.setTargetObject(target);
            inv.setTargetMethod(method);
            inv.setArguments(args);
            inv.prepare();
            inv.invoke();
        }
    }

    public void setScheduler(final Scheduler newScheduler)
    {
        this.scheduler = newScheduler;
    }

    public void setTargetBean(final Object newTargetBean)
    {
        this.targetBean = newTargetBean;
    }

    public void setTargetMethod(final String newTargetMethod)
    {
        this.targetMethod = newTargetMethod;
    }
}
