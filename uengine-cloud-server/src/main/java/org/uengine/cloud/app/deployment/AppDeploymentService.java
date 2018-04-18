package org.uengine.cloud.app.deployment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.uengine.cloud.app.*;
import org.uengine.cloud.app.config.AppConfigService;
import org.uengine.cloud.app.deployjson.AppDeployJsonService;
import org.uengine.cloud.app.log.AppLogAction;
import org.uengine.cloud.app.log.AppLogService;
import org.uengine.cloud.app.log.AppLogStatus;
import org.uengine.cloud.app.marathon.DcosApi;
import org.uengine.cloud.app.marathon.MarathonService;
import org.uengine.cloud.app.snapshot.AppSnapshot;
import org.uengine.cloud.app.snapshot.AppSnapshotService;
import org.uengine.cloud.deployment.*;
import org.uengine.cloud.templates.MustacheTemplateEngine;
import org.uengine.iam.util.ApplicationContextRegistry;
import org.uengine.iam.util.JsonUtils;
import org.uengine.iam.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by uengine on 2017. 11. 16..
 */
@Service
public class AppDeploymentService {

    @Autowired
    private AppWebService appWebService;

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

    @Autowired
    private AppEntityRepository appEntityRepository;

    @Autowired
    private AppDeployJsonService deployJsonService;

    @Autowired
    private AppLogService logService;

    /**
     * synchronous dcos api need for deployment job
     */
    @Autowired
    private DcosApi dcosApi;

    private static final Logger LOGGER = LoggerFactory.getLogger(AppDeploymentService.class);


    /**
     * 어플리케이션의 주어진 스테이지에 앱을 디플로이한다.
     * @param appName
     * @param stage
     * @param commit
     * @param snapshotId
     * @param name
     * @param description
     */
    public void deployApp(String appName, String stage, String commit, Long snapshotId, String name, String description) {

        LOGGER.info("deployApp started {}", appName);

        // 클라이언트 잡 실행시 필요한 정보를 가져온다.
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = dateFormat.format(new Date());
        if (StringUtils.isEmpty(name)) {
            name = String.format("%s %s %s Deployment", dateString, appName, stage);
        }

        Map marathonDeploymentResponse = null;

        Map log = new HashMap();
        log.put("stage", stage);
        log.put("commit", commit);

        System.out.println("Start DeployAppJob: " + appName + " : " + stage + " : " + commit);
        try {

            //앱 정보.
            //not user cache.
            AppEntity appEntity = appEntityRepository.findOne(appName);
            String appType = appEntity.getAppType();
            AppStage appStage = appWebService.getAppStage(appEntity, stage);
            int servicePort = appStage.getServicePort();
            String externalUrl = appStage.getExternal();
            String deployment = appStage.getDeployment();

            /**
             * check if pre-deployment is running. If running, cancel deployment.
             */
            DeploymentStatus preStatus = appStage.getTempDeployment().getStatus();
            if (DeploymentStatus.RUNNING.equals(preStatus) || DeploymentStatus.RUNNING_ROLLBACK.equals(preStatus)) {
                DeploymentHistoryEntity historyEntity = new DeploymentHistoryEntity(
                        appEntity,
                        stage,
                        DeploymentStatus.CANCELED
                );
                historyRepository.save(historyEntity);
            }

            /**
             * check if blue-green
             * 1. DeploymentStrategy bluegreen equal true
             * 2. Prod stage && Current Production App is exist.
             */
            boolean bluegreenDeployment = false;
            if (appStage.getDeploymentStrategy().getBluegreen()) {
                String currentDeployment = deployment;
                String currentMarathonAppId = appName + "-" + currentDeployment;
                try {
                    Map marathonApp = dcosApi.getApp(currentMarathonAppId);
                    if (marathonApp != null) {
                        bluegreenDeployment = true;
                    }
                } catch (Exception ex) {

                }
            }

            //블루그린 디플로이인 경우
            if (bluegreenDeployment) {
                String newMarathonAppId = null;
                String newDeployment = null;

                //현재 blue,green 상태와 반대되는 이름으로 생성
                String oldDeployment = deployment;
                String oldMarathonAppId = appName + "-" + oldDeployment;

                if (oldDeployment.equals("blue")) {
                    newMarathonAppId = appName + "-green";
                    newDeployment = "green";
                } else {
                    newMarathonAppId = appName + "-blue";
                    newDeployment = "blue";
                }

                //기존 마라톤 앱 가져오기(현재 프로덕션)
                Map oldMarathonApp = null;
                try {
                    oldMarathonApp = dcosApi.getApp(oldMarathonAppId);
                } catch (Exception ex) {

                }
                //신규 마라톤 앱 가져오기
                Map newMarathonApp = null;
                try {
                    newMarathonApp = dcosApi.getApp(newMarathonAppId);
                } catch (Exception ex) {

                }

                //도커 이미지 이름 가져오기
                String dockerImage = this.getDockerImage(commit, appName, oldMarathonApp);
                String deployJson = this.createDeployJson(
                        appName,
                        stage,
                        newMarathonAppId,
                        dockerImage,
                        servicePort,
                        newDeployment,
                        externalUrl,
                        appEntity,
                        appStage.getDeploymentStrategy()
                );

                /**
                 * move field
                 1)snapshot number => snapshotOld
                 */
                appStage.setSnapshotOld(appStage.getSnapshot());


                /**
                 * 3)deployment => deploymentOld
                 4)marathonAppId => marathonAppIdOld
                 5)weight => 0
                 commit = > commitOld
                 */
                appStage.setDeployment(newDeployment);
                appStage.setDeploymentOld(oldDeployment);
                appStage.setMarathonAppId("/" + newMarathonAppId);
                appStage.setMarathonAppIdOld("/" + oldMarathonAppId);

                //if auto, set weight 0
                if (appStage.getDeploymentStrategy().getCanary().getAuto()) {
                    appStage.getDeploymentStrategy().getCanary().setWeight(0);
                }

                if (oldMarathonApp != null) {
                    appStage.setCommitOld(this.getCommitRefFromMarathonApp(oldMarathonApp));
                } else {
                    appStage.setCommitOld(appStage.getCommit());
                }

                appStage.setCommit(dockerImage.split(":")[2]);

                /**
                 * 6) create or update app with current env resources.
                 */
                //신규 앱이 있을 경우 업데이트 디플로이
                if (newMarathonApp != null) {
                    marathonDeploymentResponse = dcosApi.updateApp(newMarathonAppId, deployJson);
                }
                //신규 앱이 없을 경우 신규 디플로이
                else {
                    marathonDeploymentResponse = dcosApi.createApp(deployJson);
                }
            }

            //일반 배포인 경우
            else {
                String marathonAppId = appName + "-" + deployment;
                Map marathonApp = null;
                try {
                    marathonApp = dcosApi.getApp(marathonAppId);
                } catch (Exception ex) {

                }
                String dockerImage = this.getDockerImage(commit, appName, marathonApp);
                String deployJson = this.createDeployJson(
                        appName,
                        stage,
                        marathonAppId,
                        dockerImage,
                        servicePort,
                        deployment,
                        externalUrl,
                        appEntity,
                        appStage.getDeploymentStrategy()
                );

                /**
                 * move field
                 1) snapshot number => snapshotOld
                 */
                appStage.setSnapshotOld(appStage.getSnapshot());


                /**
                 * move field
                 1)commit => commitOld
                 */
                appStage.setDeploymentOld(appStage.getDeployment());
                appStage.setMarathonAppIdOld(appStage.getMarathonAppId());
                if (marathonApp != null) {
                    appStage.setCommitOld(this.getCommitRefFromMarathonApp(marathonApp));
                } else {
                    appStage.setCommitOld(appStage.getCommit());
                }

                appStage.setCommit(dockerImage.split(":")[2]);

                //기존 앱이 있을 경우 업데이트 디플로이
                if (marathonApp != null) {
                    marathonDeploymentResponse = dcosApi.updateApp(marathonAppId, deployJson);
                }
                //기존 앱이 없을 경우 신규 디플로이
                else {
                    marathonDeploymentResponse = dcosApi.createApp(deployJson);
                }
            }

            /**
             * 7) save app if success
             */
            //create tempDeployment

            String deploymentId = null;
            //new app create;
            if (marathonDeploymentResponse.containsKey("deployments")) {
                List<Map> deployments = (List<Map>) marathonDeploymentResponse.get("deployments");
                deploymentId = deployments.get(0).get("id").toString();
            } else {
                deploymentId = marathonDeploymentResponse.get("deploymentId").toString();
            }
            TempDeployment tempDeployment = new TempDeployment();
            tempDeployment.setDeploymentId(deploymentId);
            tempDeployment.setName(name);
            tempDeployment.setDescription(description);
            tempDeployment.setStartTime(new Date().getTime());
            tempDeployment.setStatus(DeploymentStatus.RUNNING);
            tempDeployment.setCommit(appStage.getCommit());
            tempDeployment.setCommitOld(appStage.getCommitOld());
            appStage.setTempDeployment(tempDeployment);

            /**
             * if not bluegreen, and strategy is CANARY or ABTEST, force set RECREATE.
             */
            if (!bluegreenDeployment) {
                InstanceStrategy strategy = appStage.getDeploymentStrategy().getInstanceStrategy();
                if (InstanceStrategy.CANARY.equals(strategy) || InstanceStrategy.ABTEST.equals(strategy)) {
                    appStage.getDeploymentStrategy().setInstanceStrategy(InstanceStrategy.RECREATE);
                    appWebService.adjustmentStage(appStage);
                }
            }

            appEntity = appWebService.setAppStage(appEntity, appStage, stage);
            appEntity = appWebService.save(appEntity);

            //앱 변경 적용됨
            appConfigService.updateAppConfigChanged(appName, stage, false);

            /**
             * create new snapshot
             */
            //신규 스냅샷 생성
            String snapshotName = String.format("Auto saved %s %s Snapshot", dateString, appName);
            AppSnapshot snapshot = snapshotService.createSnapshot(appName, snapshotName, null);
            if (snapshot != null) {
                appStage.setSnapshot(snapshot.getId());
                appEntity = appWebService.setAppStage(appEntity, appStage, stage);
                appWebService.save(appEntity);
            }

            logService.addHistory(appName, AppLogAction.RUN_DEPLOYED_APP, AppLogStatus.SUCCESS, log);

        } catch (Exception ex) {
            LOGGER.error("deployApp failed {}", appName);

            ex.printStackTrace();
            logService.addHistory(appName, AppLogAction.RUN_DEPLOYED_APP, AppLogStatus.FAILED, log);
        }
    }

    private String getDockerImage(String commit, String appName, Map marathonApp) throws Exception {
        String dockerImage = null;
        //커밋이 없을경우 기존 마라톤 어플리케이션에서 이미지명 추출.
        if (commit == null) {
            if (marathonApp == null) {
                throw new Exception("Not found marathon app");
            }
            Map container = (Map) ((Map) marathonApp.get("app")).get("container");
            dockerImage = ((Map) container.get("docker")).get("image").toString();
        }
        //커밋이 있을경우 이미지 지정
        else {
            Environment environment = ApplicationContextRegistry.getApplicationContext().getBean(Environment.class);
            dockerImage = environment.getProperty("registry.host") + "/" + appName + ":" + commit;
        }
        return dockerImage;
    }

    public String getCommitRefFromMarathonApp(Map marathonApp) throws Exception {
        Map container = (Map) ((Map) marathonApp.get("app")).get("container");
        String dockerImage = ((Map) container.get("docker")).get("image").toString();
        return dockerImage.split(":")[2];
    }

    /**
     * 마라톤 디플로이 json 을 치환하여 반환한다.
     *
     * @param appName
     * @param stage
     * @param marathonAppId
     * @param dockerImage
     * @param servicePort
     * @param deployment
     * @param externalUrl
     * @return
     * @throws Exception
     */
    private String createDeployJson(
            String appName,
            String stage,
            String marathonAppId,
            String dockerImage,
            int servicePort,
            String deployment,
            String externalUrl,
            AppEntity appEntity,
            DeploymentStrategy deploymentStrategy
    ) throws Exception {
        Map deployJson = deployJsonService.getDeployJson(appName, stage);
        Map data = new HashMap();

        //APP_ID 는 디플로이 json 파일에 이미 "/" 처리가 되어있다.
        data.put("APP_ID", marathonAppId);
        data.put("IMAGE", dockerImage);
        data.put("SERVICE_PORT", servicePort);
        data.put("DEPLOYMENT", deployment);
        data.put("EXTERNAL_URL", externalUrl);
        data.put("PROFILE", stage);
        data.put("APP_NAME", appName);

        String configJson = null;
        try {
            Map configMap = appConfigService.getOriginalCloudConfigJson(appName, stage);
            configJson = JsonUtils.marshal(configMap);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create cloud config env set");
        }

        String deployJsonString = JsonUtils.marshal(deployJson);
        MustacheTemplateEngine templateEngine = new MustacheTemplateEngine();
        Map unmarshal = JsonUtils.unmarshal(templateEngine.executeTemplateText(deployJsonString, data));

        //port mapping
        List<Map> existPortMappings = (List<Map>) ((Map) unmarshal.get("container")).get("portMappings");
        List<Map> portMappings = new ArrayList<>();

        //main port
        Map mainPortMapping = new HashMap();
        if (existPortMappings.size() > 0) {
            mainPortMapping = JsonUtils.convertClassToMap(existPortMappings.get(0));
            mainPortMapping.put("servicePort", servicePort);
        } else {
            mainPortMapping.put("containerPort", 8080);
            mainPortMapping.put("hostPort", 0);
            mainPortMapping.put("servicePort", servicePort);
            mainPortMapping.put("protocol", "tcp");
        }
        for (int i = 0; i < portMappings.size(); i++) {
            if (portMappings.get(i).containsKey("servicePort")) {
                portMappings.get(i).put("servicePort", servicePort);
            }
        }

        //debug port
        Map debugPortMapping = new HashMap();
        debugPortMapping.put("containerPort", 8001);
        debugPortMapping.put("hostPort", 0);
        //Not important
        debugPortMapping.put("servicePort", servicePort + 20000);
        debugPortMapping.put("protocol", "tcp");

        existPortMappings.clear();
        existPortMappings.add(mainPortMapping);
        existPortMappings.add(debugPortMapping);

        //config json
        Map env = (Map) unmarshal.get("env");
        env.put("CONFIG_JSON", configJson);

        //pinpoint agent
        Environment environment = ApplicationContextRegistry.getApplicationContext().getBean(Environment.class);
        String AGENT_USE = environment.getProperty("pinpoint.use");
        String AGENT_PATH = environment.getProperty("pinpoint.agent-path");
        env.put("AGENT_USE", AGENT_USE);
        env.put("AGENT_PATH", AGENT_PATH);

        //pinpoint agent volume (if exist)
        if ("true".equals(AGENT_USE)) {
            Map container = (Map) unmarshal.get("container");
            if (!container.containsKey("volumes")) {
                container.put("volumes", new ArrayList<>());
            }
            ArrayList volumes = (ArrayList) container.get("volumes");
            Map agentPathMap = new HashMap();
            agentPathMap.put("containerPath", AGENT_PATH);
            agentPathMap.put("hostPath", AGENT_PATH);
            agentPathMap.put("mode", "RW");
            volumes.add(agentPathMap);
        }

        //elk labels
        Map docker = (Map) ((Map) unmarshal.get("container")).get("docker");
        ArrayList parameters = (ArrayList) docker.get("parameters");
        Map<String, String> labels = new HashMap();
        labels.put("APP_NAME", appEntity.getName());
        labels.put("APP_TYPE", appEntity.getAppType());
        labels.put("PROFILE", stage);
        labels.put("DEPLOYMENT", deployment);
        labels.put("IAM", appEntity.getIam());
        //TODO 커스텀 라벨 주입

        List<String> keys = new ArrayList<>(labels.keySet());
        for (String key : keys) {
            Map keyMap = new HashMap();
            keyMap.put("key", "label");
            keyMap.put("value", key + "=" + labels.get(key));
            parameters.add(keyMap);
        }

        //DeploymentStrategy
        Map upgradeStrategy = new HashMap();
        InstanceStrategy instanceStrategy = deploymentStrategy.getInstanceStrategy();
        if (InstanceStrategy.RAMP.equals(instanceStrategy)) {
            upgradeStrategy.put("maximumOverCapacity", deploymentStrategy.getRamp().getMaximumOverCapacity());
            upgradeStrategy.put("minimumHealthCapacity", 1);
        } else {
            upgradeStrategy.put("maximumOverCapacity", 1);
            upgradeStrategy.put("minimumHealthCapacity", 0);
        }
        unmarshal.put("upgradeStrategy", upgradeStrategy);

        return JsonUtils.marshal(unmarshal);
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

        LOGGER.info("rollbackApp stated {}", appName);

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
    public void finishManualCanaryDeployment(String appName, String stage) throws Exception {
        LOGGER.info("finishManualCanaryDeployment stated {}", appName);

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
    public void convertManualCanaryDeployment(String appName, String stage) throws Exception {

        LOGGER.info("convertManualCanaryDeployment stated {}", appName);

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

        LOGGER.info("removeDeployedApp stated {}", appName);

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
