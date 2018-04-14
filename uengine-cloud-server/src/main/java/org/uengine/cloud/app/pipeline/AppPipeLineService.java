package org.uengine.cloud.app.pipeline;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.uengine.cloud.app.*;
import org.uengine.cloud.app.git.GitlabExtentApi;
import org.uengine.cloud.app.git.HookController;
import org.uengine.iam.client.IamClient;
import org.uengine.iam.client.ResourceOwnerPasswordCredentials;
import org.uengine.iam.client.TokenType;
import org.uengine.iam.client.model.OauthUser;
import org.uengine.iam.util.JsonUtils;
import org.uengine.iam.util.StringUtils;

import java.util.*;

/**
 * Created by uengine on 2017. 11. 16..
 */
@Service
public class AppPipeLineService {
    @Autowired
    private Environment environment;

    @Autowired
    private AppPipeLineJsonRepository pipeLineJsonRepository;

    @Autowired
    private AppWebCacheService appWebCacheService;

    @Autowired
    private GitlabExtentApi gitlabExtentApi;

    @Autowired
    private HookController hookController;

    @Autowired
    private IamClient iamClient;

    /**
     * 어플리케이션의 파이프라인 가동 정의 파일을 가져온다.
     *
     * @param appName
     * @return
     * @throws Exception
     */
    public Map getPipeLineJson(String appName) throws Exception {
        return pipeLineJsonRepository.findByAppName(appName).getJson();
    }

    /**
     * 앱의 파이프라인 가동 정의 파일을 삭제한다.
     *
     * @param appName
     * @throws Exception
     */
    public void removePipeLineJson(String appName) throws Exception {
        pipeLineJsonRepository.removeByAppName(appName);
    }

    /**
     * 어플리케이션의 파이프라인 가동 정의를 업데이트한다.
     *
     * @param appName
     * @param pipelineJson
     * @throws Exception
     */
    public Map updatePipeLineJson(String appName, Map pipelineJson) throws Exception {
        AppPipeLineJson pipeLineJson = pipeLineJsonRepository.findByAppName(appName);

        //insert if not exist
        if (pipeLineJson == null) {
            AppPipeLineJson appPipeLineJson = new AppPipeLineJson();
            appPipeLineJson.setAppName(appName);
            appPipeLineJson.setJson(pipelineJson);
            pipeLineJson = pipeLineJsonRepository.save(appPipeLineJson);
        }
        //update if exist
        else {
            pipeLineJson.setJson(pipelineJson);
            pipeLineJson = pipeLineJsonRepository.save(pipeLineJson);
        }
        return pipeLineJson.getJson();
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
        AppEntity appEntity = appWebCacheService.findOneCache(appName);
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
            AppEntity appEntity = appWebCacheService.findOneCache(appName);
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
}
