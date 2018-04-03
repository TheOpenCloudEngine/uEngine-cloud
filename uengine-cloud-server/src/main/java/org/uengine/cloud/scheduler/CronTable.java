package org.uengine.cloud.scheduler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.uengine.cloud.app.*;
import org.uengine.cloud.deployment.DeploymentHistoryEntity;
import org.uengine.cloud.deployment.DeploymentHistoryRepository;
import org.uengine.cloud.deployment.DeploymentStatus;
import org.uengine.cloud.strategies.Canary;
import org.uengine.iam.util.HttpUtils;
import org.uengine.iam.util.JsonUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.uengine.cloud.ssh.SshService;
import org.uengine.iam.util.StringUtils;

import java.util.*;

/**
 * Created by uengine on 2017. 12. 4..
 */
@Component
public class CronTable implements InitializingBean {

    @Autowired
    private SshService sshService;

    @Autowired
    private DcosApi dcosApi;

    @Autowired
    private AppService appService;

    @Autowired
    private Environment environment;

    @Autowired
    private AppJpaRepository appEntityRepository;

    @Autowired
    private AppAccessLevelRepository appAccessLevelRepository;

    @Autowired
    private DeploymentHistoryRepository historyRepository;

    private String host;
    private String token;
    private Long lastDeploymentsReadSuccessTime;

    public Map<String, Object> dcosData;

    public List<AppEntity> appEntityList;

    public Map getDcosData() {
        return dcosData;
    }

    public List<AppEntity> getAppEntityList() {
        return appEntityList;
    }


    public Long getLastDeploymentsReadSuccessTime() {
        return lastDeploymentsReadSuccessTime;
    }

    public void setLastDeploymentsReadSuccessTime(Long lastDeploymentsReadSuccessTime) {
        this.lastDeploymentsReadSuccessTime = lastDeploymentsReadSuccessTime;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.host = environment.getProperty("dcos.host");
        this.token = environment.getProperty("dcos.token");
        this.dcosData = new HashMap<String, Object>();
        this.appEntityList = new ArrayList<>();
    }

    private Map<String, String> addHeaders() {
        Map headers = new HashMap();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "token=" + token);
        return headers;
    }

    // 애플리케이션 시작 후 1초 후에 첫 실행, 그 후 매 2초마다 주기적으로 실행한다.
    @Scheduled(initialDelay = 1000, fixedDelay = 2000)
    public void fetchDcosData() throws Exception {
        //last
        try {
            HttpResponse response = new HttpUtils().makeRequest("GET",
                    host + "dcos-history-service/history/last",
                    null,
                    this.addHeaders()
            );
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity);
            dcosData.put("last", JsonUtils.unmarshal(json));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //jobs
        try {
            HttpResponse response = new HttpUtils().makeRequest("GET",
                    host + "service/metronome/v1/jobs?embed=activeRuns&embed=schedules&embed=historySummary",
                    null,
                    this.addHeaders()
            );
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity);
            dcosData.put("jobs", JsonUtils.unmarshalToList(json));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //groups
        try {
            HttpResponse response = new HttpUtils().makeRequest("GET",
                    host + "service/marathon/v2/groups?embed=group.groups&embed=group.apps&embed=group.pods&embed=group.apps.deployments&embed=group.apps.counts&embed=group.apps.tasks&embed=group.apps.taskStats&embed=group.apps.lastTaskFailur",
                    null,
                    this.addHeaders()
            );
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity);
            dcosData.put("groups", JsonUtils.unmarshal(json));
        } catch (Exception e) {
            dcosData.put("groups", null);
            e.printStackTrace();
        }

        //queue
        try {
            HttpResponse response = new HttpUtils().makeRequest("GET",
                    host + "service/marathon/v2/queue",
                    null,
                    this.addHeaders()
            );
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity);
            dcosData.put("queue", JsonUtils.unmarshal(json));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //deployments
        try {
            HttpResponse response = new HttpUtils().makeRequest("GET",
                    host + "service/marathon/v2/deployments",
                    null,
                    this.addHeaders()
            );
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity);
            dcosData.put("deployments", JsonUtils.unmarshalToList(json));

            this.setLastDeploymentsReadSuccessTime(new Date().getTime());
        } catch (Exception e) {
            //이상일 경우 null
            dcosData.put("deployments", null);
            e.printStackTrace();
        }

        //units
        try {
            HttpResponse response = new HttpUtils().makeRequest("GET",
                    host + "system/health/v1/units",
                    null,
                    this.addHeaders()
            );
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity);
            dcosData.put("units", JsonUtils.unmarshal(json));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //state
        try {
            HttpResponse response = new HttpUtils().makeRequest("GET",
                    host + "mesos/master/state",
                    null,
                    this.addHeaders()
            );
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity);
            dcosData.put("state", JsonUtils.unmarshal(json));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 애플리케이션 시작 후 1초 후에 첫 실행, 그 후 매 2초마다 주기적으로 실행한다.
    @Scheduled(initialDelay = 1000, fixedDelay = 2000)
    public void getAppsData() throws Exception {
        try {
            List<AppEntity> all = appEntityRepository.findAll();
            for (int i = 0; i < all.size(); i++) {
                appAccessLevelRepository.addGitlabMember(all.get(i));
            }
            appEntityList = all;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 애플리케이션 시작 후 10초 후에 첫 실행, 그 후 매 10초마다 주기적으로 실행한다.
    @Scheduled(initialDelay = 10000, fixedDelay = 10000)
    public void removeSshContainer() throws Exception {
        Date currentDate = new Date();
        Map groups = dcosApi.getGroups();
        List<Map> apps = (List<Map>) groups.get("apps");
        for (int i = 0; i < apps.size(); i++) {
            Map marathonApp = apps.get(i);
            String marathonAppId = marathonApp.get("id").toString();
            if (marathonAppId.startsWith("/ssh-")) {
                String taskId = marathonAppId.replace("/ssh-", "");

                //허트비트 매퍼에 마라톤아이디가 없으면 삭제.
                if (!sshService.heartbeatMapper.containsKey(taskId)) {
                    sshService.deleteSshContainer(taskId);
                }
                //허트비트 매퍼에, 10초 이상된 taskId 삭제.
                else {
                    Date lastDate = sshService.heartbeatMapper.get(taskId);
                    long diff = currentDate.getTime() - lastDate.getTime();
                    if (diff > 1000 * 10) {
                        sshService.deleteSshContainer(taskId);
                    }
                }
            }
        }
    }

    // 애플리케이션 시작 후 10초 후에 첫 실행, 그 후 매 2초마다 주기적으로 실행한다.
    @Scheduled(initialDelay = 2000, fixedDelay = 2000)
    public void checkDeploymentComplete() throws Exception {
        if (this.getDcosData().get("deployments") == null ||
                this.getDcosData().get("groups") == null) {
            return;
        }

        List<Map> deployments = (List<Map>) this.getDcosData().get("deployments");
        List<Map> apps = (List<Map>) ((Map) this.getDcosData().get("groups")).get("apps");

        List<AppEntity> appEntityList = this.getAppEntityList();

        String[] stages = new String[]{"dev", "stg", "prod"};

        //for appEntity,
        for (AppEntity appEntity : appEntityList) {
            for (String stage : stages) {
                AppStage appStage = appService.getAppStage(appEntity, stage);

                DeploymentStatus status = appStage.getTempDeployment().getStatus();

                //Time check. LastDeploymentsReadSuccessTime should more than start time + 2s.
                if (DeploymentStatus.RUNNING_ROLLBACK.equals(status) || DeploymentStatus.RUNNING.equals(status)) {
                    boolean enableCheckDeployment = false;

                    Long startTime = null;
                    if (DeploymentStatus.RUNNING_ROLLBACK.equals(status)) {
                        startTime = appStage.getTempDeployment().getRollbackStartTime();
                    } else if (DeploymentStatus.RUNNING.equals(status)) {
                        startTime = appStage.getTempDeployment().getStartTime();
                    }
                    if (startTime != null && this.getLastDeploymentsReadSuccessTime() != null) {
                        if (this.getLastDeploymentsReadSuccessTime() > (startTime + 2000)) {
                            enableCheckDeployment = true;
                        }
                    }
                    if (!enableCheckDeployment) {
                        continue;
                    }
                }

                //if RUNNING_ROLLBACK
                if (DeploymentStatus.RUNNING_ROLLBACK.equals(status)) {

                    //finish deployment if deployment end.
                    if (this.isDeploymentFinished(appStage, deployments)) {
                        //save history and finish deployment
                        if (this.enableOverrideAppStage(appEntity, stage)) {
                            //save history and finish deployment
                            appService.finishDeployment(
                                    appEntity,
                                    appStage,
                                    stage,
                                    DeploymentStatus.ROLLBACK_SUCCEED);
                        }
                    }
                }
                //if RUNNING
                else if (DeploymentStatus.RUNNING.equals(status)) {

                    boolean bluegreen = appStage.getDeploymentStrategy().getBluegreen();
                    boolean auto = appStage.getDeploymentStrategy().getCanary().getAuto();
                    Long deploymentEndTime = appStage.getTempDeployment().getDeploymentEndTime();

                    //if deployment is finished && not has deploymentEndTime, record deploymentEndTime
                    if (this.isDeploymentFinished(appStage, deployments) && deploymentEndTime == null) {

                        //update Deployment End Time.
                        long nowTime = new Date().getTime();
                        appStage.getTempDeployment().setDeploymentEndTime(nowTime);
                        appService.setAppStage(appEntity, appStage, stage);

                        if (this.enableOverrideAppStage(appEntity, stage)) {
                            appEntityRepository.save(appEntity);
                        }
                    }

                    //if not auto canary mode
                    else if (bluegreen && !auto) {
                        //Nothing to do (By user Handle).
                    }
                    //if auto canary mode
                    else if (bluegreen && auto) {

                        //if has deploymentEndTime , (timer started)
                        if (deploymentEndTime != null) {
                            int increase = appStage.getDeploymentStrategy().getCanary().getIncrease();
                            int test = appStage.getDeploymentStrategy().getCanary().getTest();
                            int decrease = appStage.getDeploymentStrategy().getCanary().getDecrease();

                            long totalTime = Long.valueOf((increase + test + decrease)) * 60 * 1000;
                            long increaseTime = Long.valueOf((increase)) * 60 * 1000;
                            long testTime = Long.valueOf((test)) * 60 * 1000;
                            long decreaseTime = Long.valueOf((decrease)) * 60 * 1000;

                            long currentTime = new Date().getTime();
                            int currentWeight = appStage.getDeploymentStrategy().getCanary().getWeight();

                            //Update weight if time is not over (all is minute base)
                            if ((deploymentEndTime + totalTime) > currentTime) {

                                Long newWeight = new Long(0);
                                String currentStep = null;

                                //it is increase time
                                if ((deploymentEndTime + increaseTime) > currentTime) {
                                    long diff = currentTime - deploymentEndTime;
                                    newWeight = (long) (50 * ((double) diff / (double) decreaseTime));
                                    currentStep = "increase";
                                }

                                //it is test time
                                else if ((deploymentEndTime + increaseTime + testTime) > currentTime) {
                                    newWeight = new Long(50);
                                    currentStep = "test";
                                }

                                //it is decrease time
                                else {
                                    long diff = currentTime - deploymentEndTime - increaseTime - testTime;
                                    newWeight = 50 + (long) (50 * ((double) diff / (double) decreaseTime));
                                    currentStep = "decrease";
                                }

                                //save if newWeight is diff currentWeight
                                if (newWeight.intValue() != currentWeight) {
                                    appStage.getTempDeployment().setCurrentStep(currentStep);
                                    Long minuteFromDeployment = (currentTime - deploymentEndTime) / (1000 * 60);
                                    appStage.getTempDeployment().setMinuteFromDeployment(minuteFromDeployment.intValue());
                                    appStage.getDeploymentStrategy().getCanary().setWeight(newWeight.intValue());
                                    appEntity = appService.setAppStage(appEntity, appStage, stage);
                                    if (this.enableOverrideAppStage(appEntity, stage)) {
                                        appEntityRepository.save(appEntity);
                                    }
                                }
                            }
                            //finishManualCanaryDeployment if time is over
                            else {
                                if (this.enableOverrideAppStage(appEntity, stage)) {
                                    appService.finishManualCanaryDeployment(appEntity.getName(), stage);
                                }
                            }
                        }
                    }
                    //else
                    else {
                        //finish deployment if deployment end.
                        if (this.isDeploymentFinished(appStage, deployments)) {
                            if (this.enableOverrideAppStage(appEntity, stage)) {
                                //save history and finish deployment
                                appService.finishDeployment(
                                        appEntity,
                                        appStage,
                                        stage,
                                        DeploymentStatus.SUCCEED);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean enableOverrideAppStage(AppEntity entity, String stage) {
        AppEntity existEntity = appEntityRepository.findOne(entity.getName());
        if (existEntity == null) {
            return false;
        }
        AppStage appStage = appService.getAppStage(existEntity, stage);
        if (appStage.getTempDeployment().getStatus() == null) {
            return false;
        }
        return true;
    }

    private boolean isDeploymentFinished(AppStage appStage, List<Map> deployments) {
        String deploymentId = appStage.getTempDeployment().getDeploymentId();

        if (!StringUtils.isEmpty(deploymentId)) {
            boolean isRunning = false;
            for (Map deployment : deployments) {
                String marathonDeploymentId = deployment.get("id").toString();

                //마라톤에 디플로이먼트가 진행중이다.
                if (marathonDeploymentId.equals(deploymentId)) {
                    isRunning = true;
                }
            }

            //if deployment finish
            if (!isRunning) {
                return true;
            } else {
                return false;
            }
        } else {
            //Undefined deploymentId, we can't know isFinished.
            return false;
        }
    }
}
