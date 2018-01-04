/**
 * Copyright (C) 2011 Flamingo Project (http://www.cloudine.io).
 * <p/>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.uengine.cloud.scheduler;

import org.opencloudengine.garuda.common.exception.ServiceException;
import org.opencloudengine.garuda.util.DateUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Quartz Job Scheduler 기반 Scheduler Implementation.
 *
 * @author Byoung Gon, Kim
 * @see <a href="http://www.opencloudengine.org/svn/flamingo/trunk/flamingo-workflow-engine/src/main/java/org/openflamingo/engine/scheduler/QuartzJobScheduler.java">Flamingo 1 Job</a>
 * @since 2.0
 */
@Service
public class QuartzJobScheduler implements JobScheduler {

    /**
     * SLF4J Logging
     */
    private Logger logger = LoggerFactory.getLogger(QuartzJobScheduler.class);

    /**
     * Quartz Job Scheduler
     */
    @Autowired
    private Scheduler scheduler;

    @Override
    public JobKey startJob(String jobName, String jobGroupName, String cronExpression, Map<String, Object> dataMap) throws ServiceException {
        try {
            JobKey jobKey = new JobKey(jobName, jobGroupName);
            JobDetail job = JobBuilder.newJob(CreateAppJob.class).withIdentity(jobKey).build();
            job.getJobDataMap().putAll(dataMap);
            job.getJobDataMap().put("status", "RUNNING");

            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName, jobGroupName)
                    .startAt(new Date())
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)).build();

            Date scheduledTime = scheduler.scheduleJob(job, trigger);

            return jobKey;
        } catch (SchedulerException e) {
            throw new ServiceException("", e);
        }
    }

    @Override
    public JobKey registJob(String jobName, String jobGroupName, String cronExpression, Map<String, Object> dataMap) throws ServiceException {
        try {
            JobKey jobKey = new JobKey(jobName, jobGroupName);
            JobDetail job = JobBuilder.newJob(CreateAppJob.class).withIdentity(jobKey).build();
            if (dataMap.get("status") == null) {
                throw new ServiceException("Job status is null");
            }
            job.getJobDataMap().putAll(dataMap);

            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName, jobGroupName)
                    .startAt(new Date())
                    .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression)).build();

            Date scheduledTime = scheduler.scheduleJob(job, trigger);

            return jobKey;
        } catch (SchedulerException e) {
            throw new ServiceException("", e);
        }
    }

    @Override
    public JobKey startJobImmediatly(String jobName, String jobGroupName, Map<String, Object> dataMap) throws ServiceException {
        try {
            JobKey jobKey = new JobKey(jobName, jobGroupName);

            JobDetail job = null;
            if (jobGroupName.equals("appCreate")) {
                job = JobBuilder.newJob(CreateAppJob.class).withIdentity(jobKey).build();
            } else if (jobGroupName.equals("deployedApp")) {
                job = JobBuilder.newJob(DeployAppJob.class).withIdentity(jobKey).build();
            }
            job.getJobDataMap().putAll(dataMap);

            SimpleTrigger trigger = (SimpleTrigger) TriggerBuilder.newTrigger()
                    .withIdentity(jobName, jobGroupName)
                    .startAt(DateUtils.addSeconds(new Date(), 1))
                    .forJob(jobName, jobGroupName)
                    .build();

            logger.info("The batch job was immediately registered to run once.");
            Date scheduledTime = scheduler.scheduleJob(job, trigger);
            return jobKey;
        } catch (SchedulerException e) {
            throw new ServiceException("", e);
        }
    }


    @Override
    public JobKey deleteJob(String jobName, String jobGroupName) throws ServiceException {
        try {
            JobKey jobKey = new JobKey(jobName, jobGroupName);
            scheduler.deleteJob(jobKey);
            return jobKey;
        } catch (SchedulerException e) {
            throw new ServiceException("", e);
        }
    }

    @Override
    public JobKey triggerJob(String jobName, String jobGroupName) throws ServiceException {
        try {
            TriggerKey triggerKey = new TriggerKey(jobName, jobGroupName);
            Trigger trigger = scheduler.getTrigger(triggerKey);
            if (trigger == null) {
                throw new IllegalStateException("");
            }
        } catch (SchedulerException e) {
            throw new ServiceException("", e);
        }

        try {
            JobKey jobKey = new JobKey(jobName, jobGroupName);
            scheduler.triggerJob(jobKey);
            return jobKey;
        } catch (SchedulerException e) {
            throw new ServiceException("", e);
        }
    }

    @Override
    public void pauseJob(String jobName, String jobGroupName) throws ServiceException {
        try {
            JobKey jobKey = new JobKey(jobName, jobGroupName);
            scheduler.pauseJob(jobKey);

            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            jobDetail.getJobDataMap().put("status", "PAUSED");
            scheduler.addJob(jobDetail, true, true);
        } catch (SchedulerException e) {
            throw new ServiceException("", e);
        }
    }

    @Override
    public void resumeJob(String jobName, String jobGroupName) throws ServiceException {
        try {
            JobKey jobKey = new JobKey(jobName, jobGroupName);

            scheduler.resumeJob(jobKey);

            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            jobDetail.getJobDataMap().put("status", "RESUMED");
            scheduler.addJob(jobDetail, true, true);

        } catch (SchedulerException e) {
            throw new ServiceException("", e);
        }
    }

    @Override
    public void stopJob(String jobName, String jobGroupName) throws ServiceException {
        try {
            JobKey jobKey = new JobKey(jobName, jobGroupName);

            scheduler.unscheduleJob(new TriggerKey(jobName, jobGroupName));
            scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            throw new ServiceException("", e);
        }
    }

    @Override
    public List<JobExecutionContext> getCurrentExecutingJobs() throws ServiceException {
        try {
            return scheduler.getCurrentlyExecutingJobs();
        } catch (SchedulerException e) {
            throw new ServiceException("Unable to retreive the scheduling tasks that are currently running.", e);
        }
    }

    @Override
    public List<JobDetail> getAllJobs() throws ServiceException {
        try {
            List<JobDetail> jobs = new ArrayList<>();
            for (String groupName : scheduler.getJobGroupNames()) {
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                    JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                    jobs.add(jobDetail);
                }
            }
            return jobs;
        } catch (SchedulerException e) {
            throw new ServiceException("Unable to retreive the list of scheduled jobs.", e);
        }
    }

    @Override
    public boolean isCurrentExecutingJob(String jobName, String jobGroupName) {
        try {
            TriggerKey triggerKey = new TriggerKey(jobName, jobGroupName);
            Trigger.TriggerState triggerState = scheduler.getTriggerState(triggerKey);
            return triggerState == Trigger.TriggerState.NORMAL;
        } catch (SchedulerException e) {
            throw new ServiceException("", e);
        }
    }

    @Override
    public Map<String, Object> getJobDataMap(String jobName, String jobGroupName) throws ServiceException {
        try {
            JobKey jobKey = new JobKey(jobName, jobGroupName);
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            return jobDetail.getJobDataMap();
        } catch (SchedulerException e) {
            throw new ServiceException("", e);
        }
    }

    ////////////////////////////////////////////////
    // Spring Framework Setter Injection
    ////////////////////////////////////////////////

    /**
     * Quartz Job Scheduler를 설정한다.
     *
     * @param scheduler Quartz Job Scheduler
     */
    public void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }
}



