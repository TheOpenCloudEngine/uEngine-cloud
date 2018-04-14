package org.uengine.cloud.app.deployment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.uengine.cloud.app.*;
import org.uengine.cloud.app.config.AppConfigService;
import org.uengine.cloud.app.marathon.DcosApi;
import org.uengine.cloud.app.marathon.MarathonService;
import org.uengine.cloud.app.snapshot.AppSnapshotService;
import org.uengine.cloud.deployment.*;
import org.uengine.iam.util.StringUtils;

import java.util.*;

/**
 * Created by uengine on 2017. 11. 16..
 */
@Service
public class AppDeploymentService {

    @Autowired
    private Environment environment;

    @Autowired
    private AppWebService appWebService;

    @Autowired
    @Lazy
    private AppAsyncService appAsyncService;

    @Autowired
    private AppConfigService appConfigService;

    @Autowired
    private AppSnapshotService snapshotService;

    @Autowired
    private DeploymentHistoryRepository historyRepository;

    @Autowired
    private MarathonService marathonService;

    @Autowired
    private AppWebCacheService appWebCacheService;

    /**
     * synchronous dcos api need for deployment job
     */
    @Autowired
    private DcosApi dcosApi;

    private static final Logger LOGGER = LoggerFactory.getLogger(AppDeploymentService.class);


    /**
     * 어플리케이션의 주어진 스테이지의 마라톤 서비스를 재기동한다.
     *
     * @param appName
     * @param stage
     * @return
     * @throws Exception
     */
    //TODO need to kafka event
    public void runDeployedApp(
            String appName,
            String stage,
            String commit,
            Long snapshotId,
            String name,
            String description
    ) throws Exception {

        //디플로이 백그라운드 작업 시작.
        appAsyncService.deployApp(appName, stage, commit, snapshotId, name, description);

        //앱 변경 적용됨
        appConfigService.updateAppConfigChanged(appName, stage, false);
    }

    /**
     * Case. Rollback App which is on deployment.
     * <p>
     * 1.move field (set old production to current production)
     * 1)snapshotOld => snapshot
     * 2)deploymentOld => deployment
     * 3)marathonAppIdOld => marathonAppId
     * 4)weight => 100
     * <p>
     * 2.load old production snapshot, and restoreSnapshot without redeployment.
     * 3.remove current production.
     *
     * @param appName
     * @throws Exception
     */
    //TODO need to kafka event
    public void rollbackApp(String appName, String stage) throws Exception {
        AppEntity appEntity = appWebCacheService.findOneCache(appName);

        AppStage appStage = appWebService.getAppStage(appEntity, stage);

        boolean isBlueGreen = false;
        String currentMarathonAppId = null;
        String deploymentId = null;
        if (appStage.getDeploymentStrategy().getBluegreen()) {
            if (!DeploymentStatus.RUNNING.equals(appStage.getTempDeployment().getStatus())) {
                throw new Exception(String.format("Not RUNNING deployment to rollback, %s, %s", appName, stage));
            }
            isBlueGreen = true;
            String deployment = appStage.getDeployment();

            //롤백을 할 수 있는 마라톤 어플이 있는지 확인한다.
            String rollbackMarathonAppId = null;
            currentMarathonAppId = appStage.getMarathonAppId();
            if (deployment.equals("blue")) {
                rollbackMarathonAppId = "/" + appName + "-green";
            } else {
                rollbackMarathonAppId = "/" + appName + "-blue";
            }

            Map marathonApp = dcosApi.getApp(rollbackMarathonAppId);
            if (marathonApp == null) {
                throw new Exception("Not found marathon app to rollback, " + rollbackMarathonAppId);
            }
        } else {
            if (!DeploymentStatus.RUNNING.equals(appStage.getTempDeployment().getStatus())) {
                throw new Exception(String.format("Not RUNNING deployment to rollback, %s, %s", appName, stage));
            }

            deploymentId = appStage.getTempDeployment().getDeploymentId();
            if (marathonService.getMarathonDeploymentById(deploymentId) == null) {
                throw new Exception(String.format(
                        "Not found marathon deployment to rollback, %s, %s, %s", appName, stage, deploymentId));
            }

            Map marathonApp = dcosApi.getApp(appStage.getMarathonAppId());
            if (marathonApp == null) {
                throw new Exception("Not found marathon app to rollback, " + appStage.getMarathonAppId());
            }
        }

        //앱의 Old 영역을 롤백을 프로덕션으로 등록한다.
        Long snapshotOld = appStage.getSnapshotOld();
        appStage.setSnapshot(snapshotOld);
        appStage.setDeployment(appStage.getDeploymentOld());
        appStage.setMarathonAppId(appStage.getMarathonAppIdOld());
        appStage.setCommit(appStage.getCommitOld());

        appStage.setSnapshotOld(null);
        appStage.setMarathonAppIdOld(null);
        appStage.setDeploymentOld(null);
        appStage.setCommitOld(null);

        //set weight as 100
        appStage.getDeploymentStrategy().getCanary().setWeight(100);
        appWebService.setAppStage(appEntity, appStage, stage);

        //first save for snapshot restore.
        appEntity = appWebService.save(appEntity);

        //load old app snapshot, and restoreSnapshot without redeployment.
        List<String> stages = new ArrayList<>();
        stages.add(stage);
        snapshotService.restoreSnapshot(snapshotOld, stages, null, false);


        //블루그린은 그냥 히스토리를 롤백 성공으로 빼고 삭제시켜버린다.
        //remove current production.
        if (isBlueGreen) {
            try {
                dcosApi.deleteApp(currentMarathonAppId);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            //save history and finish deployment
            this.finishDeployment(appEntity, appStage, stage, DeploymentStatus.ROLLBACK_SUCCEED);
        }


        //일반 롤백시, appStage.getTempDeployment().getDeploymentId() 로 롤백을 한다.
        //  성공시 deploymentId 및 ROLLBACK_RUNNING 업데이트 하고 종료.
        //  실패시 히스토리를 실패 처리한다.
        else {
            String rollbackDeploymentId = null;
            try {
                Map deployment = dcosApi.deleteDeployment(deploymentId);
                rollbackDeploymentId = deployment.get("deploymentId").toString();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            //Rollback failed.
            if (StringUtils.isEmpty(rollbackDeploymentId)) {

                //save history and finish deployment
                this.finishDeployment(appEntity, appStage, stage, DeploymentStatus.ROLLBACK_FAILED);
            }
            //Rollback Success.
            else {
                //update tempDeployment for app and save.
                TempDeployment tempDeployment = appStage.getTempDeployment();
                tempDeployment.setStatus(DeploymentStatus.RUNNING_ROLLBACK);
                tempDeployment.setDeploymentId(rollbackDeploymentId);
                tempDeployment.setRollbackStartTime(new Date().getTime());
                appStage.setTempDeployment(tempDeployment);
                appWebService.setAppStage(appEntity, appStage, stage);
                appWebService.save(appEntity);
            }
        }
    }

    /**
     * Case. Finish app deployment which is on manual canary deployment.
     * <p>
     * 1.move field
     * 1)snapshotOld => 0
     * 2)weight => 100
     * <p>
     * 2.remove old app
     *
     * @param appName
     * @param stage
     * @throws Exception
     */
    //TODO need to kafka event
    public void finishManualCanaryDeployment(String appName, String stage) throws Exception {
        AppEntity appEntity = appWebCacheService.findOneCache(appName);

        AppStage appStage = appWebService.getAppStage(appEntity, stage);

        boolean isBlueGreen = false;
        String currentMarathonAppId = null;
        String deploymentId = null;

        if (!appStage.getDeploymentStrategy().getBluegreen()) {
            throw new Exception(String.format("Not BLUE/GREEN deployment to finish, %s, %s", appName, stage));
        }

        if (!DeploymentStatus.RUNNING.equals(appStage.getTempDeployment().getStatus())) {
            throw new Exception(String.format("Not RUNNING deployment to finish, %s, %s", appName, stage));
        }

        String deployment = appStage.getDeployment();

        //삭제할 롤백 마라톤 앱이 있는지 확인한다.
        String rollbackDeployment = null;
        String rollbackMarathonAppId = null;
        if (deployment.equals("blue")) {
            rollbackDeployment = "green";
            rollbackMarathonAppId = "/" + appName + "-green";
        } else {
            rollbackDeployment = "blue";
            rollbackMarathonAppId = "/" + appName + "-blue";
        }

        //1.move field
        appStage.setSnapshotOld(null);
        appStage.setMarathonAppIdOld(null);
        appStage.setDeploymentOld(null);
        appStage.setCommitOld(null);

        //weight 100
        appStage.getDeploymentStrategy().getCanary().setWeight(100);
        appWebService.setAppStage(appEntity, appStage, stage);
        appEntity = appWebService.save(appEntity);

        //remove old app
        try {
            dcosApi.deleteApp(rollbackMarathonAppId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //save history and finish deployment
        this.finishDeployment(appEntity, appStage, stage, DeploymentStatus.SUCCEED);
    }

    /**
     * Convert manual canary deployment which is on auto canary deployment.
     *
     * @param appName
     * @param stage
     * @throws Exception
     */
    //TODO need to block
    public void convertManualCanaryDeployment(String appName, String stage) throws Exception {
        AppEntity appEntity = appWebCacheService.findOneCache(appName);

        AppStage appStage = appWebService.getAppStage(appEntity, stage);

        if (!appStage.getDeploymentStrategy().getCanary().getActive()) {
            throw new Exception(String.format("Not Canary deployment to convert manually, %s, %s", appName, stage));
        }

        if (!DeploymentStatus.RUNNING.equals(appStage.getTempDeployment().getStatus())) {
            throw new Exception(String.format("Not RUNNING deployment to convert manually, %s, %s", appName, stage));
        }

        if (!appStage.getDeploymentStrategy().getCanary().getAuto()) {
            throw new Exception(String.format("Not Auto configuration to convert manually, %s, %s", appName, stage));
        }

        appStage.getDeploymentStrategy().getCanary().setAuto(false);
        appWebService.setAppStage(appEntity, appStage, stage);
        appWebService.save(appEntity);
    }

    /**
     * 어플리케이션의 주어진 스테이지의 마라톤 서비스를 삭제한다.
     *
     * @param appName
     * @param stage
     * @throws Exception
     */
    public void removeDeployedApp(String appName, String stage) throws Exception {
        //특정 디플로이 단계를 삭제 (removeDeployedApp) => 마라톤 어플리케이션을 삭제하는것을 의미한다.
        //미배포 상태 => 마라톤 어플리케이션이 없을 경우 미배포 상태로 정의한다.
        //메소스 앱 삭제

        //디플로이 중인 정보 삭제
        AppEntity appEntity = appWebCacheService.findOneCache(appName);
        AppStage appStage = appWebService.getAppStage(appEntity, stage);
        appStage.setTempDeployment(null);
        appWebService.setAppStage(appEntity, appStage, stage);
        appEntity = appWebService.save(appEntity);

        if (stage.equals("prod")) {
            String marathonAppId = appName + "-blue";
            try {
                dcosApi.deleteApp(marathonAppId);
            } catch (Exception ex) {
            }

            marathonAppId = appName + "-green";
            try {
                dcosApi.deleteApp(marathonAppId);
            } catch (Exception ex) {
            }
        } else {
            String marathonAppId = appName + "-" + stage;
            dcosApi.deleteApp(marathonAppId);
        }
    }

    /**
     * 디플로이먼트를 종료한다.
     *
     * @param appEntity
     * @param appStage
     * @param stage
     * @param status
     */
    public void finishDeployment(AppEntity appEntity, AppStage appStage, String stage, DeploymentStatus status) throws Exception {

        DeploymentHistoryEntity historyEntity = new DeploymentHistoryEntity(
                appEntity,
                stage,
                status
        );
        historyRepository.save(historyEntity);

        //remove tempDeployment for app and save.
        appStage.setTempDeployment(null);
        appWebService.setAppStage(appEntity, appStage, stage);
        appWebService.save(appEntity);
    }
}
