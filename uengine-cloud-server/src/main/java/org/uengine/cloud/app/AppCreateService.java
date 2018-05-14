package org.uengine.cloud.app;

import org.apache.commons.lang.StringEscapeUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.uengine.cloud.app.config.AppConfigService;
import org.uengine.cloud.app.deployjson.AppDeployJsonService;
import org.uengine.cloud.app.git.GitMirrorService;
import org.uengine.cloud.app.git.GithubExtentApi;
import org.uengine.cloud.app.git.GitlabExtentApi;
import org.uengine.cloud.app.log.AppLogAction;
import org.uengine.cloud.app.log.AppLogService;
import org.uengine.cloud.app.log.AppLogStatus;
import org.uengine.cloud.app.marathon.DcosApi;
import org.uengine.cloud.app.pipeline.AppPipeLineService;
import org.uengine.cloud.catalog.CatalogService;
import org.uengine.cloud.catalog.CategoryItem;
import org.uengine.cloud.catalog.FileMapping;
import org.uengine.cloud.templates.MustacheTemplateEngine;
import org.uengine.cloud.tenant.TenantContext;
import org.uengine.iam.client.IamClient;
import org.uengine.iam.client.model.OauthUser;
import org.uengine.iam.util.JsonUtils;
import org.uengine.iam.util.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AppCreateService {

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
    private GithubExtentApi githubExtentApi;

    @Autowired
    private AppKafkaService appKafkaService;

    @Autowired
    private GitMirrorService gitMirrorService;

    private static final Logger LOGGER = LoggerFactory.getLogger(AppCreateService.class);

    public AppEntity initCreateApp(AppCreate appCreate) throws Exception {
        String userName = TenantContext.getThreadLocalInstance().getUserId();
        IamClient iamClient = new IamClient(environment.getProperty("iam.host"),
                Integer.parseInt(environment.getProperty("iam.port")),
                environment.getProperty("iam.clientId"),
                environment.getProperty("iam.clientSecret"));

        OauthUser oauthUser = iamClient.getUser(userName);
        Assert.notNull(oauthUser.getMetaData().get("gitlab-id"), "gitlab-id required.");
        int gitlabId = (int) oauthUser.getMetaData().get("gitlab-id");

        //깃랩에 유저가 있는지 체크
        User existGitlabUser = gitLabApi.getUserApi().getUser(gitlabId);
        Assert.notNull(existGitlabUser, "Not found gitlab user id: " + gitlabId);

        //user save
        appCreate.setUser(TenantContext.getThreadLocalInstance().getUser());

        //appName 체크
        Assert.notNull(appCreate.getAppName(), "App name is required");

        //이름 강제 고정
        String appName = appCreate.getAppName().toLowerCase().replaceAll(" ", "-");
        //remove all the special characters a part of alpha numeric characters, space and hyphen.
        appName = appName.replaceAll("[^a-zA-Z0-9 -]", "");
        appCreate.setAppName(appName);

        //appName 중복 체크
        AppEntity existEntity = appWebCacheService.findOneCache(appCreate.getAppName());
        Assert.isNull(existEntity, "App name is exist");

        //러너체크
        gitlabExtentApi.getDockerRunnerId();


        //repoType is must gitlab or github
        //if repoType gitlab
        if (!"github".equals(appCreate.getRepoType())) {
            appCreate.setRepoType("gitlab");
            appCreate.setAppManageMethod(AppManageMethod.CREATE_NEW_IN_GITLAB);
        }
        //if repoType github
        else {
            if (appCreate.getGithubRepoId() != null) {
                appCreate.setAppManageMethod(AppManageMethod.MANAGE_EXISTING_GITHUB_PROJECT);
            } else {
                appCreate.setAppManageMethod(AppManageMethod.CREATE_NEW_IN_GITHUB);
            }
        }

        AppManageMethod manageMethod = appCreate.getAppManageMethod();
        LOGGER.info("AppCreate manageMethod is {}", manageMethod.toString());

        //if github project, check user has githubToken and alive.
        String githubToken = null;
        if (AppManageMethod.MANAGE_EXISTING_GITHUB_PROJECT.equals(manageMethod) ||
                AppManageMethod.CREATE_NEW_IN_GITHUB.equals(manageMethod)) {
            Assert.notNull(oauthUser.getMetaData().get("githubToken"), "githubToken required.");
            githubToken = (String) oauthUser.getMetaData().get("githubToken");

            org.eclipse.egit.github.core.User githubUser = githubExtentApi.getUser(githubToken);
            Assert.notNull(githubUser, "Invalid github token.");
        }

        //if mange existing github project, check user has admin permission.
        if (AppManageMethod.MANAGE_EXISTING_GITHUB_PROJECT.equals(manageMethod)) {
            //set catalog is null
            appCreate.setTemplateSpecific(null);
            appCreate.setCategoryItemId(null);

            //check access level about repository. (pull need.) => get repository, and check permissions.admin is true
            Map repository = githubExtentApi.getRepositoryById(githubToken, appCreate.getGithubRepoId());
            Assert.notNull(repository, "Github repository not found");

            boolean isAdmin = (boolean) ((Map) repository.get("permissions")).get("admin");
            Assert.isTrue(isAdmin, "User does not have permissions for github repository.");
        }

        //if create new in gitlab or create new in github, check just user is alive described in previous logic.


        //신규 appEntity
        AppEntity appEntity = new AppEntity();
        appEntity.setName(appCreate.getAppName());
        appEntity.setAppType(appCreate.getCategoryItemId());
        appEntity.setIam(userName);
        appEntity.setRepoType(appCreate.getRepoType());
        appEntity.setGithubRepoId(appCreate.getGithubRepoId());

        //생성 상태 저장
        appEntity.setCreateStatus("repository-create");

        //콘피그 패스워드 생성
        appEntity.setConfigPassword(UUID.randomUUID().toString());

        //초기 멤버 유저 저장
        appEntity.setMemberIds("m" + gitlabId + "m");

        //it will throw exception if transaction accident fired.
        AppEntity save = appWebCacheService.saveCache(appEntity);

        //send to kafka for next step.
        appKafkaService.createAppSend(appCreate);

        return save;
    }

    /**
     * 템플릿 프로젝트를 생성한다.
     *
     * @param appCreate
     * @param appEntity
     * @param triggerVariables
     * @return
     * @throws Exception
     */
    public Project createTemplateProject(
            Namespace namespace,
            AppCreate appCreate,
            AppEntity appEntity,
            Map<String, Object> triggerVariables) throws Exception {
        //프로젝트 포크
        //템플릿 프로젝트 정보얻기.
        CategoryItem categoryItem = catalogService.getCategoryItemWithFiles(appCreate.getCategoryItemId());

        //포크하기
        Project project = gitLabApi.getProjectApi().forkProject(categoryItem.getProjectId(), namespace.getPath());
        Integer projectId = project.getId();

        //appEntity 프로젝트 아이디 반영
        appEntity.setProjectId(projectId);

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
        project.setPath(appCreate.getRepositoryName());
        project.setName(appCreate.getRepositoryName());
        gitLabApi.getProjectApi().updateProject(project);


        //template 특유의 정보가 있으면 전달함
        if (appCreate.getTemplateSpecific() != null && appCreate.getTemplateSpecific().getData() != null)
            triggerVariables.put("templateSpecificData", appCreate.getTemplateSpecific().getData());


        //mapping 파일들의 콘텐트를 교체한다.
        List<FileMapping> mappings = categoryItem.getMappings();

        //template 특유의 파일 목록이 있으면 파일 생성 목록에 추가해줌
        if (appCreate.getTemplateSpecific() != null && appCreate.getTemplateSpecific().getFileMappings() != null)
            mappings.addAll(appCreate.getTemplateSpecific().getFileMappings());

        Map<String, String> templateFileCache = new HashMap<>();

        MustacheTemplateEngine templateEngine = new MustacheTemplateEngine();
        for (FileMapping mapping : mappings) {

            Map<String, Object> templateData = triggerVariables;

            String path = mapping.getPath();
            String file = mapping.getFile();

            //override if there are template data specified in each file mapping
            if (mapping.getData() != null) {
                templateData = new HashMap<String, Object>();
                templateData.putAll(triggerVariables);
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

        //마라톤 디플로이 명세 파일 복사 - 생성시 결정한 시스템 자원을 반영한다.
        String[] stages = new String[]{"dev", "stg", "prod"};
        for (String stage : stages) {
            Map deployJson = null;
            switch (stage) {
                case "prod":
                    deployJson = JsonUtils.unmarshal(categoryItem.getDeployProd());
                    break;
                case "dev":
                    deployJson = JsonUtils.unmarshal(categoryItem.getDeployDev());
                    break;
                case "stg":
                    deployJson = JsonUtils.unmarshal(categoryItem.getDeployStg());
                    break;
            }
            deployJson.put("cpus", appCreate.getCpu());
            deployJson.put("mem", appCreate.getMem());
            deployJson.put("instances", appCreate.getInstances());
            deployJsonService.updateDeployJson(appEntity.getName(), stage, deployJson);
        }

        //클라우드 콘피그 파일 복사
        appConfigService.createAppConfigYml(
                appCreate.getAppName(),
                categoryItem.getConfig(),
                categoryItem.getConfigDev(),
                categoryItem.getConfigStg(),
                categoryItem.getConfigProd()
        );

        return project;
    }

    /**
     * 빈 프로젝트를 생성한다.
     *
     * @param appCreate
     * @return
     */
    public Project createEmptyProject(
            Namespace namespace,
            AppCreate appCreate,
            AppEntity appEntity) throws Exception {

        appCreate.setImportGitUrl(null);
        return this.createImportGitUrlProject(namespace, appCreate, appEntity);
    }

    public Project createImportGitUrlProject(Namespace namespace,
                                             AppCreate appCreate,
                                             AppEntity appEntity) throws Exception {
        Project projectToBe = new Project();
        projectToBe.setPublic(true);
        projectToBe.setVisibility(Visibility.PUBLIC);
        projectToBe.setPath(appCreate.getRepositoryName());
        projectToBe.setName(appCreate.getRepositoryName());
        projectToBe.setNamespace(namespace);

        Project project = null;
        if (StringUtils.isEmpty(appCreate.getImportGitUrl())) {
            project = gitLabApi.getProjectApi().createProject(projectToBe);
        } else {
            project = gitLabApi.getProjectApi().createProject(projectToBe, appCreate.getImportGitUrl());
        }
        Integer projectId = project.getId();

        //appEntity 프로젝트 아이디 반영
        appEntity.setProjectId(projectId);


        //마라톤 디플로이 명세 파일 복사 - 생성시 결정한 시스템 자원을 반영한다.
        int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
        String deployJsonText = gitlabExtentApi.getRepositoryFile(repoId, "master", "template/common/ci-deploy.json");
        Map deployJson = JsonUtils.marshal(deployJsonText);

        String[] stages = new String[]{"dev", "stg", "prod"};
        for (String stage : stages) {
            Map<String, Object> copyJson = JsonUtils.convertClassToMap(deployJson);
            copyJson.put("cpus", appCreate.getCpu());
            copyJson.put("mem", appCreate.getMem());
            copyJson.put("instances", appCreate.getInstances());
            deployJsonService.updateDeployJson(appEntity.getName(), stage, copyJson);
        }

        //클라우드 콘피그 파일 생성
        appConfigService.createDefaultAppConfigYml(appCreate.getAppName());
        return project;
    }

    /**
     * 앱을 생성한다. (from kafka)
     *
     * @param appCreate
     */
    public void performAppCreation(AppCreate appCreate) {

        //repository name 미 설정시 앱 이름으로 대체.
        if (StringUtils.isEmpty(appCreate.getRepositoryName())) {
            appCreate.setRepositoryName(appCreate.getAppName());
        }

        // 클라이언트 잡 실행시 필요한 정보를 가져온다.
        int pipelineId = 0;

        Map<String, Object> log = null;
        try {
            log = JsonUtils.convertClassToMap(appCreate);
        } catch (IOException ex) {
            log = new HashMap<>();
        }

        //앱 찾기
        AppEntity appEntity = appEntityRepository.findOne(appCreate.getAppName());

        //return if already performed.
        if ("repository-create-success".equals(appEntity.getCreateStatus())
                || "repository-create-failed".equals(appEntity.getCreateStatus())) {
            return;
        }

        try {
            appEntity = this.bindUniquePortForAppEntity(appCreate, appEntity);

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

            //트리거 값 구하기.
            Map<String, Object> triggerVariables = pipeLineService.getTriggerVariables(appCreate.getAppName());

            //프로젝트 생성하기, appEntity projectId insert.
            int projectId = 0;


            Namespace gitlabNamespace = null;
            //if gitlab project and use namespace, set gitlabNamespace as group namespace.
            if ("gitlab".equals(appCreate.getRepoType()) && !StringUtils.isEmpty(appCreate.getNamespace())) {
                String namespace = appCreate.getNamespace();
                List<Namespace> namespaces = gitLabApi.getNamespaceApi().findNamespaces(namespace);
                for (Namespace space : namespaces) {
                    if (space.getPath().equals(namespace)) {
                        gitlabNamespace = space;
                    }
                }
            }
            //else, set gitlabNamespace as gitlab user namespace
            else {
                String username = gitlabUser.getUsername();
                List<Namespace> namespaces = gitLabApi.getNamespaceApi().findNamespaces(username);
                for (Namespace space : namespaces) {
                    if (space.getPath().equals(username)) {
                        gitlabNamespace = space;
                    }
                }
            }

            //if Manage existing github project, createEmptyProject
            if (AppManageMethod.MANAGE_EXISTING_GITHUB_PROJECT.equals(appCreate.getAppManageMethod())) {
                Project project = this.createEmptyProject(gitlabNamespace, appCreate, appEntity);
                projectId = project.getId();
            }
            //if has importUrl, createImportGitUrlProject
            else if (!StringUtils.isEmpty(appCreate.getImportGitUrl())) {
                Project project = this.createImportGitUrlProject(gitlabNamespace, appCreate, appEntity);
                projectId = project.getId();
            }
            //if has catalogItemId, createTemplateProject
            else if (!StringUtils.isEmpty(appCreate.getCategoryItemId())) {
                Project project = this.createTemplateProject(gitlabNamespace, appCreate, appEntity, triggerVariables);
                projectId = project.getId();
            }
            //else create empty project.
            else {
                Project project = this.createEmptyProject(gitlabNamespace, appCreate, appEntity);
                projectId = project.getId();
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
            gitLabApi.getProjectApi().addHook(projectId, triggerVariables.get("UENGINE_CLOUD_URL").toString() + "/hook", hook, false, null);

            //트리거 등록
            gitlabExtentApi.createTrigger(projectId, gitlabUser.getUsername(), "dcosTrigger");


            //파이프라인 파일 복사
            int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
            String pipelineText = gitlabExtentApi.getRepositoryFile(repoId, "master", "template/common/ci-pipeline.json");
            pipeLineService.updatePipeLineJson(appEntity.getName(), JsonUtils.unmarshal(pipelineText));


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
                        repoId, "master", "deployment/" + triggerVariables.get("APP_NAME").toString() + "/" + fileName, scriptText);
            }

            //스테이터스 변경
            appEntity.setCreateStatus("repository-create-success");
            appWebService.save(appEntity);


            //if Create new in gitlab, excutePipelineTrigger Immediately.
            if (AppManageMethod.CREATE_NEW_IN_GITLAB.equals(appCreate.getAppManageMethod())) {
                Map pipeline = pipeLineService.excutePipelineTrigger(appCreate.getAppName(), "master", null);
                pipelineId = (int) pipeline.get("id");
            }
            //if Create new in github, 'createNewGithubProject'
            else if (AppManageMethod.CREATE_NEW_IN_GITHUB.equals(appCreate.getAppManageMethod())) {
                gitMirrorService.createNewGithubProject(
                        appEntity,
                        oauthUser,
                        triggerVariables,
                        appCreate.getNamespace(),
                        appCreate.getRepositoryName());
            }
            //if Manage existing github project, 'manageExistGithubProject'
            else if (AppManageMethod.MANAGE_EXISTING_GITHUB_PROJECT.equals(appCreate.getAppManageMethod())) {
                gitMirrorService.manageExistGithubProject(
                        appEntity,
                        oauthUser,
                        triggerVariables,
                        appCreate.getGithubRepoId());
            }

            //신규 프로젝트 맴버 캐쉬 업데이트
            appWebCacheService.updateAppMemberCache(appEntity.getName());

            System.out.println("end");

            logService.addHistory(appCreate.getAppName(), AppLogAction.CREATE_APP, AppLogStatus.SUCCESS, log);

        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                int projectId = appEntity.getProjectId();

                //파이프라인이 있다면 파이프라인 중지.
                if (pipelineId > 0) {
                    gitLabApi.getPipelineApi().cancelPipelineJobs(projectId, pipelineId);
                }

                //프로젝트 삭제
                if (projectId > 0) {
                    gitLabApi.getProjectApi().deleteProject(projectId);
                    try {
                        gitLabApi.getProjectApi().deleteProject(projectId);
                    } catch (Exception exx) {

                    }
                }

                //미러 프로젝트 삭제
                gitMirrorService.deleteMirrorProject(appEntity.getName());

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

    private AppEntity bindUniquePortForAppEntity(AppCreate appCreate, AppEntity appEntity) {
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

        return appEntity;
    }
}
