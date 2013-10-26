package com.nadeem.app.scheduler;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Date;

import org.joda.time.Minutes;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import com.nadeem.app.scheduler.BaseDynamicScheduler.InvocationDetail;
import com.nadeem.app.scheduler.BaseDynamicScheduler.MethodInvocatingScheduledJob;

public class BaseDynamicSchedulerTest
{
    private BaseDynamicScheduler targetBeingTested;
    private MethodInvocatingScheduledJob scheduledJob;

    @Mock
    private Scheduler scheduler;
    @Mock
    private JobDetail jobDetail;
    @Mock
    private JobExecutionContext jobExecutionContext;
    @Mock
    private JobDataMap jobDataMap;
    @Mock
    private TestClass testClass;

    @Before
    public void doBeforeEachTest()
    {
        MockitoAnnotations.initMocks(this);
        this.targetBeingTested = new BaseDynamicScheduler(this.scheduler);
        this.scheduledJob = new MethodInvocatingScheduledJob();
    }

    @Test
    public void shouldScheduleNewScheduler() throws SchedulerException
    {
        this.targetBeingTested.scheduleWithInterval("Job", "GROUP", Minutes.ONE.toStandardDuration(),
            new InvocationDetail(new Object(), "GROUP", Arrays.asList("Prof")));
        verify(this.scheduler, times(1)).scheduleJob(any(JobDetail.class), any(Trigger.class));
    }

    @Test
    public void shouldReScheduleScheduler() throws Exception
    {
        when(this.scheduler.getJobDetail(anyString(), anyString())).thenReturn(this.jobDetail);
        this.targetBeingTested.scheduleInvocation("Job", "GROUP", new Date(), new InvocationDetail(new Object(),
            "GROUP", Arrays.asList("Prof")));
        verify(this.scheduler, times(1)).rescheduleJob(anyString(), anyString(), any(Trigger.class));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenFetchingJobDetailsFails() throws SchedulerException
    {
        doThrow(new SchedulerException()).when(this.scheduler).getJobDetail(anyString(), anyString());
        this.targetBeingTested.scheduleInvocation("Job", "GROUP", new Date(), new InvocationDetail(new Object(),
            "GROUP", Arrays.asList("Prof")));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenSchedulingNewOne() throws SchedulerException
    {
        when(this.scheduler.getJobDetail(anyString(), anyString())).thenReturn(null);
        doThrow(new SchedulerException()).when(this.scheduler).scheduleJob((JobDetail) anyObject(),
            (Trigger) anyObject());
        this.targetBeingTested.scheduleInvocation("Job", "GROUP", new Date(), new InvocationDetail(new Object(),
            "GROUP", Arrays.asList("Prof")));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenReSchedulingAJob() throws SchedulerException
    {
        when(this.scheduler.getJobDetail(anyString(), anyString())).thenReturn(new JobDetail());
        doThrow(new SchedulerException()).when(this.scheduler).rescheduleJob(anyString(), anyString(),
            (Trigger) anyObject());
        this.targetBeingTested.scheduleInvocation("Job", "GROUP", new Date(), new InvocationDetail(new Object(),
            "GROUP", Arrays.asList("Prof")));
    }

    @Test
    public void shouldRemoveSchedulerSuccessfully() throws SchedulerException
    {
        this.targetBeingTested.deleteJob(anyString(), anyString());
        verify(this.scheduler, times(1)).removeTriggerListener(anyString());
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhileRemovingScheduler() throws SchedulerException
    {
        doThrow(new SchedulerException()).when(this.scheduler).removeTriggerListener(anyString());
        this.targetBeingTested.deleteJob(anyString(), anyString());
    }

    @Test
    public void shouldSuccessfullySetProperties() throws Exception
    {
        this.targetBeingTested.afterPropertiesSet();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionIfMethodIsBlankOnAfterPropertiesSet() throws Exception
    {
        new BaseDynamicScheduler(null).afterPropertiesSet();
    }

    @Test
    public void testScheduledJobClassExecuteMethod() throws JobExecutionException, NoSuchMethodException
    {
        when(this.jobDataMap.get("targetBean")).thenReturn(this.testClass);
        when(this.jobDataMap.getString("method")).thenReturn("testMethod");
        when(this.jobDataMap.get("arguments")).thenReturn(null);
        when(this.jobDetail.getJobDataMap()).thenReturn(this.jobDataMap);
        when(this.jobExecutionContext.getJobDetail()).thenReturn(this.jobDetail);

        this.scheduledJob.execute(this.jobExecutionContext);
        verify(this.testClass, atLeastOnce()).testMethod();
    }

    @Test(expected = JobExecutionException.class)
    public void shouldThrowExceptionWhileExecutingScheduledJob() throws JobExecutionException,
        InvocationTargetException, IllegalAccessException
    {
        when(this.jobDetail.getJobDataMap()).thenReturn(this.jobDataMap);
        when(this.jobExecutionContext.getJobDetail()).thenReturn(this.jobDetail);

        this.scheduledJob.execute(this.jobExecutionContext);
    }

    private static class TestClass
    {
        public void testMethod()
        {
            // TODO
        }
    }
}
