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

import org.apache.commons.io.IOUtils;
import org.gitlab4j.api.GitLabApi;
import org.uengine.cloud.app.*;
import org.uengine.cloud.deployment.DeploymentHistoryEntity;
import org.uengine.cloud.deployment.DeploymentHistoryRepository;
import org.uengine.cloud.deployment.DeploymentStatus;
import org.uengine.cloud.deployment.TempDeployment;
import org.uengine.cloud.log.AppLogAction;
import org.uengine.cloud.log.AppLogService;
import org.uengine.cloud.log.AppLogStatus;
import org.uengine.cloud.snapshot.AppSnapshot;
import org.uengine.cloud.snapshot.AppSnapshotService;
import org.uengine.cloud.strategies.DeploymentStrategy;
import org.uengine.cloud.strategies.InstanceStrategy;
import org.uengine.iam.util.ApplicationContextRegistry;
import org.uengine.iam.util.JsonUtils;
import org.uengine.iam.util.StringUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.core.env.Environment;
import org.uengine.cloud.templates.MustacheTemplateEngine;

import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

public class DeployAppJob implements Job {

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {

        // 클라이언트 잡 실행시 필요한 정보를 가져온다.
        JobDataMap map = jobExecutionContext.getMergedJobDataMap();
        String appName = map.get("appName").toString();
        String stage = map.get("stage").toString();
        Long snapshotId = (Long) map.get("snapshotId");
        boolean exchange = (boolean) map.get("exchange");
        String commit = null;
        if (map.get("commit") != null) {
            commit = map.get("commit").toString();
        }
        String name = map.get("name") != null ? map.get("name").toString() : null;
        String description = map.get("description") != null ? map.get("description").toString() : null;

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = dateFormat.format(new Date());
        if (StringUtils.isEmpty(name)) {
            name = String.format("%s %s %s Deployment", dateString, appName, stage);
        }

        Map marathonDeploymentReponse = null;

        Map log = new HashMap();
        log.put("stage", stage);
        log.put("commit", commit);

        System.out.println("Start DeployAppJob: " + appName + " : " + stage + " : " + commit);

        Environment environment = ApplicationContextRegistry.getApplicationContext().getBean(Environment.class);
        AppService appService = ApplicationContextRegistry.getApplicationContext().getBean(AppService.class);
        DcosApi dcosApi = ApplicationContextRegistry.getApplicationContext().getBean(DcosApi.class);
        AppLogService logService = ApplicationContextRegistry.getApplicationContext().getBean(AppLogService.class);
        AppJpaRepository appJpaRepository = ApplicationContextRegistry.getApplicationContext().getBean(AppJpaRepository.class);
        AppSnapshotService snapshotService = ApplicationContextRegistry.getApplicationContext().getBean(AppSnapshotService.class);
        DeploymentHistoryRepository historyRepository = ApplicationContextRegistry.getApplicationContext().getBean(DeploymentHistoryRepository.class);
        try {

            //앱 정보.
            AppEntity appEntity = appJpaRepository.findOne(appName);
            String appType = appEntity.getAppType();
            AppStage appStage = appService.getAppStage(appEntity, stage);
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
                        DeploymentStatus.RUNNING.equals(preStatus) ? DeploymentStatus.CANCELED : DeploymentStatus.RUNNING_ROLLBACK
                );
                historyRepository.save(historyEntity);
            }

            /**
             * check if blue-green
             * 1. DeploymentStrategy bluegreen equal true
             * 2. Prod stage && Current Production App is exist.
             * 3. exchange is true
             */
            boolean bluegreenDeployment = false;
            if (appStage.getDeploymentStrategy().getBluegreen()
                    && stage.equals("prod") && exchange) {
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
                appStage.getDeploymentStrategy().getCanary().setWeight(0);

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
                    marathonDeploymentReponse = dcosApi.updateApp(newMarathonAppId, deployJson);
                }
                //신규 앱이 없을 경우 신규 디플로이
                else {
                    marathonDeploymentReponse = dcosApi.createApp(deployJson);
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

                //기존 앱이 있을 경우 업데이트 디플로이
                if (marathonApp != null) {
                    marathonDeploymentReponse = dcosApi.updateApp(marathonAppId, deployJson);
                }
                //기존 앱이 없을 경우 신규 디플로이
                else {
                    marathonDeploymentReponse = dcosApi.createApp(deployJson);
                }
            }

            /**
             * 7) save app if success
             */
            //create tempDeployment
            String deploymentId = marathonDeploymentReponse.get("deploymentId").toString();
            TempDeployment tempDeployment = new TempDeployment();
            tempDeployment.setDeploymentId(deploymentId);
            tempDeployment.setName(name);
            tempDeployment.setDescription(description);
            tempDeployment.setStartTime(new Date().getTime());
            tempDeployment.setStatus(DeploymentStatus.RUNNING);
            appStage.setTempDeployment(tempDeployment);

            appEntity = appService.setAppStage(appEntity, appStage, stage);
            appEntity = appJpaRepository.save(appEntity);

            /**
             * create new snapshot
             */
            //신규 스냅샷 생성
            String snapshotName = String.format("Auto saved %s %s Snapshot", dateString, appName);
            AppSnapshot snapshot = snapshotService.createSnapshot(appName, snapshotName, null);
            if (snapshot != null) {
                appStage.setSnapshot(snapshot.getId());
                appEntity = appService.setAppStage(appEntity, appStage, stage);
                appJpaRepository.save(appEntity);
            }

            logService.addHistory(appName, AppLogAction.RUN_DEPLOYED_APP, AppLogStatus.SUCCESS, log);

        } catch (Exception ex) {
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
        AppService appService = ApplicationContextRegistry.getApplicationContext().getBean(AppService.class);
        Map deployJson = appService.getDeployJson(appName, stage);
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
            Map configMap = appService.getOriginalCloudConfigJson(appName, stage);
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
}
