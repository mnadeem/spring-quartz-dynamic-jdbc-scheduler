package com.nadeem.app.scheduler;

import java.text.ParseException;
import java.util.Date;

import org.joda.time.Duration;
import org.quartz.CronTrigger;
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

    private static final String TARGET_BEAN = "targetBean";
    private static final String ARGUMENTS_KEY = "arguments";
    private static final String METHOD_NAME_KEY = "method";

    private final Scheduler scheduler;

    public BaseDynamicScheduler(final Scheduler newScheduler)
    {
        this.scheduler = newScheduler;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        Assert.notNull(this.scheduler, "Scheduler must be set.");
    }

    public void scheduleInvocation(final String jobName, final String group, final String cronExpression,
        final InvocationDetail invocationDetail)
    {
        schedule(createDynamicJobDetail(invocationDetail, jobName, group), buildCronTrigger(jobName, group, cronExpression));
    }

    private Trigger buildCronTrigger(final String jobName, final String group, final String cronExpression)
    {
        CronTrigger trigger;
        try
        {
            trigger = new CronTrigger(jobName, group, jobName, group, cronExpression);
        }
        catch (ParseException e)
        {
            throw new IllegalArgumentException("Invalid cronExpression " + cronExpression, e);
        }
        return trigger;
    }

    public void scheduleInvocation(final String jobName, final String group, final Date when,
        final InvocationDetail invocationDetail)
    {
        schedule(createDynamicJobDetail(invocationDetail, jobName, group), buildExactTimeTrigger(jobName, group, when));
    }

    private SimpleTrigger buildExactTimeTrigger(final String jobName, final String group, final Date when)
    {
        SimpleTrigger trigger = new SimpleTrigger(jobName, group, when);
        trigger.setJobName(jobName);
        trigger.setJobGroup(group);
        return trigger;
    }

    public void scheduleWithInterval(final String jobName, final String group, final Duration repeateInterval,
        final InvocationDetail invocationDetail)
    {
        SimpleTrigger trigger = buildRepeatingInterval(jobName, group, repeateInterval);
        schedule(createDynamicJobDetail(invocationDetail, jobName, group), trigger);
    }

    private SimpleTrigger buildRepeatingInterval(final String jobName, final String group,
        final Duration repeateInterval)
    {
        SimpleTrigger trigger = new SimpleTrigger(jobName, group, new Date());
        trigger.setRepeatInterval(repeateInterval.getMillis());
        trigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
        trigger.setJobName(jobName);
        trigger.setJobGroup(group);
        return trigger;
    }

    private JobDetail createDynamicJobDetail(final InvocationDetail invocationDetail, final String jobName, final String group)
    {
        JobDetail detail = new JobDetail(jobName, group, MethodInvocatingScheduledJob.class);
        setJobArguments(invocationDetail, detail);
        setJobToAutoDelete(detail);
        return detail;
    }

    private void setJobArguments(final InvocationDetail invocationDetail, final JobDetail detail)
    {
        detail.getJobDataMap().put(TARGET_BEAN, invocationDetail.getTargetBean());
        detail.getJobDataMap().put(METHOD_NAME_KEY, invocationDetail.getTargetMethod());
        detail.getJobDataMap().put(ARGUMENTS_KEY, invocationDetail.getMethodArgs());
    }

    private void setJobToAutoDelete(final JobDetail detail)
    {
        detail.setDurability(false);
    }

    public void schedule(final JobDetail job, final Trigger trigger)
    {
        if (isJobExists(job))
        {
            rescheduleJob(job, trigger);
        }
        else
        {
            doScheduleJob(job, trigger);
        }
    }

    public boolean isJobExists(final JobDetail job)
    {
        try
        {
            return this.scheduler.getJobDetail(job.getName(), job.getGroup()) != null;
        }
        catch (SchedulerException e)
        {
            throw new IllegalStateException("Failed to find Job.", e);
        }
    }

    private void doScheduleJob(final JobDetail job, final Trigger trigger)
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
            throw new IllegalStateException("Failed to reschedule the Job.", e);
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
            throw new IllegalStateException("Failed to delete the Job.", e);
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

    public static class InvocationDetail
    {
        private Object targetBean;
        private String targetMethod;
        private Object[] methodArgs;

        public InvocationDetail(final Object newTargetBean, final String newTargetMethod, final Object[] newMethodArgs)
        {
            this.targetBean = newTargetBean;
            this.targetMethod = newTargetMethod;
            this.methodArgs = newMethodArgs;
        }

        public Object getTargetBean()
        {
            return targetBean;
        }

        public String getTargetMethod()
        {
            return targetMethod;
        }

        public Object[] getMethodArgs()
        {
            return methodArgs;
        }
    }
}
