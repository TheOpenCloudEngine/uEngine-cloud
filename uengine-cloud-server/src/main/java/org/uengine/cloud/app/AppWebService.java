package org.uengine.cloud.app;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.uengine.cloud.app.config.AppConfigService;
import org.uengine.cloud.app.deployjson.AppDeployJsonService;
import org.uengine.cloud.app.emitter.AppEntityBaseMessageHandler;
import org.uengine.cloud.app.emitter.AppEntityBaseMessageTopic;
import org.uengine.cloud.app.git.GitMirrorService;
import org.uengine.cloud.app.git.GitlabExtentApi;
import org.uengine.cloud.app.log.AppLogAction;
import org.uengine.cloud.app.log.AppLogService;
import org.uengine.cloud.app.log.AppLogStatus;
import org.uengine.cloud.app.marathon.DcosApi;
import org.uengine.cloud.app.pipeline.AppPipeLineService;
import org.uengine.cloud.catalog.CatalogService;
import org.uengine.cloud.catalog.CategoryItem;
import org.uengine.cloud.catalog.FileMapping;
import org.uengine.cloud.deployment.DeploymentStrategy;
import org.uengine.cloud.deployment.InstanceStrategy;
import org.uengine.cloud.templates.MustacheTemplateEngine;
import org.uengine.iam.client.IamClient;
import org.uengine.iam.client.model.OauthUser;
import org.uengine.iam.util.HttpUtils;
import org.uengine.iam.util.JsonUtils;
import org.uengine.iam.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.uengine.cloud.tenant.TenantContext;

import java.io.IOException;
import java.util.*;

/**
 * Created by uengine on 2017. 11. 16..
 */
@Service
public class AppWebService {
    @Autowired
    private Environment environment;

    @Autowired
    private DcosApi dcosApi;

    @Autowired
    private GitLabApi gitLabApi;

    @Autowired
    private AppConfigService appConfigService;

    @Autowired
    private AppWebCacheService appWebCacheService;

    @Autowired
    private GitMirrorService gitMirrorService;

    @Autowired
    private AppEntityBaseMessageHandler messageHandler;


    private static final Logger LOGGER = LoggerFactory.getLogger(AppWebService.class);


    public AppEntity save(AppEntity appEntity) throws Exception {
        return this.save(appEntity, false);
    }

    //TODO need redis -> sse
    public AppEntity save(AppEntity appEntity, boolean updateVcap) throws Exception {

        //if mesos status remain, remove.
        AppStage dev = this.adjustmentStage(appEntity.getDev());
        dev.setMesos(null);
        appEntity.setDev(dev);

        AppStage stg = this.adjustmentStage(appEntity.getStg());
        stg.setMesos(null);
        appEntity.setStg(stg);

        AppStage prod = this.adjustmentStage(appEntity.getProd());
        prod.setMesos(null);
        appEntity.setProd(prod);

        AppEntity save = appWebCacheService.saveCache(appEntity);

        if (updateVcap) {
            appConfigService.addAppToVcapService(appEntity.getName());
        }

        messageHandler.publish(AppEntityBaseMessageTopic.app, save, null, null);
        return save;
    }

    /**
     * 깃랩 멤버데이터에 따라 어세스 레벨을 부여한다.
     *
     * @param appEntity
     * @param oauthUser
     * @return
     */
    public AppEntity setAccessLevel(AppEntity appEntity, OauthUser oauthUser) {
        try {
            appEntity.setAccessLevel(0);
            int gitlabId = ((Long) oauthUser.getMetaData().get("gitlab-id")).intValue();
            List<Member> members = appWebCacheService.getAppMemberCache(appEntity.getName());
            for (int i = 0; i < members.size(); i++) {
                Member member = members.get(i);
                if (member.getId() == gitlabId) {
                    appEntity.setAccessLevel(member.getAccessLevel().toValue());
                }
            }
        } catch (Exception ex) {
            appEntity.setAccessLevel(0);
        }
        return appEntity;
    }

    /**
     * 어플리케이션을 삭제한다.
     *
     * @param appName
     * @param removeRepository
     * @throws Exception
     */
    //TODO need to kafka event
    public void deleteApp(String appName, boolean removeRepository) throws Exception {
        AppEntity appEntity = appWebCacheService.findOneCache(appName);

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

        //미러 프로젝트 삭제
        gitMirrorService.deleteMirrorProject(appEntity.getName());

        //vcap 서비스, config.yml 삭제
        appConfigService.removeAppToVcapService(appName);
        appConfigService.removeAppConfigYml(appName);

        //app 삭제
        appWebCacheService.deleteCache(appName);

        //삭제 알림
        messageHandler.publish(AppEntityBaseMessageTopic.app, appEntity, null, null);
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
}
