package org.uengine.cloud.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;
import org.opencloudengine.garuda.util.HttpUtils;
import org.opencloudengine.garuda.util.JsonUtils;
import org.opencloudengine.garuda.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.uengine.cloud.scheduler.CronTable;
import org.uengine.cloud.scheduler.JobScheduler;
import org.uengine.cloud.tenant.TenantContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by uengine on 2017. 11. 16..
 */
@Service
public class AppService {
    @Autowired
    Environment environment;

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
    private CronTable cronTable;

    public Map getAppCreateStatus(String appName) throws Exception {
        int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
        String createFilePath = "deployment/" + appName + "/create.json";
        String content = gitlabExtentApi.getRepositoryFile(repoId, "master", createFilePath);
        return JsonUtils.unmarshal(content);
    }

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
    }

    /**
     * 프로덕션 환경의 blue,green 을 변경한다.
     *
     * @param appName
     * @throws Exception
     */
    public void rollbackDeployedApp(String appName) throws Exception {
        Map app = this.getAppByName(appName);
        Map prod = (Map) app.get("prod");
        String deployment = prod.get("deployment").toString();

        //롤백을 할 수 있는 마라톤 어플이 있는지 확인한다.
        String rollbackDeployment = null;
        String rollbackMarathonAppId = null;
        String currentMarathonAppId = prod.get("marathonAppId").toString();
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
        prod.put("deployment", rollbackDeployment);
        prod.put("marathonAppId", rollbackMarathonAppId);

        Map dcosMap = this.getDcosMap();
        Map apps = (Map) ((Map) dcosMap.get("dcos")).get("apps");
        apps.put(appName, app);
        this.saveDcosYaml(dcosMap);

        //ci-deploy-rollback.json 을  ci-deploy-production.json 으로.
        try {
            this.copyDeployJson(appName, "rollback", "prod");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        //라우터 리프레쉬
        dcosApi.refreshRouter();

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
    public Map getAppIncludDeployJson(String appName) throws Exception {
        Map app = this.getAppByName(appName);
        String[] stages = new String[]{"prod", "stg", "dev"};
        for (String stage : stages) {
            Map deployJson = this.getDeployJson(appName, stage);
            ((Map) app.get(stage)).put("deploy-json", deployJson);
        }
        return app;
    }

    /**
     * 어플리케이션의 dcos 정보 및 deploy 정보를 업데이트한다.
     *
     * @param appName
     * @param appMap
     * @return
     * @throws Exception
     */
    public Map updateAppIncludDeployJson(String appName, Map appMap) throws Exception {
        String copy = JsonUtils.marshal(appMap);

        String[] stages = new String[]{"prod", "stg", "dev"};
        for (String stage : stages) {
            Map stageMap = ((Map) appMap.get(stage));
            Map deployJson = (Map) stageMap.get("deploy-json");
            this.updateDeployJson(appName, stage, deployJson);
            stageMap.remove("deploy-json");
        }

        //appMap 최종 업로드
        Map dcosMap = this.getDcosMap();
        Map apps = (Map) ((Map) dcosMap.get("dcos")).get("apps");
        apps.put(appName, appMap);
        this.saveDcosYaml(dcosMap);

        return JsonUtils.unmarshal(copy);
    }

    /**
     * 어플리케이션을 삭제한다.
     *
     * @param appName
     * @param removeRepository
     * @throws Exception
     */
    public void deleteApp(String appName, boolean removeRepository) throws Exception {
        Map app = this.getAppByName(appName);

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
            int projectId = Integer.parseInt(((Map) app.get("gitlab")).get("projectId").toString());
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


        //dcos app 삭제
        Map dcosMap = this.getDcosMap();
        Map apps = (Map) ((Map) dcosMap.get("dcos")).get("apps");
        apps.remove(appName);
        if (apps.isEmpty()) {
            ((Map) dcosMap.get("dcos")).put("apps", "");
        }
        this.saveDcosYaml(dcosMap);
    }

    public void addAppToVcapService(String appName) throws Exception {
        Map app = this.getAppByName(appName);
        String[] stages = new String[]{"prod", "stg", "dev"};
        Map service = new HashMap();
        for (String stage : stages) {
            Map map = new HashMap();
            map.put("external", ((Map) app.get(stage)).get("external").toString());
            map.put("internal", ((Map) app.get(stage)).get("internal").toString());
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

    public void createAppConfigYml(String appName) throws Exception {
        int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
        String json = "{\"aaa\":{\"bbb\":{\"ccc\":\"Hello\"}}}";
        Map map = JsonUtils.unmarshal(json);

        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        String content = yamlReader.writeValueAsString(map);

        gitlabExtentApi.updateOrCraeteRepositoryFile(repoId,
                "master", appName + "-dev.yml", content);

        gitlabExtentApi.updateOrCraeteRepositoryFile(repoId,
                "master", appName + "-stg.yml", content);

        gitlabExtentApi.updateOrCraeteRepositoryFile(repoId,
                "master", appName + "-prod.yml", content);
    }

    public String updateAppConfigYml(String appName, String content, String stage) throws Exception {
        int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
        gitlabExtentApi.updateOrCraeteRepositoryFile(repoId,
                "master", appName + "-" + stage + ".yml", content);
        return content;
    }

    public String getAppConfigYml(String appName, String stage) throws Exception {
        int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
        return gitlabExtentApi.getRepositoryFile(repoId,
                "master", appName + "-" + stage + ".yml");
    }

    public void removeAppConfigYml(String appName) throws Exception {
        int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
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
    public Map createApp(AppCreate appCreate) throws Exception {

        //appName 체크
        if (StringUtils.isEmpty(appCreate.getAppName())) {
            throw new Exception("App name is empty");
        }
        appCreate.setAppName(appCreate.getAppName().toLowerCase().replaceAll(" ", "-"));

        //appName 중복 체크
        Map<String, Map> apps = this.getApps();
        if (!apps.isEmpty()) {
            for (Map.Entry<String, Map> entry : apps.entrySet()) {
                if (entry.getKey().equals(appCreate.getAppName())) {
                    throw new Exception("App name is exist");
                }
            }
        }

        //TODO 깃랩 프로젝트 appname 체크

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

        //app 생성

        //doos app 파일 받기
        Map dcosMap = this.getDcosMap();

        //깃랩 사용자 정의
        String userId = TenantContext.getThreadLocalInstance().getUserId();
        String gitlabUsername = userId.split("@")[0];
        String gitlabName = TenantContext.getThreadLocalInstance().getUser().get("name").toString();

        //신규 app 맵
        Map app = new HashMap();
        app.put("number", appCreate.getAppNumber());
        app.put("appType", appCreate.getCategoryItemId());
        app.put("iam", userId);
        app.put("owner", gitlabUsername);


        app.put("gitlab", new HashMap<>());
        Map gitMap = (Map) app.get("gitlab");
        gitMap.put("projectId", null);

        //prod,stg,dev
        Map<String, Object> prod = new HashMap<>();
        prod.put("commit-sha", null);
        prod.put("active", true);
        prod.put("marathonAppId", "/" + appCreate.getAppName() + "-green");
        prod.put("service-port", appCreate.getProdPort());
        prod.put("external", appCreate.getExternalProdDomain());
        prod.put("internal", appCreate.getInternalProdDomain());
        prod.put("deployment", "green");
        app.put("prod", prod);

        Map<String, Object> stg = new HashMap<>();
        stg.put("commit-sha", null);
        stg.put("active", true);
        stg.put("marathonAppId", "/" + appCreate.getAppName() + "-stg");
        stg.put("service-port", appCreate.getStgPort());
        stg.put("external", appCreate.getExternalStgDomain());
        stg.put("internal", appCreate.getInternalStgDomain());
        stg.put("deployment", "stg");
        app.put("stg", stg);

        Map<String, Object> dev = new HashMap<>();
        dev.put("commit-sha", null);
        dev.put("active", true);
        dev.put("marathonAppId", "/" + appCreate.getAppName() + "-dev");
        dev.put("service-port", appCreate.getDevPort());
        dev.put("external", appCreate.getExternalDevDomain());
        dev.put("internal", appCreate.getInternalDevDomain());
        dev.put("deployment", "dev");
        app.put("dev", dev);

        apps.put(appCreate.getAppName(), app);
        ((Map) dcosMap.get("dcos")).put("apps", apps);
        this.saveDcosYaml(dcosMap);

        //deployment/create.json 생성.
        //후에 초기 마라톤 명세서 작성시 리소스 분배 정보가 담겨있으니 템플릿 변환에 참조하도록 한다.
        Map createMap = new HashMap();
        createMap.put("status", "repository-create");
        createMap.put("definition", JsonUtils.convertClassToMap(appCreate));
        gitlabExtentApi.updateOrCraeteRepositoryFile(
                Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId")),
                "master", "deployment/" + appCreate.getAppName() + "/create.json", JsonUtils.marshal(createMap)
        );

        //vcapservice 등록
        this.addAppToVcapService(appCreate.getAppName());


        //<appName>-dev,stg,prod.yml 생성
        this.createAppConfigYml(appCreate.getAppName());


        //앱 생성 백그라운드 작업 시작.
        appCreate.setUser(TenantContext.getThreadLocalInstance().getUser());
        jobScheduler.startJobImmediatly(UUID.randomUUID().toString(), "appCreate", JsonUtils.convertClassToMap(appCreate));

        System.out.println("End");
        return app;
    }

    /**
     * 모든 어플리케이션을 가져온다.
     *
     * @return
     * @throws Exception
     */
    public Map<String, Map> getApps() throws Exception {
//        String configUrl = environment.getProperty("spring.cloud.config.uri");
//        String url = configUrl + "/" + "dcos-apps.json";
//        HttpResponse httpResponse = new HttpUtils().makeRequest("GET", url, null, new HashMap<>());
//        HttpEntity entity = httpResponse.getEntity();
//        String json = EntityUtils.toString(entity);

        //Map map = JsonUtils.marshal(json);


        Map dcosData = cronTable.getDcosData();
        Map map = (Map) dcosData.get("devopsApps");
        Map dcos = (Map) map.get("dcos");
        try {
            return (Map) dcos.get("apps");
        } catch (Exception ex) {
            return new HashMap<>();
        }
    }

    /**
     * 주어진 이름에 해당하는 어플리케이션을 가져온다.
     *
     * @param appName
     * @return
     * @throws Exception
     */
    public Map getAppByName(String appName) throws Exception {
        Map<String, Map> apps = this.getApps();
        Map app = null;
        if (!apps.isEmpty()) {
            for (Map.Entry<String, Map> entry : apps.entrySet()) {
                if (entry.getKey().equals(appName)) {
                    app = entry.getValue();
                }
            }
        }
        return app;
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
        Map app = this.getAppByName(appName);
        int projectId = (int) ((Map) app.get("gitlab")).get("projectId");
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

        //프로젝트 파라미터
        data.put("APP_NAME", APP_NAME);
        data.put("CONFIG_SERVER_URL", "http://" + CONFIG_SERVER_URL);
        data.put("CONFIG_SERVER_INTERNAL_URL", "http://" + CONFIG_SERVER_INTERNAL_URL);
        data.put("REGISTRY_URL", REGISTRY_URL);
        data.put("UENGINE_CLOUD_URL", "http://" + UENGINE_CLOUD_URL);
        data.put("CONFIG_REPO_ID", CONFIG_REPO_ID);
        //data.put("PROFILE", stage);
        data.put("APPLICATION_NAME", appName);
        return data;
    }

    /**
     * dcos-apps.yml 의 데이터를 가져온다.
     *
     * @return
     * @throws Exception
     */
    public Map getDcosMap() throws Exception {
        String dcosYml = gitlabExtentApi.getRepositoryFile(
                Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId")),
                "master", "dcos-apps.yml"
        );
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        return yamlReader.readValue(dcosYml, Map.class);
    }

    /**
     * dcos-apps.yml 을 업데이트한다.
     *
     * @param dcosMap
     * @throws Exception
     */
    public void saveDcosYaml(Map dcosMap) throws Exception {
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        String dcosYaml = yamlReader.writeValueAsString(dcosMap);

        gitlabExtentApi.updateOrCraeteRepositoryFile(
                Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId")),
                "master", "dcos-apps.yml", dcosYaml);
    }
}
