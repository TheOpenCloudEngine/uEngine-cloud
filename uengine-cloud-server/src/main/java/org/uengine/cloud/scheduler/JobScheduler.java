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
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;

import java.util.List;
import java.util.Map;

/**
 * Job Scheduler를 관리하기 위한 다양한 기능을 제공하는 Job Scheduler Interface.
 *
 * @author Byoung Gon, Kim
 * @version 0.2
 */
public interface JobScheduler {

    /**
     * 새로운 Job을 등록하고 Cron Expression에 따라서 지정한 시간에 동작을 시작한다.
     *
     * @param jobName        Job Name
     * @param jobGroupName   Job Group Name
     * @param cronExpression Cron Expression
     * @param dataMap        Job Varaibles
     * @return Job Key
     */
    JobKey startJob(String jobName, String jobGroupName, String cronExpression, Map<String, Object> dataMap) throws ServiceException;

    /**
     * 새로운 Job을 등록하고 Cron Expression에 따라서 지정한 시간에 동작을 시작한다.
     * status가 running이 아닌 경우에 사용한다.
     *
     * @param jobName        Job Name
     * @param jobGroupName   Job Group Name
     * @param cronExpression Cron Expression
     * @param dataMap        Job Varaibles
     * @return Job Key
     */
    JobKey registJob(String jobName, String jobGroupName, String cronExpression, Map<String, Object> dataMap) throws ServiceException;

    /**
     * 새로운 Job을 등록하고 바로 동작을 시작한다.
     *
     * @param jobName      Job Name
     * @param jobGroupName Job Group Name
     * @param dataMap      Job Varaibles
     * @return Job Key
     */
    JobKey startJobImmediatly(String jobName, String jobGroupName, Map<String, Object> dataMap) throws ServiceException;

    /**
     * 등록된 Job을 삭제한다.
     *
     * @param jobName      Job Name
     * @param jobGroupName Job Group Name
     * @return Job Key
     */
    JobKey deleteJob(String jobName, String jobGroupName) throws ServiceException;

    /**
     * Job을 즉시 실행한다.
     *
     * @param jobName      Job Name
     * @param jobGroupName Job Group Name
     * @return Job Key
     */
    JobKey triggerJob(String jobName, String jobGroupName) throws ServiceException;

    /**
     * 동작중인 Job을 일시 중지한다.
     *
     * @param jobName      Job Name
     * @param jobGroupName Job Group Name
     */
    void pauseJob(String jobName, String jobGroupName) throws ServiceException;

    /**
     * 일지 중지 상태인 Job을 다시 시작한다.
     *
     * @param jobName      Job Name
     * @param jobGroupName Job Group Name
     */
    void resumeJob(String jobName, String jobGroupName) throws ServiceException;

    /**
     * 실행 중인 Job을 중지한다.
     *
     * @param jobName      Job Name
     * @param jobGroupName Job Group Name
     */
    void stopJob(String jobName, String jobGroupName) throws ServiceException;

    /**
     * 실행 중인 Job을 조회한다.
     *
     * @return Quartz Job Execution Context 목록
     */
    List<JobExecutionContext> getCurrentExecutingJobs() throws ServiceException;

    /**
     * 지정한 Job Name의 Job이 현재 실행중인지 확인한다.
     *
     * @param jobName      Job Name
     * @param jobGroupName Job Group Name
     * @return 실행중인 경우 <tt>true</tt>
     */
    public boolean isCurrentExecutingJob(String jobName, String jobGroupName);

    /**
     * Job Id의 저장된 Job Data Map을 조회한다.
     *
     * @param jobName      Job Name
     * @param jobGroupName Job Group Name
     * @return Job의 Key Value 형식의 메타 정보
     */
    Map<String, Object> getJobDataMap(String jobName, String jobGroupName) throws ServiceException;

    /**
     * 등록되어 있는 모든 스케줄링 작업을 반환한다.
     *
     * @return 등록되어 있는 모든 스케줄링 작업
     * @throws ServiceException 스케줄링 작업을 확인할 수 없는 경우
     */
    List<JobDetail> getAllJobs() throws ServiceException;


}