package org.uengine.cloud.app;

import com.google.common.base.Joiner;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Member;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uengine.cloud.app.config.AppConfigService;
import org.uengine.cloud.app.git.GitlabExtentApi;
import org.uengine.cloud.app.marathon.DcosApi;
import org.uengine.cloud.deployment.DeploymentStrategy;
import org.uengine.cloud.deployment.InstanceStrategy;
import org.uengine.cloud.services.AppService;
import org.uengine.cloud.tenant.TenantContext;
import org.uengine.iam.client.IamClient;
import org.uengine.iam.client.model.OauthUser;
import org.uengine.iam.util.HttpUtils;
import org.uengine.iam.util.JsonUtils;
import org.uengine.iam.util.StringUtils;

import java.util.*;

/**
 * Created by uengine on 2017. 11. 16..
 */
@Service
public class AppWebCacheService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppWebCacheService.class);

    @Autowired
    private AppEntityRepository appEntityRepository;

    @Autowired
    private GitLabApi gitLabApi;

    @Autowired
    private AppWebService appWebService;

    @Cacheable(value = "app", key = "#name")
    public AppEntity findOneCache(String name) {
        LOGGER.info("find app from jdbc {}", name);
        return appEntityRepository.findOne(name);
    }

    @CachePut(value = "app", key = "#appEntity.name")
    @Transactional
    public AppEntity saveCache(AppEntity appEntity) {
        return appEntityRepository.save(appEntity);
    }

    @CacheEvict(value = "app", key = "#name")
    @Transactional
    public void deleteCache(String name) {
        appEntityRepository.delete(name);
    }

    @Cacheable(value = "appAllNames", key = "'appAllNames'")
    public List<String> findAllAppNamesCache() {
        LOGGER.info("find all app names from jdbc {}");
        return appEntityRepository.findAllAppNames();
    }


    @CachePut(value = "appAllNames", key = "'appAllNames'")
    @Transactional
    //From AppScheduler
    public List<String> updateAllAppNamesCache() {
        LOGGER.info("update all app names to redis {}");
        return appEntityRepository.findAllAppNames();
    }

    //For Marathon LB fetch job
    @Cacheable(value = "appAll", key = "'appAll'")
    public List<AppEntity> findAllAppsCache() {
        LOGGER.info("find all apps from jdbc {}");
        //return this.updateAllApps();
        return appEntityRepository.findAll();
    }


    @CachePut(value = "appAll", key = "'appAll'")
    @Transactional
    //From AppScheduler
    public List<AppEntity> updateAllAppsCache() {
        LOGGER.info("update all apps to redis {}");
        return appEntityRepository.findAll();
    }


    @Cacheable(value = "appMembers", key = "#appName + '-member'")
    public List<Member> getAppMemberCache(String appName) {
        LOGGER.info("find app member from git for app {}", appName);

        AppEntity appEntity = this.findOneCache(appName);

        try {
            int projectId = appEntity.getProjectId();
            return gitLabApi.getProjectApi().getMembers(projectId);

        } catch (Exception ex) {
            LOGGER.error("failed find members from git for app {}", appName);
            return new ArrayList<>();
        }
    }

    //TODO need from kafka event
    //TODO need redis -> websocket
    @CachePut(value = "appMembers", key = "#appName + '-member'")
    @Transactional
    public List<Member> updateAppMemberCache(String appName) {
        LOGGER.info("update app member to redis for app {}", appName);

        AppEntity appEntity = this.findOneCache(appName);

        try {
            int projectId = appEntity.getProjectId();
            List<Member> members = gitLabApi.getProjectApi().getMembers(projectId);

            //update members.
            List<String> memberIds = new ArrayList<>();
            for (int i = 0; i < members.size(); i++) {
                Member member = members.get(i);
                String memberId = "m" + member.getId() + "m";
                memberIds.add(memberId);
            }
            appEntity.setMemberIds(Joiner.on(",").join(memberIds));
            appWebService.save(appEntity);
            return members;

        } catch (Exception ex) {
            LOGGER.error("failed update members to redis for app {}", appName);
            return new ArrayList<>();
        }
    }

    //TODO need from delete app
    @CacheEvict(value = "appMembers", key = "#appName + '-member'")
    @Transactional
    public void deleteAppMemberCache(String appName) {
        LOGGER.error("remove app member from redis for app {}", appName);
    }
}
