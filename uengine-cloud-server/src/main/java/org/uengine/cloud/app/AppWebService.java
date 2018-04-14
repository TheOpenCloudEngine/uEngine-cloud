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
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;
import org.uengine.cloud.app.config.AppConfigService;
import org.uengine.cloud.app.git.GitlabExtentApi;
import org.uengine.cloud.app.marathon.DcosApi;
import org.uengine.cloud.deployment.DeploymentStrategy;
import org.uengine.cloud.deployment.InstanceStrategy;
import org.uengine.iam.client.IamClient;
import org.uengine.iam.client.model.OauthUser;
import org.uengine.iam.util.HttpUtils;
import org.uengine.iam.util.JsonUtils;
import org.uengine.iam.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.uengine.cloud.tenant.TenantContext;

import java.util.*;

/**
 * Created by uengine on 2017. 11. 16..
 */
@Service
public class AppWebService {
    @Autowired
    private Environment environment;

    @Autowired
    private AppEntityRepository appEntityRepository;

    @Autowired
    private DcosApi dcosApi;

    @Autowired
    private GitLabApi gitLabApi;

    @Autowired
    private AppConfigService appConfigService;

    @Autowired
    private GitlabExtentApi gitlabExtentApi;

    @Autowired
    @Lazy
    private AppAsyncService appAsyncService;

    @Autowired
    private AppWebCacheService appWebCacheService;

    public List<AppEntity> cachedAllApps = new ArrayList<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(AppWebService.class);


    //TODO need redis -> websocket
    public AppEntity save(AppEntity appEntity) throws Exception {

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

        //and sse or websocket app changed event call to ui.

        //send to redis? => websocket event call.

        //Future. vcap 서비스 업데이트
        appConfigService.addAppToVcapService(appEntity.getName());

        return save;
    }


    //TODO need from kafka event
    //if group, get group projects -> get apps by projectId
    public void updateAppMemberForGroup(int groupId) throws Exception {
        LOGGER.info("update app member to redis for group {}", groupId);
        List<Project> projects = gitLabApi.getGroupApi().getProjects(groupId);
        for (int i = 0; i < projects.size(); i++) {
            AppEntity appEntity = appEntityRepository.findByProjectId(projects.get(i).getId());
            appWebCacheService.updateAppMemberCache(appEntity.getName());
        }
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

        //vcap 서비스, config.yml 삭제
        appConfigService.removeAppToVcapService(appName);
        appConfigService.removeAppConfigYml(appName);

        //app 삭제
        appWebCacheService.deleteCache(appEntity.getName());
    }


    /**
     * 어플리케이션을 생성한다.
     *
     * @param appCreate
     * @return
     * @throws Exception
     */
    //TODO need to kafka event
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
        AppEntity existEntity = appWebCacheService.findOneCache(appCreate.getAppName());
        if (existEntity != null) {
            throw new Exception("App name is exist");
        }

        //not use cache. find number list
        List<Integer> allAppNumbers = appEntityRepository.findAllAppNumbers();

        //포트 설정
        int min = 1;
        int max = 100;
        int appNumber = 1;
        for (int i = min; i <= max; i++) {
            boolean canUse = true;
            if (!allAppNumbers.isEmpty()) {
                for (int i1 = 0; i1 < allAppNumbers.size(); i1++) {
                    Integer number = allAppNumbers.get(i1);
                    if (i == number) {
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
        gitlabExtentApi.getDockerRunnerId();


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

        //it will throw exception if transaction accident fired.
        AppEntity save = this.save(appEntity);

        //앱 생성 백그라운드 작업 시작.
        appCreate.setUser(TenantContext.getThreadLocalInstance().getUser());
        appAsyncService.createApp(appCreate);

        System.out.println("End");
        return save;
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
