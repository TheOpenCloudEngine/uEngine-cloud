package org.uengine.cloud.migration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.uengine.cloud.app.*;
import org.uengine.cloud.scheduler.JobScheduler;
import org.uengine.cloud.tenant.TenantContext;
import org.uengine.iam.client.IamClient;
import org.uengine.iam.client.ResourceOwnerPasswordCredentials;
import org.uengine.iam.client.TokenType;
import org.uengine.iam.client.model.OauthUser;
import org.uengine.iam.util.HttpUtils;
import org.uengine.iam.util.JsonUtils;
import org.uengine.iam.util.StringUtils;

import java.util.*;

/**
 * Created by uengine on 2017. 11. 16..
 */
@Service
public class MigrationService {
    @Autowired
    private Environment environment;

    @Autowired
    private GitLabApi gitLabApi;

    @Autowired
    private GitlabExtentApi gitlabExtentApi;

    @Autowired
    private AppService appService;

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

    public void migration() throws Exception {
        Map<String, Map> appsMap = getAppsMap();
        Set<String> keySet = appsMap.keySet();
        for (String appName : keySet) {
            Map app = appsMap.get(appName);

            AppEntity entity = new AppEntity();
            entity.setName(appName);

            entity.setAppType(app.get("appType").toString());

            int projectId = (int) ((Map) app.get("gitlab")).get("projectId");
            entity.setProjectId(projectId);

            entity.setIam(app.get("iam").toString());

            entity.setNumber((int) app.get("number"));

            String[] stages = new String[]{"dev", "stg", "prod"};
            for (String stage : stages) {
                Map stageMap = (Map) app.get(stage);
                AppStage appStage = new AppStage();
                appStage.setConfigChanged(false);
                appStage.setDeployment(stageMap.get("deployment").toString());
                appStage.setExternal(stageMap.get("external").toString());
                appStage.setInternal(stageMap.get("internal").toString());
                appStage.setMarathonAppId(stageMap.get("marathonAppId").toString());
                appStage.setServicePort((int) stageMap.get("service-port"));

                switch (stage) {
                    case "dev":
                        entity.setDev(appStage);
                        break;
                    case "stg":
                        entity.setStg(appStage);
                        break;
                    case "prod":
                        entity.setProd(appStage);
                        break;
                }
            }

            //앱 저장
            appJpaRepository.save(entity);

            //모든 앱 cloud-config-server/deploy/deploy....json 전부 CONFIG_JSON 추가.
            String[] deployStages = new String[]{"dev", "stg", "prod", "rollback"};
            for (String stage : deployStages) {
                try {
                    Map deployJson = appService.getDeployJson(appName, stage);
                    Map env = (Map) deployJson.get("env");
                    env.put("CONFIG_JSON", "{{CONFIG_JSON}}");

                    appService.updateDeployJson(appName, stage, deployJson);
                } catch (Exception ex) {

                }
            }

            //모든 앱 커밋 중지
            Map pipeLineJson = appService.getPipeLineJson(appName);
            List<String> refs = (List<String>) pipeLineJson.get("refs");
            refs = new ArrayList<String>();
            pipeLineJson.put("refs", refs);
            appService.updatePipeLineJson(appName, pipeLineJson);


            //모든 앱 ci 파일 전부 수정
            ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
            String ciFile = gitlabExtentApi.getRepositoryFile(projectId, "master", ".gitlab-ci.yml");
            Map map = yamlReader.readValue(ciFile, Map.class);
            String[] jobs = new String[]{"test", "dev", "staging", "production"};
            for (String job : jobs) {
                List<String> script = new ArrayList<>();

                switch (job) {
                    case "test":
                        script.add("curl -H \"access_token: ${ACCESS_TOKEN}\" ${UENGINE_CLOUD_URL}/gitlab/api/v4/projects/${CONFIG_REPO_ID}/repository/files/deployment%2F${APP_NAME}%2Fci-test.sh/raw?ref=master | sh");
                        break;
                    case "dev":
                        script.add("curl -H \"access_token: ${ACCESS_TOKEN}\" ${UENGINE_CLOUD_URL}/gitlab/api/v4/projects/${CONFIG_REPO_ID}/repository/files/template%2Fcommon%2Fci-deploy-dev.sh/raw?ref=master | sh");
                        break;
                    case "staging":
                        script.add("curl -H \"access_token: ${ACCESS_TOKEN}\" ${UENGINE_CLOUD_URL}/gitlab/api/v4/projects/${CONFIG_REPO_ID}/repository/files/template%2Fcommon%2Fci-deploy-staging.sh/raw?ref=master | sh");
                        break;
                    case "production":
                        script.add("curl -H \"access_token: ${ACCESS_TOKEN}\" ${UENGINE_CLOUD_URL}/gitlab/api/v4/projects/${CONFIG_REPO_ID}/repository/files/template%2Fcommon%2Fci-deploy-production.sh/raw?ref=master | sh");
                }
                Map map1 = (Map) map.get(job);
                map1.put("script", script);
            }

            String converted = yamlReader.writeValueAsString(map);
            gitlabExtentApi.updateOrCraeteRepositoryFile(projectId, "master", ".gitlab-ci.yml", converted);

            //모든 앱 커밋 원복
            refs = new ArrayList<String>();
            refs.add("master");
            pipeLineJson.put("refs", refs);
            appService.updatePipeLineJson(appName, pipeLineJson);


            //String configId = environment.getProperty("gitlab.config-repo.projectId");
            //String aaa = "curl -H \\\"access_token: ${ACCESS_TOKEN}\\\" ${UENGINE_CLOUD_URL}/gitlab/api/v4/projects/${CONFIG_REPO_ID}/repository/files/template%2Fcommon%2Fci-deploy-production.sh/raw?ref=master | sh";
        }
    }

    /**
     * dcos-apps.yml 의 데이터를 가져온다.
     *
     * @return
     * @throws Exception
     */
    public Map<String, Map> getAppsMap() throws Exception {
        String dcosYml = gitlabExtentApi.getRepositoryFile(
                Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId")),
                "master", "dcos-apps.yml"
        );
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Map map = yamlReader.readValue(dcosYml, Map.class);
        Map dcos = (Map) map.get("dcos");
        return (Map<String, Map>) dcos.get("apps");
    }
}
