package org.uengine.cloud.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.uengine.cloud.app.deployment.AppDeploymentService;
import org.uengine.cloud.app.marathon.DcosApi;
import org.uengine.cloud.app.marathon.MarathonCacheService;
import org.uengine.cloud.deployment.DeploymentStatus;
import org.uengine.cloud.redis.LeaderWrapper;
import org.uengine.iam.util.StringUtils;

import java.util.*;

/**
 * Created by uengine on 2017. 12. 4..
 */
@Component
public class AppScheduler {

    @Autowired
    private AppWebService appWebService;

    @Autowired
    private AppWebCacheService appWebCacheService;

    @Autowired
    private LeaderWrapper leaderWrapper;

    @Autowired
    private MarathonCacheService marathonCacheService;

    @Autowired
    private DcosApi dcosApi;

    @Autowired
    private AppDeploymentService deploymentService;

    @Autowired
    private AppLockService lockService;

    public List<AppEntity> appEntityList;


    @Scheduled(initialDelay = 1000, fixedDelay = 3000)
    public void leaderScheduler() {
        //if not leader, skip.
        if (!leaderWrapper.amILeader()) {
            return;
        }
        try {
            appWebCacheService.updateAllAppNamesCache();
        } catch (Exception ex) {

        }
        try {
            List<AppEntity> appEntities = appWebCacheService.updateAllAppsCache();
            this.appEntityList = appEntities;
        } catch (Exception ex) {

        }
        try {
            marathonCacheService.updateServiceAppsCache();
        } catch (Exception ex) {

        }

        try {
            marathonCacheService.updateDcosLastCache();
        } catch (Exception ex) {

        }

        try {
            this.checkDeploymentComplete();
        } catch (Exception ex) {

        }
    }


    //TODO deployment event from kafka.
    public void checkDeploymentComplete() throws Exception {
        List<Map> deployments = dcosApi.getDeployments();
        List<AppEntity> appEntityList = this.appEntityList;
        Long lastDeploymentsReadSuccessTime = new Date().getTime();

        String[] stages = new String[]{"dev", "stg", "prod"};

        //for appEntity,
        for (AppEntity appEntity : appEntityList) {
            for (String stage : stages) {
                AppStage appStage = appWebService.getAppStage(appEntity, stage);

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
                    if (startTime != null && lastDeploymentsReadSuccessTime > (startTime + 2000)) {
                        enableCheckDeployment = true;
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

                            if (!lockService.isLock(appEntity.getName())) {
                                //save history and finish deployment
                                deploymentService.finishDeployment(
                                        appEntity,
                                        appStage,
                                        stage,
                                        DeploymentStatus.ROLLBACK_SUCCEED);
                            }
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
                        appWebService.setAppStage(appEntity, appStage, stage);

                        if (this.enableOverrideAppStage(appEntity, stage)) {

                            if (!lockService.isLock(appEntity.getName())) {
                                appWebService.save(appEntity);
                            }
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
                                    appEntity = appWebService.setAppStage(appEntity, appStage, stage);
                                    if (this.enableOverrideAppStage(appEntity, stage)) {
                                        if (!lockService.isLock(appEntity.getName())) {
                                            appWebService.save(appEntity);
                                        }
                                    }
                                }
                            }
                            //finishManualCanaryDeployment if time is over
                            else {
                                if (this.enableOverrideAppStage(appEntity, stage)) {
                                    if (!lockService.isLock(appEntity.getName())) {
                                        deploymentService.finishManualCanaryDeployment(appEntity.getName(), stage);
                                    }
                                }
                            }
                        }
                    }
                    //else
                    else {
                        //finish deployment if deployment end.
                        if (this.isDeploymentFinished(appStage, deployments)) {
                            if (this.enableOverrideAppStage(appEntity, stage)) {
                                if (!lockService.isLock(appEntity.getName())) {
                                    //save history and finish deployment
                                    deploymentService.finishDeployment(
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
    }

    private boolean enableOverrideAppStage(AppEntity entity, String stage) {
        AppEntity existEntity = appWebCacheService.findOneCache(entity.getName());
        if (existEntity == null) {
            return false;
        }
        AppStage appStage = appWebService.getAppStage(existEntity, stage);
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
