package org.uengine.cloud.scheduler;

import org.apache.commons.lang.StringEscapeUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.ProjectHook;
import org.gitlab4j.api.models.User;
import org.gitlab4j.api.models.Visibility;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.uengine.cloud.app.*;
import org.uengine.cloud.catalog.CatalogService;
import org.uengine.cloud.catalog.CategoryItem;
import org.uengine.cloud.catalog.FileMapping;
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
import org.uengine.cloud.templates.MustacheTemplateEngine;
import org.uengine.iam.client.IamClient;
import org.uengine.iam.client.model.OauthUser;
import org.uengine.iam.util.ApplicationContextRegistry;
import org.uengine.iam.util.JsonUtils;
import org.uengine.iam.util.StringUtils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class AppAsyncService {

    @Autowired
    private Environment environment;

    @Autowired
    private AppService appService;

    @Autowired
    private GitLabApi gitLabApi;

    @Autowired
    private GitlabExtentApi gitlabExtentApi;

    @Autowired
    private AppLogService logService;

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private AppJpaRepository appJpaRepository;

    @Autowired
    private DeploymentHistoryRepository historyRepository;

    @Autowired
    private DcosApi dcosApi;

    @Autowired
    private AppSnapshotService snapshotService;

    @Async
    public void createApp(AppCreate appCreate) {
        // 클라이언트 잡 실행시 필요한 정보를 가져온다.
        int pipelineId = 0;
        int projectId = 0;

        Map<String, Object> log = null;
        try {
            log = JsonUtils.convertClassToMap(appCreate);
        } catch (IOException ex) {
            log = new HashMap<>();
        }

        try {
            //DCOS 앱 찾기
            AppEntity appEntity = appJpaRepository.findOne(appCreate.getAppName());

            //iamUserName 은 IAM 유저네임
            String iamUserName = appEntity.getIam();


            //IAM 유저정보(패스워드 포함)
            IamClient iamClient = new IamClient(environment.getProperty("iam.host"),
                    Integer.parseInt(environment.getProperty("iam.port")),
                    environment.getProperty("iam.clientId"),
                    environment.getProperty("iam.clientSecret"));
            OauthUser oauthUser = iamClient.getUser(iamUserName);

            //깃랩 유저
            int gitlabId = (int) oauthUser.getMetaData().get("gitlab-id");
            User gitlabUser = gitLabApi.getUserApi().getUser(gitlabId);

            //네임스페이스 구하기
            String namespace = gitlabUser.getUsername();
            if (!StringUtils.isEmpty(appCreate.getNamespace())) {
                namespace = appCreate.getNamespace();
            }

            //프로젝트 포크

            //템플릿 프로젝트 정보얻기.
            CategoryItem categoryItem = catalogService.getCategoryItemWithFiles(appCreate.getCategoryItemId());

            //포크하기
            Map forkProject = gitlabExtentApi.forkProject(categoryItem.getProjectId(), namespace);


            projectId = (int) forkProject.get("id");
            Project project = gitLabApi.getProjectApi().getProject(projectId);

            //포크릴레이션 삭제
            gitlabExtentApi.deleteForkRelation(project.getId());

            //레파지토리 이름 변경, 프로젝트 이름 변경, 퍼블릭 변경
            project.setPublic(true);
            project.setVisibility(Visibility.PUBLIC);
            project.setPath(appCreate.getAppName());
            project.setName(appCreate.getAppName());
            gitLabApi.getProjectApi().updateProject(project);

            Map<String, Object> data = appService.getTriggerVariables(appCreate.getAppName());

            //template 특유의 정보가 있으면 전달함
            if (appCreate.getTemplateSpecific() != null && appCreate.getTemplateSpecific().getData() != null)
                data.put("templateSpecificData", appCreate.getTemplateSpecific().getData());


            //mapping 파일들의 콘텐트를 교체한다.
            List<FileMapping> mappings = categoryItem.getMappings();

            //template 특유의 파일 목록이 있으면 파일 생성 목록에 추가해줌
            if (appCreate.getTemplateSpecific() != null && appCreate.getTemplateSpecific().getFileMappings() != null)
                mappings.addAll(appCreate.getTemplateSpecific().getFileMappings());

            Map<String, String> templateFileCache = new HashMap<>();

            MustacheTemplateEngine templateEngine = new MustacheTemplateEngine();
            for (FileMapping mapping : mappings) {

                Map<String, Object> templateData = data;

                String path = mapping.getPath();
                String file = mapping.getFile();

                //override if there are template data specified in each file mapping
                if (mapping.getData() != null) {
                    templateData = new HashMap<String, Object>();
                    templateData.putAll(data);
                    templateData.put("templateSpecificData", mapping.getData());

                    if (templateFileCache.containsKey(file))
                        file = templateFileCache.get(file);
                    else {
                        String templateFile = gitlabExtentApi.getRepositoryFile(categoryItem.getProjectId(), "master", "template/file/" + file);
                        templateFileCache.put(file, templateFile);
                        file = templateFile;
                    }
                }

                String body = templateEngine.executeTemplateText(file, templateData);

                body = StringEscapeUtils.unescapeHtml(body);

                gitlabExtentApi.updateOrCraeteRepositoryFile(projectId, "master", path, body);
            }


            //러너 등록. 웹훅 등록. 트리거 등록. 마라톤 데피니션 및 스크립트 파일들 복사. 파이프라인 실행. 스테이터스 변경
            //러너 등록.
            int dockerRunnerId = gitlabExtentApi.getDockerRunnerId();
            gitlabExtentApi.enableRunner(projectId, dockerRunnerId);

            //웹훅 등록
            ProjectHook hook = new ProjectHook();
            hook.setPushEvents(true);
            hook.setPipelineEvents(true);
            hook.setEnableSslVerification(false);
            gitLabApi.getProjectApi().addHook(projectId, data.get("UENGINE_CLOUD_URL").toString() + "/hook", hook, false, null);

            //트리거 등록
            gitlabExtentApi.createTrigger(projectId, gitlabUser.getUsername(), "dcosTrigger");

            //마라톤 디플로이 명세 파일 복사
            int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
            String[] stages = new String[]{"dev", "stg", "prod"};

            //생성시 결정한 시스템 자원을 반영한다.
            for (String stage : stages) {
                Map deployJson = null;
                String fileName = null;
                switch (stage) {
                    case "prod":
                        deployJson = JsonUtils.unmarshal(categoryItem.getDeployProd());
                        fileName = "ci-deploy-production.json";
                        break;
                    case "dev":
                        deployJson = JsonUtils.unmarshal(categoryItem.getDeployDev());
                        fileName = "ci-deploy-dev.json";
                        break;
                    case "stg":
                        deployJson = JsonUtils.unmarshal(categoryItem.getDeployStg());
                        fileName = "ci-deploy-staging.json";
                        break;
                }
                deployJson.put("cpus", appCreate.getCpu());
                deployJson.put("mem", appCreate.getMem());
                deployJson.put("instances", appCreate.getInstances());
                gitlabExtentApi.updateOrCraeteRepositoryFile(
                        repoId, "master",
                        "deployment/" + data.get("APP_NAME").toString() + "/" + fileName, JsonUtils.marshal(deployJson));
            }

            //스크립트 및 파이프라인 파일 복사
            String[] scripFiles = new String[]{
                    "template/common/ci-deploy-production.sh",
                    "template/common/ci-deploy-staging.sh",
                    "template/common/ci-deploy-dev.sh",
                    "template/common/ci-test.sh",
                    "template/common/ci-pipeline.json"
            };

            for (String scripFile : scripFiles) {
                String scriptText = gitlabExtentApi.getRepositoryFile(repoId, "master", scripFile);
                String[] split = scripFile.split("/");
                String fileName = split[split.length - 1];
                gitlabExtentApi.updateOrCraeteRepositoryFile(
                        repoId, "master", "deployment/" + data.get("APP_NAME").toString() + "/" + fileName, scriptText);
            }


            //클라우드 콘피그 파일 복사
            appService.createAppConfigYml(
                    appCreate.getAppName(),
                    categoryItem.getConfig(),
                    categoryItem.getConfigDev(),
                    categoryItem.getConfigStg(),
                    categoryItem.getConfigProd()
            );

            //dcos projectId 업데이트
            appEntity.setProjectId(projectId);

            //스테이터스 변경
            appEntity.setCreateStatus("repository-create-success");
            appJpaRepository.save(appEntity);


            //파이프라인 트리거 실행.
            Map pipeline = appService.excutePipelineTrigger(appCreate.getAppName(), "master", null);
            pipelineId = (int) pipeline.get("id");


            System.out.println("end");

            logService.addHistory(appCreate.getAppName(), AppLogAction.CREATE_APP, AppLogStatus.SUCCESS, log);

        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                //파이프라인이 있다면 파이프라인 중지.
                if (pipelineId > 0) {
                    gitLabApi.getPipelineApi().cancelPipelineJobs(projectId, pipelineId);
                }

                //프로젝트 삭제
                if (projectId > 0) {
                    gitLabApi.getProjectApi().deleteProject(projectId);
                    //한번 더 삭제 TODO 이상함..
                    try {
                        gitLabApi.getProjectApi().deleteProject(projectId);
                    } catch (Exception exx) {

                    }
                }

                //vcap 서비스, config.yml 삭제
                try {
                    appService.removeAppToVcapService(appCreate.getAppName());
                    appService.removeAppConfigYml(appCreate.getAppName());
                } catch (Exception ee) {

                }

                //스테이터스 실패 등록
                //deployment/create.json 변경.
                Map createMap = new HashMap();
                createMap.put("status", "repository-create-failed");
                createMap.put("definition", JsonUtils.convertClassToMap(appCreate));
                gitlabExtentApi.updateOrCraeteRepositoryFile(
                        Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId")),
                        "master", "deployment/" + appCreate.getAppName() + "/create.json", JsonUtils.marshal(createMap)
                );

                //삭제 시도에 대한 이력
                logService.addHistory(appCreate.getAppName(), AppLogAction.CREATE_APP, AppLogStatus.FAILED, log);

            } catch (Exception e) {
                e.printStackTrace();

                //삭제 시도를 실패했을 경우도 실패 이력
                logService.addHistory(appCreate.getAppName(), AppLogAction.CREATE_APP, AppLogStatus.FAILED, log);
            }
        }
    }

    @Async
    public void deployApp(String appName, String stage, String commit, Long snapshotId, String name, String description) {

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
                    appService.adjustmentStage(appStage);
                }
            }

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
