package org.uengine.cloud.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.User;
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

    /**
     * 어플리케이션의 주어진 스테이지의 마라톤 서비스를 재기동한다.
     *
     * @param appName
     * @param stage
     * @return
     * @throws Exception
     */
    public void runDeployedApp(String appName, String stage, String commit) throws Exception {

        Map data = new HashMap();
        data.put("appName", appName);
        data.put("stage", stage);
        data.put("commit", commit);

        //디플로이 백그라운드 작업 시작.
        jobScheduler.startJobImmediatly(UUID.randomUUID().toString(), "deployedApp", data);

        //앱 변경 적용됨
        this.updateAppConfigChanged(appName, stage, false);
    }

    /**
     * 프로덕션 환경의 blue,green 을 변경한다.
     *
     * @param appName
     * @throws Exception
     */
    public void rollbackDeployedApp(String appName) throws Exception {
        AppEntity appEntity = appJpaRepository.findOne(appName);

        AppStage prod = appEntity.getProd();
        String deployment = prod.getDeployment();

        //롤백을 할 수 있는 마라톤 어플이 있는지 확인한다.
        String rollbackDeployment = null;
        String rollbackMarathonAppId = null;
        String currentMarathonAppId = prod.getMarathonAppId();
        if (deployment.equals("blue")) {
            rollbackDeployment = "green";
            rollbackMarathonAppId = "/" + appName + "-green";
        } else {
            rollbackDeployment = "blue";
            rollbackMarathonAppId = "/" + appName + "-blue";
        }

        Map marathonApp = dcosApi.getApp(rollbackMarathonAppId);
        if (marathonApp == null) {
            throw new Exception("Not found marathon app to rollback, " + rollbackMarathonAppId);
        }

        //dcosApp 에 롤백을 프로덕션으로 등록한다.
        prod.setDeployment(rollbackDeployment);
        prod.setMarathonAppId(rollbackMarathonAppId);
        appEntity.setProd(prod);

        appJpaRepository.save(appEntity);

        //ci-deploy-rollback.json 을  ci-deploy-production.json 으로.
        try {
            this.copyDeployJson(appName, "rollback", "prod");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //라우터 리프레쉬
        dcosApi.refreshRoute();

        //기존 프로덕션은 삭제한다.
        try {
            dcosApi.deleteApp(currentMarathonAppId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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
    public Map getAppIncludeDeployJson(String appName) throws Exception {
        AppEntity appEntity = appAccessLevelRepository.findByName(appName);
        Map<String, Object> map = JsonUtils.convertClassToMap(appEntity);
        String[] stages = new String[]{"prod", "stg", "dev"};
        for (String stage : stages) {
            Map deployJson = this.getDeployJson(appName, stage);

            switch (stage) {
                case "dev":
                    ((Map) map.get("dev")).put("deployJson", deployJson);
                    break;
                case "stg":
                    ((Map) map.get("stg")).put("deployJson", deployJson);
                    break;
                case "prod":
                    ((Map) map.get("prod")).put("deployJson", deployJson);
                    break;
            }
        }
        return map;
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
        AppStage dev = appEntity.getDev();
        dev.setDeployJson(null);
        appEntity.setDev(dev);

        AppStage stg = appEntity.getStg();
        stg.setDeployJson(null);
        appEntity.setStg(stg);

        AppStage prod = appEntity.getProd();
        prod.setDeployJson(null);
        appEntity.setProd(prod);
        appJpaRepository.save(appEntity);

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
        AppStage dev = appEntity.getDev();
        dev.setDeployJson(null);
        appEntity.setDev(dev);

        AppStage stg = appEntity.getStg();
        stg.setDeployJson(null);
        appEntity.setStg(stg);

        AppStage prod = appEntity.getProd();
        prod.setDeployJson(null);
        appEntity.setProd(prod);

        appJpaRepository.save(appEntity);
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

    public void addAppToVcapService(String appName) throws Exception {
        AppEntity appEntity = appJpaRepository.findOne(appName);
        String[] stages = new String[]{"prod", "stg", "dev"};
        Map service = new HashMap();
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

        String applicationYml = this.getApplicationYml();
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Map vcapMap = yamlReader.readValue(applicationYml, Map.class);
        Map vcaps = (Map) ((Map) vcapMap.get("vcap")).get("services");
        vcaps.put(appName, service);

        applicationYml = yamlReader.writeValueAsString(vcapMap);
        gitlabExtentApi.updateOrCraeteRepositoryFile(
                Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId")),
                "master", "application.yml", applicationYml);
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
        appCreate.setAppName(appCreate.getAppName().toLowerCase().replaceAll(" ", "-"));

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
        //TODO 스테이지가 없으면 모든 스테이지가 변화된 것.
        AppEntity appEntity = appJpaRepository.findOne(appName);
        if (StringUtils.isEmpty(stage)) {
            appEntity.getDev().setConfigChanged(true);
            appEntity.getStg().setConfigChanged(true);
            appEntity.getProd().setConfigChanged(true);
        } else {
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

            if (isChanged) {
                appStage.setConfigChanged(true);
            } else {
                appStage.setConfigChanged(false);
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
}
