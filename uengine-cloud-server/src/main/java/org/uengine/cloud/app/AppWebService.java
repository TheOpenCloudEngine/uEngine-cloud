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
    private AppWebCacheService appWebCacheService;

    @Autowired
    private CatalogService catalogService;

    @Autowired
    private AppPipeLineService pipeLineService;

    @Autowired
    private AppDeployJsonService deployJsonService;

    @Autowired
    private AppWebService appWebService;

    @Autowired
    private AppLogService logService;

    @Autowired
    private AppEntityBaseMessageHandler messageHandler;


    private static final Logger LOGGER = LoggerFactory.getLogger(AppWebService.class);

    /**
     * 앱을 생성한다.
     *
     * @param appCreate
     */
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

        //앱 찾기
        AppEntity appEntity = appEntityRepository.findOne(appCreate.getAppName());

        try {
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

            //number save
            appEntity.setNumber(appCreate.getAppNumber());

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
            try {
                gitlabExtentApi.deleteForkRelation(project.getId());
            } catch (Exception ex) {
                //resome.
                ex.printStackTrace();
            }


            //레파지토리 이름 변경, 프로젝트 이름 변경, 퍼블릭 변경
            project.setPublic(true);
            project.setVisibility(Visibility.PUBLIC);
            project.setPath(appCreate.getAppName());
            project.setName(appCreate.getAppName());
            gitLabApi.getProjectApi().updateProject(project);

            Map<String, Object> data = pipeLineService.getTriggerVariables(appCreate.getAppName());

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
                deployJsonService.updateDeployJson(appEntity.getName(), stage, deployJson);
            }

            //파이프라인 파일 복사
            String pipelineText = gitlabExtentApi.getRepositoryFile(repoId, "master", "template/common/ci-pipeline.json");
            Map pipelineJson = pipeLineService.updatePipeLineJson(appEntity.getName(), JsonUtils.unmarshal(pipelineText));

            //스크립트 파일 복사
            String[] scripFiles = new String[]{
                    "template/common/ci-deploy-production.sh",
                    "template/common/ci-deploy-staging.sh",
                    "template/common/ci-deploy-dev.sh",
                    "template/common/ci-test.sh",
            };

            for (String scripFile : scripFiles) {
                String scriptText = gitlabExtentApi.getRepositoryFile(repoId, "master", scripFile);
                String[] split = scripFile.split("/");
                String fileName = split[split.length - 1];
                gitlabExtentApi.updateOrCraeteRepositoryFile(
                        repoId, "master", "deployment/" + data.get("APP_NAME").toString() + "/" + fileName, scriptText);
            }


            //클라우드 콘피그 파일 복사
            appConfigService.createAppConfigYml(
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
            appWebService.save(appEntity);


            //파이프라인 트리거 실행.
            Map pipeline = pipeLineService.excutePipelineTrigger(appCreate.getAppName(), "master", null);
            pipelineId = (int) pipeline.get("id");

            //신규 프로젝트 맴버 캐쉬 업데이트
            appWebCacheService.updateAppMemberCache(appEntity.getName());

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
                    appConfigService.removeAppToVcapService(appCreate.getAppName());
                    appConfigService.removeAppConfigYml(appCreate.getAppName());
                } catch (Exception ee) {

                }

                //스테이터스 실패 등록
                appEntity.setCreateStatus("repository-create-failed");
                appWebCacheService.saveCache(appEntity);

                //삭제 시도에 대한 이력
                logService.addHistory(appCreate.getAppName(), AppLogAction.CREATE_APP, AppLogStatus.FAILED, log);

            } catch (Exception e) {
                e.printStackTrace();

                //삭제 시도를 실패했을 경우도 실패 이력
                logService.addHistory(appCreate.getAppName(), AppLogAction.CREATE_APP, AppLogStatus.FAILED, log);
            }
        }
    }

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

        //vcap 서비스, config.yml 삭제
        appConfigService.removeAppToVcapService(appName);
        appConfigService.removeAppConfigYml(appName);

        //app 삭제
        appWebCacheService.deleteCache(appName);

        //삭제 알림
        messageHandler.publish(AppEntityBaseMessageTopic.app, appEntity, null, null);
    }


//    /**
//     * 어플리케이션을 생성한다.
//     *
//     * @param appCreate
//     * @return
//     * @throws Exception
//     */
//    //TODO need to kafka event
////    public AppEntity createApp(AppCreate appCreate) throws Exception {
////
////    }


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
