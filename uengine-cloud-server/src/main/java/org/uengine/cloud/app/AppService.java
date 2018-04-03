package org.uengine.cloud.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.User;
import org.uengine.cloud.deployment.DeploymentHistoryEntity;
import org.uengine.cloud.deployment.DeploymentHistoryRepository;
import org.uengine.cloud.deployment.DeploymentStatus;
import org.uengine.cloud.deployment.TempDeployment;
import org.uengine.cloud.scheduler.CronTable;
import org.uengine.cloud.snapshot.AppSnapshotService;
import org.uengine.cloud.strategies.DeploymentStrategy;
import org.uengine.cloud.strategies.InstanceStrategy;
import org.uengine.iam.client.IamClient;
import org.uengine.iam.client.ResourceOwnerPasswordCredentials;
import org.uengine.iam.client.TokenType;
import org.uengine.iam.client.model.OauthUser;
import org.uengine.iam.util.HttpUtils;
import org.uengine.iam.util.JsonUtils;
import org.uengine.iam.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.uengine.cloud.scheduler.JobScheduler;
import org.uengine.cloud.tenant.TenantContext;

import java.util.*;

/**
 * Created by uengine on 2017. 11. 16..
 */
@Service
public class AppService {
    @Autowired
    private Environment environment;

    @Autowired
    private GitLabApi gitLabApi;

    @Autowired
    private GitlabExtentApi gitlabExtentApi;

    @Autowired
    private JobScheduler jobScheduler;

    @Autowired
    private DcosApi dcosApi;

    @Autowired
    private HookController hookController;

    @Autowired
    private AppAccessLevelRepository appAccessLevelRepository;

    @Autowired
    private AppJpaRepository appJpaRepository;

    @Autowired
    private IamClient iamClient;

    @Autowired
    private CronTable cronTable;

    @Autowired
    private AppSnapshotService snapshotService;

    @Autowired
    private DeploymentHistoryRepository historyRepository;

    /**
     * 어플리케이션의 주어진 스테이지의 마라톤 서비스를 재기동한다.
     *
     * @param appName
     * @param stage
     * @return
     * @throws Exception
     */
    public void runDeployedApp(
            String appName,
            String stage,
            String commit,
            Long snapshotId,
            String name,
            String description
    ) throws Exception {

        Map data = new HashMap();
        data.put("appName", appName);
        data.put("stage", stage);
        data.put("commit", commit);
        data.put("snapshotId", snapshotId);
        data.put("name", name);
        data.put("description", description);

        //디플로이 백그라운드 작업 시작.
        jobScheduler.startJobImmediatly(UUID.randomUUID().toString(), "deployedApp", data);

        //앱 변경 적용됨
        this.updateAppConfigChanged(appName, stage, false);
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
    public void rollbackApp(String appName, String stage) throws Exception {
        AppEntity appEntity = appJpaRepository.findOne(appName);

        AppStage appStage = this.getAppStage(appEntity, stage);

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
            if (this.getMarathonDeploymentById(deploymentId) == null) {
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
        this.setAppStage(appEntity, appStage, stage);

        //first save for snapshot restore.
        appEntity = appJpaRepository.save(appEntity);

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
                this.setAppStage(appEntity, appStage, stage);
                appJpaRepository.save(appEntity);
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
        AppEntity appEntity = appJpaRepository.findOne(appName);

        AppStage appStage = this.getAppStage(appEntity, stage);

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

//        Map marathonApp = dcosApi.getApp(rollbackMarathonAppId);
//        if (marathonApp == null) {
//            throw new Exception("Not found marathon app to finish, " + rollbackMarathonAppId);
//        }

        //1.move field
        appStage.setSnapshotOld(null);
        appStage.setMarathonAppIdOld(null);
        appStage.setDeploymentOld(null);
        appStage.setCommitOld(null);

        //weight 100
        appStage.getDeploymentStrategy().getCanary().setWeight(100);
        this.setAppStage(appEntity, appStage, stage);
        appJpaRepository.save(appEntity);

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
        AppEntity appEntity = appJpaRepository.findOne(appName);

        AppStage appStage = this.getAppStage(appEntity, stage);

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
        this.setAppStage(appEntity, appStage, stage);
        appJpaRepository.save(appEntity);
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
        AppEntity appEntity = appJpaRepository.findOne(appName);
        AppStage appStage = this.getAppStage(appEntity, stage);
        appStage.setTempDeployment(null);
        this.setAppStage(appEntity, appStage, stage);
        appJpaRepository.save(appEntity);

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
     * 어플리케이션의 주어진 스테이지에 따른 배포 정보를 반환한다.
     *
     * @param appName
     * @param stage   prod,stg,dev
     * @return
     * @throws Exception
     */
    public Map getDeployJson(String appName, String stage) throws Exception {
        int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
        String deployFilePath = this.getDeployJsonPath(appName, stage);
        String content = gitlabExtentApi.getRepositoryFile(repoId, "master", deployFilePath);
        return JsonUtils.unmarshal(content);
    }

    /**
     * 어플리케이션의 주어진 스테이지 배포정보의 깃랩 레파지토리 위치를 가져온다.
     *
     * @param appName
     * @param stage
     * @return
     * @throws Exception
     */
    private String getDeployJsonPath(String appName, String stage) throws Exception {
        String deployJsonFilePath = null;
        switch (stage) {
            case "rollback":
                deployJsonFilePath = "deployment/" + appName + "/ci-deploy-rollback.json";
                break;
            case "prod":
                deployJsonFilePath = "deployment/" + appName + "/ci-deploy-production.json";
                break;
            case "stg":
                deployJsonFilePath = "deployment/" + appName + "/ci-deploy-staging.json";
                break;
            case "dev":
                deployJsonFilePath = "deployment/" + appName + "/ci-deploy-dev.json";
                break;
        }
        return deployJsonFilePath;
    }

    /**
     * 어플리케이션의 주어진 스테이지에 따른 배포 정보를 업데이트한다.
     *
     * @param appName
     * @param stage
     * @param deployJson
     * @throws Exception
     */
    public void updateDeployJson(String appName, String stage, Map deployJson) throws Exception {
        int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
        String deployFilePath = this.getDeployJsonPath(appName, stage);
        gitlabExtentApi.updateOrCraeteRepositoryFile(repoId, "master", deployFilePath, JsonUtils.marshal(deployJson));
    }

    public void copyDeployJson(String appName, String sourceStage, String targetStage) throws Exception {
        Map deployJson = this.getDeployJson(appName, sourceStage);
        this.updateDeployJson(appName, targetStage, deployJson);
    }

    /**
     * 어플리케이션의 파이프라인 가동 정의 파일을 가져온다.
     *
     * @param appName
     * @return
     * @throws Exception
     */
    public Map getPipeLineJson(String appName) throws Exception {
        int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
        String pipeLineFilePath = "deployment/" + appName + "/ci-pipeline.json";
        String content = gitlabExtentApi.getRepositoryFile(repoId, "master", pipeLineFilePath);
        return JsonUtils.unmarshal(content);
    }

    /**
     * 어플리케이션의 파이프라인 가동 정의를 업데이트한다.
     * 이때, 프로젝트의 ci 파일도 함께 업데이트된다.
     *
     * @param appName
     * @param pipelineJson
     * @throws Exception
     */
    public Map updatePipeLineJson(String appName, Map pipelineJson) throws Exception {
        int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));

        //파이프라인 정의 파일 업데이트
        String pipeLineFilePath = "deployment/" + appName + "/ci-pipeline.json";
        gitlabExtentApi.updateOrCraeteRepositoryFile(repoId, "master", pipeLineFilePath, JsonUtils.marshal(pipelineJson));
        return pipelineJson;
    }

    /**
     * 어플리케이션 dcos 정보에 deploy 정보를 합산하여 리턴한다.
     *
     * @param appName
     * @return
     * @throws Exception
     */
    public AppEntity getAppIncludeDeployJson(String appName) throws Exception {
        AppEntity appEntity = appAccessLevelRepository.findByName(appName);
        String[] stages = new String[]{"prod", "stg", "dev"};
        for (String stage : stages) {
            Map deployJson = this.getDeployJson(appName, stage);

            switch (stage) {
                case "dev":
                    AppStage dev = appEntity.getDev();
                    dev.setDeployJson(deployJson);
                    appEntity.setDev(dev);
                    break;
                case "stg":
                    AppStage stg = appEntity.getStg();
                    stg.setDeployJson(deployJson);
                    appEntity.setStg(stg);
                    break;
                case "prod":
                    AppStage prod = appEntity.getProd();
                    prod.setDeployJson(deployJson);
                    appEntity.setProd(prod);
                    break;
            }
        }

        //메소스 상태 첨부
        try {
            String prodDeployment = appEntity.getProd().getDeployment();
            String[] deployments = new String[]{"blue", "green", "stg", "dev"};
            String[] expectMarathonIds = new String[]{
                    "/" + appName + "-dev",
                    "/" + appName + "-stg",
                    "/" + appName + "-" + prodDeployment
            };
            List<Map> list = (List) ((Map) cronTable.getDcosData().get("groups")).get("apps");
            for (int i = 0; i < list.size(); i++) {
                Map marathonApp = list.get(i);
                String marathonAppId = marathonApp.get("id").toString();
                if (Arrays.asList(expectMarathonIds).contains(marathonAppId)) {

                    //타스트 관련 항목을 추가
                    Map mesos = new HashMap();
                    Set<String> keySet = marathonApp.keySet();
                    for (String key : keySet) {
                        if (key.startsWith("task") || key.startsWith("container")) {
                            mesos.put(key, marathonApp.get(key));
                        }
                    }

                    AppStage appStage = null;
                    if (marathonAppId.endsWith("-dev")) {
                        appStage = appEntity.getDev();
                        appStage.setMesos(mesos);
                        appEntity.setDev(appStage);
                    } else if (marathonAppId.endsWith("-stg")) {
                        appStage = appEntity.getStg();
                        appStage.setMesos(mesos);
                        appEntity.setStg(appStage);
                    } else if (marathonAppId.endsWith("-green") || marathonAppId.endsWith("-blue")) {
                        appStage = appEntity.getProd();
                        appStage.setMesos(mesos);
                        appEntity.setProd(appStage);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return appEntity;
    }

    /**
     * 어플리케이션의 dcos 정보 및 deploy 정보를 업데이트한다.
     *
     * @param appName
     * @param appEntity
     * @return
     * @throws Exception
     */
    public AppEntity updateAppIncludeDeployJson(String appName, AppEntity appEntity) throws Exception {

        String[] stages = new String[]{"prod", "stg", "dev"};
        for (String stage : stages) {
            Map deployJson = null;
            switch (stage) {
                case "dev":
                    deployJson = appEntity.getDev().getDeployJson();
                    break;
                case "stg":
                    deployJson = appEntity.getStg().getDeployJson();
                    break;
                case "prod":
                    deployJson = appEntity.getProd().getDeployJson();
                    break;
            }
            this.updateDeployJson(appName, stage, deployJson);
        }

        //appMap 최종 업로드
        AppStage dev = this.adjustmentStage(appEntity.getDev());
        dev.setDeployJson(null);
        dev.setMesos(null);
        appEntity.setDev(dev);

        AppStage stg = this.adjustmentStage(appEntity.getStg());
        stg.setDeployJson(null);
        stg.setMesos(null);
        appEntity.setStg(stg);

        AppStage prod = this.adjustmentStage(appEntity.getProd());
        prod.setDeployJson(null);
        prod.setMesos(null);
        appEntity.setProd(prod);
        appJpaRepository.save(appEntity);

        //vcap 서비스 업데이트
        this.addAppToVcapService(appName);

        return appEntity;
    }

    /**
     * 어플리케이션의 dcos-apps.yml 정보만 업데이트 한다.
     *
     * @param appName
     * @param appEntity
     * @return
     * @throws Exception
     */
    public AppEntity updateAppExcludeDeployJson(String appName, AppEntity appEntity) throws Exception {

        appEntity.setName(appName);

        //appMap 최종 업로드
        AppStage dev = this.adjustmentStage(appEntity.getDev());
        dev.setDeployJson(null);
        dev.setMesos(null);
        appEntity.setDev(dev);

        AppStage stg = this.adjustmentStage(appEntity.getStg());
        stg.setDeployJson(null);
        stg.setMesos(null);
        appEntity.setStg(stg);

        AppStage prod = this.adjustmentStage(appEntity.getProd());
        prod.setDeployJson(null);
        prod.setMesos(null);
        appEntity.setProd(prod);

        appJpaRepository.save(appEntity);

        //vcap 서비스 업데이트
        this.addAppToVcapService(appName);

        return appEntity;
    }

    /**
     * 어플리케이션을 삭제한다.
     *
     * @param appName
     * @param removeRepository
     * @throws Exception
     */
    public void deleteApp(String appName, boolean removeRepository) throws Exception {
        AppEntity appEntity = appJpaRepository.findOne(appName);

        //메소스 앱 삭제
        String[] stages = new String[]{"blue", "green", "stg", "dev"};
        for (String stage : stages) {
            try {
                dcosApi.deleteApp("/" + appName + "-" + stage);
            } catch (Exception ex) {

            }
        }

        //프로젝트 삭제
        try {
            int projectId = appEntity.getProjectId();
            if (projectId > 0 && removeRepository) {
                gitLabApi.getProjectApi().deleteProject(projectId);
                try {
                    //한번 더 삭제 TODO 이상함..
                    gitLabApi.getProjectApi().deleteProject(projectId);
                } catch (Exception exx) {

                }
            }
        } catch (Exception ex) {

        }

        //vcap 서비스, config.yml 삭제
        this.removeAppToVcapService(appName);
        this.removeAppConfigYml(appName);

        //app 삭제
        appJpaRepository.delete(appEntity);
    }

    /**
     * vcap 서비스에 앱의 주소를 등록한다. 요청된 스테이지에 한해 수행한다.
     *
     * @param appEntity
     * @param stages
     * @throws Exception
     */
    public void addAppToVcapService(AppEntity appEntity, List<String> stages) throws Exception {
        String applicationYml = this.getApplicationYml();
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Map vcapMap = yamlReader.readValue(applicationYml, Map.class);
        Map vcaps = (Map) ((Map) vcapMap.get("vcap")).get("services");
        Map service = new HashMap();

        //기존 vcap 서비스에 서비스가 있다면, 기존 서비스맵으로 대체한다.
        if (vcaps.containsKey(appEntity.getName())) {
            service = (Map) vcaps.get(appEntity.getName());
        }

        for (String stage : stages) {
            Map map = new HashMap();
            switch (stage) {
                case "dev":
                    map.put("external", appEntity.getDev().getExternal());
                    map.put("internal", appEntity.getDev().getInternal());
                    break;
                case "stg":
                    map.put("external", appEntity.getStg().getExternal());
                    map.put("internal", appEntity.getStg().getInternal());
                    break;
                case "prod":
                    map.put("external", appEntity.getProd().getExternal());
                    map.put("internal", appEntity.getProd().getInternal());
                    break;
            }
            service.put(stage, map);
        }

        vcaps.put(appEntity.getName(), service);

        applicationYml = yamlReader.writeValueAsString(vcapMap);
        gitlabExtentApi.updateOrCraeteRepositoryFile(
                Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId")),
                "master", "application.yml", applicationYml);
    }

    public void addAppToVcapService(String appName) throws Exception {
        String[] stages = {"dev", "stg", "prod"};
        this.addAppToVcapService(appJpaRepository.findOne(appName), Arrays.asList(stages));
    }

    public void removeAppToVcapService(String appName) throws Exception {
        String applicationYml = this.getApplicationYml();
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Map vcapMap = yamlReader.readValue(applicationYml, Map.class);
        Map vcaps = (Map) ((Map) vcapMap.get("vcap")).get("services");
        vcaps.remove(appName);

        applicationYml = yamlReader.writeValueAsString(vcapMap);
        gitlabExtentApi.updateOrCraeteRepositoryFile(
                Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId")),
                "master", "application.yml", applicationYml);
    }

    public String getApplicationYml() throws Exception {
        int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
        String filePath = "application.yml";
        return gitlabExtentApi.getRepositoryFile(repoId, "master", filePath);
    }

    public void createAppConfigYml(String appName, String common, String dev, String stg, String prod) throws Exception {
        String defaultString = "---\n" +
                "# =================================================\n" +
                "# The common configuration file will be overwritten\n" +
                "# =================================================";

        int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
        gitlabExtentApi.updateOrCraeteRepositoryFile(repoId,
                "master", appName + ".yml", common);

        gitlabExtentApi.updateOrCraeteRepositoryFile(repoId,
                "master", appName + "-dev.yml", dev);

        gitlabExtentApi.updateOrCraeteRepositoryFile(repoId,
                "master", appName + "-stg.yml", stg);

        gitlabExtentApi.updateOrCraeteRepositoryFile(repoId,
                "master", appName + "-prod.yml", prod);
    }

    public String updateAppConfigYml(String appName, String content, String stage) throws Exception {
        int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
        if (StringUtils.isEmpty(stage)) {
            gitlabExtentApi.updateOrCraeteRepositoryFile(repoId,
                    "master", appName + ".yml", content);
        } else {
            gitlabExtentApi.updateOrCraeteRepositoryFile(repoId,
                    "master", appName + "-" + stage + ".yml", content);
        }
        this.updateAppConfigChanged(appName, stage, true);
        return content;
    }

    public String getAppConfigYml(String appName, String stage) throws Exception {
        int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
        if (StringUtils.isEmpty(stage)) {
            return gitlabExtentApi.getRepositoryFile(repoId,
                    "master", appName + ".yml");
        } else {
            return gitlabExtentApi.getRepositoryFile(repoId,
                    "master", appName + "-" + stage + ".yml");
        }
    }

    public void removeAppConfigYml(String appName) throws Exception {
        int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
        gitlabExtentApi.removeRepositoryFile(repoId,
                "master", appName + ".yml");
        gitlabExtentApi.removeRepositoryFile(repoId,
                "master", appName + "-dev.yml");
        gitlabExtentApi.removeRepositoryFile(repoId,
                "master", appName + "-stg.yml");
        gitlabExtentApi.removeRepositoryFile(repoId,
                "master", appName + "-prod.yml");
    }


    /**
     * 어플리케이션을 생성한다.
     *
     * @param appCreate
     * @return
     * @throws Exception
     */
    public AppEntity createApp(AppCreate appCreate) throws Exception {

        //appName 체크
        if (StringUtils.isEmpty(appCreate.getAppName())) {
            throw new Exception("App name is empty");
        }

        //이름 강제 고정
        String appName = appCreate.getAppName().toLowerCase().replaceAll(" ", "-");
        //remove all the special characters a part of alpha numeric characters, space and hyphen.
        appName = appName.replaceAll("[^a-zA-Z0-9 -]", "");
        appCreate.setAppName(appName);

        //appName 중복 체크
        AppEntity existEntity = appJpaRepository.findOne(appCreate.getAppName());
        if (existEntity != null) {
            throw new Exception("App name is exist");
        }

        List<AppEntity> apps = appJpaRepository.findAll();

        //포트 설정
        int min = 1;
        int max = 100;
        int appNumber = 1;
        for (int i = min; i <= max; i++) {
            boolean canUse = true;
            if (!apps.isEmpty()) {
                for (int i1 = 0; i1 < apps.size(); i1++) {
                    AppEntity entity = apps.get(i1);
                    if (i == entity.getNumber()) {
                        canUse = false;
                    }
                }
            }
            if (canUse) {
                appNumber = i;
                break;
            }
        }
        int prodPort = 10010 + ((appNumber - 1) * 3) + 1;
        int stgPort = 10010 + ((appNumber - 1) * 3) + 2;
        int devPort = 10010 + ((appNumber - 1) * 3) + 3;
        String internalProdDomain = "marathon-lb-internal.marathon.mesos:" + prodPort;
        String internalStgDomain = "marathon-lb-internal.marathon.mesos:" + stgPort;
        String internalDevDomain = "marathon-lb-internal.marathon.mesos:" + devPort;
        appCreate.setAppNumber(appNumber);
        appCreate.setDevPort(devPort);
        appCreate.setStgPort(stgPort);
        appCreate.setProdPort(prodPort);
        appCreate.setInternalDevDomain(internalDevDomain);
        appCreate.setInternalStgDomain(internalStgDomain);
        appCreate.setInternalProdDomain(internalProdDomain);

        //깃랩 프로젝트 체크
        gitLabApi.unsudo();
        if (appCreate.getProjectId() > 0) {
            try {
                Project project = gitLabApi.getProjectApi().getProject(appCreate.getProjectId());
            } catch (Exception e) {
                throw new Exception(e);
            }
        }

        //러너체크
        int runnerId = gitlabExtentApi.getDockerRunnerId();


        //깃랩 사용자 정의
        String userName = TenantContext.getThreadLocalInstance().getUserId();
        IamClient iamClient = new IamClient(environment.getProperty("iam.host"),
                Integer.parseInt(environment.getProperty("iam.port")),
                environment.getProperty("iam.clientId"),
                environment.getProperty("iam.clientSecret"));

        OauthUser oauthUser = iamClient.getUser(userName);
        int gitlabId = (int) oauthUser.getMetaData().get("gitlab-id");

        //깃랩에 유저가 있는지 체크
        User existGitlabUser = gitLabApi.getUserApi().getUser(gitlabId);
        if (existGitlabUser == null) {
            throw new Exception("Not found gitlab user id: " + gitlabId);
        }

        //신규 appEntity
        AppEntity appEntity = new AppEntity();
        appEntity.setName(appCreate.getAppName());
        appEntity.setNumber(appCreate.getAppNumber());
        appEntity.setAppType(appCreate.getCategoryItemId());
        appEntity.setIam(userName);

        //prod,stg,dev
        AppStage prod = new AppStage();
        prod.setMarathonAppId("/" + appCreate.getAppName() + "-green");
        prod.setServicePort(appCreate.getProdPort());
        prod.setExternal(appCreate.getExternalProdDomain());
        prod.setInternal(appCreate.getInternalProdDomain());
        prod.setDeployment("green");
        appEntity.setProd(prod);

        AppStage stg = new AppStage();
        stg.setMarathonAppId("/" + appCreate.getAppName() + "-stg");
        stg.setServicePort(appCreate.getStgPort());
        stg.setExternal(appCreate.getExternalStgDomain());
        stg.setInternal(appCreate.getInternalStgDomain());
        stg.setDeployment("stg");
        appEntity.setStg(stg);


        AppStage dev = new AppStage();
        dev.setMarathonAppId("/" + appCreate.getAppName() + "-dev");
        dev.setServicePort(appCreate.getDevPort());
        dev.setExternal(appCreate.getExternalDevDomain());
        dev.setInternal(appCreate.getInternalDevDomain());
        dev.setDeployment("dev");
        appEntity.setDev(dev);

        //생성 상태 저장
        appEntity.setCreateStatus("repository-create");

        //콘피그 패스워드 생성
        appEntity.setConfigPassword(UUID.randomUUID().toString());

        //시큐어 콘피그 여부 저장
        appEntity.setInsecureConfig(appCreate.getInsecureConfig());

        AppEntity save = appJpaRepository.save(appEntity);


        //vcapservice 등록
        this.addAppToVcapService(appCreate.getAppName());

        //앱 생성 백그라운드 작업 시작.
        appCreate.setUser(TenantContext.getThreadLocalInstance().getUser());
        jobScheduler.startJobImmediatly(UUID.randomUUID().toString(), "appCreate", JsonUtils.convertClassToMap(appCreate));

        System.out.println("End");
        return save;
    }

    /**
     * 어플리케이션의 깃랩 파이프라인을 실행시킨다.
     *
     * @param appName
     * @param ref
     * @return
     * @throws Exception
     */
    public Map excutePipelineTrigger(String appName, String ref, String stage) throws Exception {
        AppEntity appEntity = appJpaRepository.findOne(appName);
        int projectId = appEntity.getProjectId();
        String token = gitlabExtentApi.getProjectDcosTriggerToken(projectId);

        //콘텐트 교체.
        //교체할 파라미터 셋
        Map<String, Object> variables = this.getTriggerVariables(appName);
        Map pipeline = gitlabExtentApi.triggerPipeline(projectId, token, ref, variables);
        String pipelineId = pipeline.get("id").toString();

        //예약된 스테이지가 있다면 등록
        if (!StringUtils.isEmpty(stage)) {
            String reservedStage = "";
            switch (stage) {
                case "dev":
                    reservedStage = "dev";
                    break;
                case "stg":
                    reservedStage = "staging";
                    break;
                case "prod":
                    reservedStage = "production";
                    break;
            }

            hookController.addReservedStage(pipelineId, reservedStage);
        }
        return pipeline;
    }

    /**
     * 어플리케이션의 깃랩 CI 트리거에 치환할 값을 가져온다.
     *
     * @param appName
     * @return
     */
    public Map<String, Object> getTriggerVariables(String appName) {
        //콘텐트 교체.
        //교체할 파라미터 셋
        Map<String, Object> data = new HashMap<>();
        String APP_NAME = appName;
        String CONFIG_SERVER_URL = environment.getProperty("vcap.services.uengine-cloud-config.external");
        String CONFIG_SERVER_INTERNAL_URL = environment.getProperty("vcap.services.uengine-cloud-config.internal");
        String REGISTRY_URL = environment.getProperty("registry.host");
        String UENGINE_CLOUD_URL = environment.getProperty("vcap.services.uengine-cloud-server.external");
        int CONFIG_REPO_ID = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
        String NEXUS_MVN_URL = environment.getProperty("nexus.mvn.public");
        String NEXUS_NPM_URL = environment.getProperty("nexus.npm.public");
        String PACKAGE_URL = environment.getProperty("registry.package");

        String accessToken = null;
        try {
            AppEntity appEntity = appJpaRepository.findOne(appName);
            String userName = appEntity.getIam();
            OauthUser oauthUser = iamClient.getUser(userName);
            ResourceOwnerPasswordCredentials passwordCredentials = new ResourceOwnerPasswordCredentials();
            passwordCredentials.setUsername(oauthUser.getUserName());
            passwordCredentials.setPassword(oauthUser.getUserPassword());
            passwordCredentials.setScope("cloud-server");
            passwordCredentials.setToken_type(TokenType.JWT);

            Map claim = new HashMap();
            passwordCredentials.setClaim(JsonUtils.marshal(claim));

            Map map = iamClient.accessToken(passwordCredentials);
            accessToken = map.get("access_token").toString();
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create ci Iam access_token");
        }

        //프로젝트 파라미터
        data.put("APP_NAME", APP_NAME);
        data.put("CONFIG_SERVER_URL", "http://" + CONFIG_SERVER_URL);
        data.put("CONFIG_SERVER_INTERNAL_URL", "http://" + CONFIG_SERVER_INTERNAL_URL);
        data.put("REGISTRY_URL", REGISTRY_URL);
        data.put("UENGINE_CLOUD_URL", "http://" + UENGINE_CLOUD_URL);
        data.put("CONFIG_REPO_ID", CONFIG_REPO_ID);
        data.put("ACCESS_TOKEN", accessToken);
        data.put("NEXUS_MVN_URL", NEXUS_MVN_URL);
        data.put("NEXUS_NPM_URL", NEXUS_NPM_URL);
        data.put("PACKAGE_URL", PACKAGE_URL);
        //data.put("PROFILE", stage);
        //data.put("APPLICATION_NAME", appName);

        try {
            System.out.println(data.get("APP_NAME").toString());
            System.out.println(data.get("CONFIG_SERVER_URL").toString());
            System.out.println(data.get("CONFIG_SERVER_INTERNAL_URL").toString());
            System.out.println(data.get("REGISTRY_URL").toString());
            System.out.println(data.get("UENGINE_CLOUD_URL").toString());
            System.out.println(data.get("CONFIG_REPO_ID").toString());
            System.out.println(data.get("ACCESS_TOKEN").toString());
            System.out.println(data.get("NEXUS_MVN_URL").toString());
            System.out.println(data.get("NEXUS_NPM_URL").toString());
            System.out.println(data.get("PACKAGE_URL").toString());
        } catch (Exception ex) {

        }
        return data;
    }


    public void updateAppConfigChanged(String appName, String stage, boolean isChanged) throws Exception {
        //스테이지가 없으면 모든 스테이지가 변화된 것.
        AppEntity appEntity = appJpaRepository.findOne(appName);
        if (StringUtils.isEmpty(stage)) {
            AppStage dev = appEntity.getDev();
            dev.setConfigChanged(true);

            AppStage stg = appEntity.getStg();
            stg.setConfigChanged(true);

            AppStage prod = appEntity.getProd();
            prod.setConfigChanged(true);

            appEntity.setDev(dev);
            appEntity.setStg(stg);
            appEntity.setProd(prod);
        } else {
            AppStage appStage = null;
            switch (stage) {
                case "dev":
                    appStage = appEntity.getDev();
                    if (isChanged) {
                        appStage.setConfigChanged(true);
                    } else {
                        appStage.setConfigChanged(false);
                    }
                    appEntity.setDev(appStage);
                    break;
                case "stg":
                    appStage = appEntity.getStg();
                    if (isChanged) {
                        appStage.setConfigChanged(true);
                    } else {
                        appStage.setConfigChanged(false);
                    }
                    appEntity.setStg(appStage);
                    break;
                case "prod":
                    appStage = appEntity.getProd();
                    if (isChanged) {
                        appStage.setConfigChanged(true);
                    } else {
                        appStage.setConfigChanged(false);
                    }
                    appEntity.setProd(appStage);
                    break;
            }
        }
        this.updateAppExcludeDeployJson(appName, appEntity);
    }

    public Map getOriginalCloudConfigJson(String appName, String stage) throws Exception {
        HttpResponse response = new HttpUtils().makeRequest("GET",
                "http://" + environment.getProperty("vcap.services.uengine-cloud-config.external") + "/" + appName + "-" + stage + ".json",
                null,
                new HashMap<>()
        );
        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity);
        return JsonUtils.unmarshal(json);
    }

    public Map getAppRegistryTags(String appName) throws Exception {
        String registryHost = environment.getProperty("registry.public-host");
        if (StringUtils.isEmpty(registryHost)) {
            registryHost = environment.getProperty("registry.host");
        }
        HttpResponse res = new HttpUtils().makeRequest("GET",
                "http://" + registryHost + "/v2/" + appName + "/tags/list",
                null,
                new HashMap<>()
        );
        HttpEntity entity = res.getEntity();
        String json = EntityUtils.toString(entity);
        return JsonUtils.unmarshal(json);
    }

    /**
     * appStage 를 룰에 마추어 저장한다.
     * 룰 - 배포전략
     *
     * @param appStage
     * @return
     */
    public AppStage adjustmentStage(AppStage appStage) {
        DeploymentStrategy deploymentStrategy = appStage.getDeploymentStrategy();
        InstanceStrategy instanceStrategy = deploymentStrategy.getInstanceStrategy();
        if (InstanceStrategy.RECREATE.equals(instanceStrategy)) {
            deploymentStrategy.setBluegreen(false);
            deploymentStrategy.getCanary().setActive(false);
            deploymentStrategy.getAbtest().setActive(false);
        } else if (InstanceStrategy.RAMP.equals(instanceStrategy)) {
            deploymentStrategy.setBluegreen(false);
            deploymentStrategy.getCanary().setActive(false);
            deploymentStrategy.getAbtest().setActive(false);
        } else if (InstanceStrategy.CANARY.equals(instanceStrategy)) {
            deploymentStrategy.setBluegreen(true);
            deploymentStrategy.getCanary().setActive(true);
            deploymentStrategy.getAbtest().setActive(false);
        } else if (InstanceStrategy.ABTEST.equals(instanceStrategy)) {
            deploymentStrategy.setBluegreen(true);
            deploymentStrategy.getCanary().setActive(true);
            deploymentStrategy.getAbtest().setActive(true);
        }
        return appStage;
    }

    public AppStage getAppStage(AppEntity appEntity, String stage) {
        AppStage appStage = null;
        switch (stage) {
            case "dev":
                appStage = appEntity.getDev();
                break;
            case "stg":
                appStage = appEntity.getStg();
                break;
            case "prod":
                appStage = appEntity.getProd();
                break;
        }
        return appStage;
    }

    public AppEntity setAppStage(AppEntity appEntity, AppStage appStage, String stage) {
        switch (stage) {
            case "dev":
                appEntity.setDev(appStage);
                break;
            case "stg":
                appEntity.setStg(appStage);
                break;
            case "prod":
                appEntity.setProd(appStage);
                break;
        }
        return appEntity;
    }

    public Map getMarathonDeploymentById(String deploymentId) {
        if (cronTable.getDcosData().get("deployments") == null) {
            return null;
        }
        List<Map> deployments = (List<Map>) cronTable.getDcosData().get("deployments");

        for (Map deployment : deployments) {
            if (deployment.get("id").toString().equals(deploymentId)) {
                return deployment;
            }
        }
        return null;
    }

    public void finishDeployment(AppEntity appEntity, AppStage appStage, String stage, DeploymentStatus status) {

        DeploymentHistoryEntity historyEntity = new DeploymentHistoryEntity(
                appEntity,
                stage,
                status
        );
        historyRepository.save(historyEntity);

        //remove tempDeployment for app and save.
        appStage.setTempDeployment(null);
        this.setAppStage(appEntity, appStage, stage);
        appJpaRepository.save(appEntity);
    }
}
