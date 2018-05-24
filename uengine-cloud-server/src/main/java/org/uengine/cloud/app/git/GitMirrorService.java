package org.uengine.cloud.app.git;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryHook;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.uengine.cloud.app.AppEntity;
import org.uengine.cloud.app.AppWebCacheService;
import org.uengine.cloud.app.AppWebService;
import org.uengine.cloud.app.pipeline.AppPipeLineService;
import org.uengine.iam.client.IamClient;
import org.uengine.iam.client.ResourceOwnerPasswordCredentials;
import org.uengine.iam.client.TokenType;
import org.uengine.iam.client.model.OauthUser;
import org.uengine.iam.util.JsonUtils;
import org.uengine.iam.util.StringUtils;

import javax.json.Json;
import java.util.*;

@Service
public class GitMirrorService {

    @Autowired
    private GithubExtentApi githubExtentApi;

    @Autowired
    private GitLabApi gitLabApi;

    @Autowired
    private GitlabExtentApi gitlabExtentApi;

    @Autowired
    private AppWebService appWebService;

    @Autowired
    private AppWebCacheService appWebCacheService;

    @Autowired
    private IamClient iamClient;

    @Autowired
    private Environment environment;

    private static String MIRROR_PROJECT_PREFIX = "-mirror";
    private static String SYNC_TO_GITHUB = "github";
    private static String SYNC_TO_GITLAB = "gitlab";

    private static final Logger LOGGER = LoggerFactory.getLogger(GitMirrorService.class);

    public String getMirrorProjectPrefix() {
        return this.MIRROR_PROJECT_PREFIX;
    }

    public void createNewGithubProject(
            AppEntity appEntity,
            OauthUser oauthUser,
            Map<String, Object> triggerVariables,
            String orgName,
            String repoName) throws Exception {

        //check token.
        Assert.notNull(oauthUser.getMetaData().get("githubToken"), "githubToken required.");
        String githubToken = (String) oauthUser.getMetaData().get("githubToken");

        //create github repository
        Repository repository = githubExtentApi.createRepository(githubToken, orgName, repoName);
        appEntity.setGithubRepoId(repository.getId());
        appWebService.save(appEntity);

        //create github webhook.
        String url = triggerVariables.get("UENGINE_CLOUD_URL").toString() + "/githubhook";
        RepositoryHook repositoryHook = githubExtentApi.createRepositoryHook(githubToken, repository, url);

        //push gitlab source codes to github. (trigger project ci will continue in shell script.)
        this.syncGitlabToGithub(appEntity.getName(), null, true);
    }

    public void manageExistGithubProject(
            AppEntity appEntity,
            OauthUser oauthUser,
            Map<String, Object> triggerVariables,
            Long githubRepoId) throws Exception {

        //check token.
        Assert.notNull(oauthUser.getMetaData().get("githubToken"), "githubToken required.");
        String githubToken = (String) oauthUser.getMetaData().get("githubToken");

        Map repo = githubExtentApi.getRepositoryById(githubToken, githubRepoId);
        Assert.notNull(repo, "repository not found.");
        Repository repository = JsonUtils.convertValue(repo, Repository.class);

        //create github webhook if not exist.
        boolean isHookExist = false;
        String url = triggerVariables.get("UENGINE_CLOUD_URL").toString() + "/githubhook";
        List<RepositoryHook> hooks = githubExtentApi.listRepositoryHook(githubToken, repository);
        if (!hooks.isEmpty()) {
            for (RepositoryHook hook : hooks) {
                if (url.equals(hook.getConfig().get("url").toString())) {
                    isHookExist = true;
                }
            }
        }
        if (!isHookExist) {
            RepositoryHook repositoryHook = githubExtentApi.createRepositoryHook(githubToken, repository, url);
        }

        //push github source codes to gitlab.
        this.syncGithubToGitlab(appEntity.getName(), null);
    }

    public Map syncGithubToGitlab(String appName, String commit) throws Exception {
        return this.executeMirrorPipelineTrigger(appName, commit, SYNC_TO_GITLAB, false);
    }

    public Map syncGitlabToGithub(String appName, String commit, boolean runCiAfterSync) throws Exception {
        return this.executeMirrorPipelineTrigger(appName, commit, SYNC_TO_GITHUB, runCiAfterSync);
    }

    /**
     * 미러 프로젝트의 파이프라인을 실행시킨다.
     *
     * @param appName
     * @return
     * @throws Exception
     */
    public Map executeMirrorPipelineTrigger(String appName, String commit, String syncTo, boolean runCiAfterSync) throws Exception {

        //commit just need for user navigate which commit is mirroring.
        LOGGER.info("executeMirrorPipelineTrigger {}, sync to  {}, commit {}", appName, syncTo, commit);

        AppEntity appEntity = appWebCacheService.findOneCache(appName);
        String userName = appEntity.getIam();
        OauthUser oauthUser = iamClient.getUser(userName);

        Assert.notNull(oauthUser.getMetaData().get("gitlab-id"), "gitlab-id required.");
        int gitlabId = (int) oauthUser.getMetaData().get("gitlab-id");
        User gitlabUser = gitLabApi.getUserApi().getUser(gitlabId);
        Assert.notNull(gitlabUser, "gitlabUser not exist.");

        //check mirror ci project, and create if not exist.
        Project mirrorProject = this.getMirrorProject(appName);
        if (mirrorProject == null) {
            mirrorProject = this.createMirrorProject(appName, gitlabUser);
        }

        int mirrorProjectId = mirrorProject.getId();
        String token = gitlabExtentApi.getProjectDcosTriggerToken(mirrorProjectId);

        //콘텐트 교체.
        //교체할 파라미터 셋
        Map<String, Object> variables = this.getMirrorTriggerVariables(appName, oauthUser, syncTo);

        // CI_RUN if need pipeline run after sync repository.
        // it will be needed if repository first created.
        if (runCiAfterSync) {
            variables.put("CI_RUN", "true");
        }

        Map pipeline = gitlabExtentApi.triggerPipeline(mirrorProjectId, token, "master", variables);

        return pipeline;
    }

    /**
     * 미러 파이프라인을 실행하기 위한 파라미터들을 수집한다.
     *
     * @param appName
     * @param oauthUser
     * @param syncTo
     * @return
     * @throws Exception
     */
    private Map<String, Object> getMirrorTriggerVariables(String appName, OauthUser oauthUser, String syncTo) throws Exception {

        Map<String, Object> data = new HashMap<>();
        String APP_NAME = appName;
        String UENGINE_CLOUD_URL = environment.getProperty("vcap.services.uengine-cloud-server.external");
        int CONFIG_REPO_ID = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
        String PACKAGE_URL = environment.getProperty("registry.package");

        AppEntity appEntity = appWebCacheService.findOneCache(appName);
        String accessToken = null;
        try {
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

        Assert.notNull(oauthUser.getMetaData().get("githubToken"), "githubToken required.");
        String githubToken = (String) oauthUser.getMetaData().get("githubToken");

        LOGGER.info("Search github repo id {}", appEntity.getGithubRepoId());
        Map repository = githubExtentApi.getRepositoryById(githubToken, appEntity.getGithubRepoId());
        Assert.notNull(repository, "github repository not found.");

        LOGGER.info("github repo payload is {}", JsonUtils.marshal(repository));
        String GITHUB_REPO_OWNER = ((Map) repository.get("owner")).get("login").toString();
        String GITHUB_REPO_NAME = repository.get("name").toString();
        String GITHUB_TOKEN = githubToken;

        //check tokens, and create if not exist.
        ImpersonationToken impersonationToken = null;
        int gitlabId = (int) oauthUser.getMetaData().get("gitlab-id");
        String tokenName = "cloud-server-token";
        List<ImpersonationToken> tokens = gitLabApi.getUserApi().getImpersonationTokens(gitlabId);
        if (tokens != null && !tokens.isEmpty()) {
            for (ImpersonationToken token : tokens) {
                if (tokenName.equals(token.getName())) {
                    impersonationToken = token;
                }
            }
        }
        if (impersonationToken == null) {
            LOGGER.info("Generate new gitlab impersonationToken for user {}", oauthUser.getUserName());
            Calendar c = Calendar.getInstance();
            c.setTime(new Date());
            c.add(Calendar.YEAR, 100);
            ImpersonationToken.Scope[] scopes = {ImpersonationToken.Scope.API};
            impersonationToken = gitLabApi.getUserApi().createImpersonationToken(gitlabId, tokenName, c.getTime(), scopes);
        }

        Project gitlabProject = gitLabApi.getProjectApi().getProject(appEntity.getProjectId());
        String GITLAB_REPO_OWNER = gitlabProject.getNamespace().getPath();
        String GITLAB_REPO_NAME = gitlabProject.getPath();
        String GITLAB_TOKEN = impersonationToken.getToken();
        String GITLAB_URL = environment.getProperty("gitlab.host").replace("http://", "");
        String SYNC_TO = syncTo;

        //프로젝트 파라미터
        data.put("APP_NAME", APP_NAME);
        data.put("UENGINE_CLOUD_URL", "http://" + UENGINE_CLOUD_URL);
        data.put("CONFIG_REPO_ID", CONFIG_REPO_ID);
        data.put("ACCESS_TOKEN", accessToken);
        data.put("PACKAGE_URL", PACKAGE_URL);

        data.put("GITHUB_REPO_OWNER", GITHUB_REPO_OWNER);
        data.put("GITHUB_REPO_NAME", GITHUB_REPO_NAME);
        data.put("GITHUB_TOKEN", GITHUB_TOKEN);

        data.put("GITLAB_REPO_OWNER", GITLAB_REPO_OWNER);
        data.put("GITLAB_REPO_NAME", GITLAB_REPO_NAME);
        data.put("GITLAB_TOKEN", GITLAB_TOKEN);
        data.put("GITLAB_URL", GITLAB_URL);

        data.put("SYNC_TO", SYNC_TO);

        try {
            System.out.println(data.get("APP_NAME").toString());
            System.out.println(data.get("UENGINE_CLOUD_URL").toString());
            System.out.println(data.get("CONFIG_REPO_ID").toString());
            System.out.println(data.get("ACCESS_TOKEN").toString());
            System.out.println(data.get("PACKAGE_URL").toString());

            System.out.println(data.get("GITHUB_REPO_OWNER").toString());
            System.out.println(data.get("GITHUB_REPO_NAME").toString());
            System.out.println(data.get("GITHUB_TOKEN").toString());

            System.out.println(data.get("GITLAB_REPO_OWNER").toString());
            System.out.println(data.get("GITLAB_REPO_NAME").toString());
            System.out.println(data.get("GITLAB_TOKEN").toString());
            System.out.println(data.get("GITLAB_URL").toString());

            System.out.println(data.get("SYNC_TO").toString());

        } catch (Exception ex) {

        }
        return data;
    }

    /**
     * 미러 프로젝트를 가져온다.
     *
     * @param appName
     * @return
     * @throws Exception
     */
    public Project getMirrorProject(String appName) throws Exception {
        Project mirrorProject = null;
        List<Project> projects = gitLabApi.getProjectApi().getProjects(appName + MIRROR_PROJECT_PREFIX);
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).getName().equals(appName + MIRROR_PROJECT_PREFIX)) {
                mirrorProject = projects.get(i);
            }
        }
        return mirrorProject;
    }

    /**
     * 미러 프로젝트를 생성한다.
     *
     * @param appName
     * @param gitlabUser
     * @return
     * @throws Exception
     */
    public Project createMirrorProject(String appName, User gitlabUser) throws Exception {

        //set gitlabNamespace as gitlab user namespace
        Namespace gitlabNamespace = null;
        String username = gitlabUser.getUsername();
        List<Namespace> namespaces = gitLabApi.getNamespaceApi().findNamespaces(username);
        for (Namespace space : namespaces) {
            if (space.getPath().equals(username)) {
                gitlabNamespace = space;
            }
        }
        Assert.notNull(gitlabNamespace, "gitlabNamespace for user " + username + " not found.");

        //create mirror project.
        Project projectToBe = new Project();
        projectToBe.setPublic(true);
        projectToBe.setVisibility(Visibility.PUBLIC);
        projectToBe.setPath(appName + MIRROR_PROJECT_PREFIX);
        projectToBe.setName(appName + MIRROR_PROJECT_PREFIX);
        projectToBe.setNamespace(gitlabNamespace);

        Project project = gitLabApi.getProjectApi().createProject(projectToBe);
        Integer projectId = project.getId();

        //러너 등록.
        int dockerRunnerId = gitlabExtentApi.getDockerRunnerId();
        gitlabExtentApi.enableRunner(projectId, dockerRunnerId);

        //트리거 등록
        gitlabExtentApi.createTrigger(projectId, gitlabUser.getUsername(), "dcosTrigger");

        //ci 파일 복사
        int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
        String ciText = gitlabExtentApi.getRepositoryFile(repoId, "master", "template/common/.gitlab-ci-mirror.yml");
        gitlabExtentApi.updateOrCraeteRepositoryFile(
                projectId, "master", ".gitlab-ci.yml", ciText);

        return project;
    }

    public void deleteMirrorProject(String appName) {
        //미러 프로젝트 삭제
        try {
            Project mirrorProject = this.getMirrorProject(appName);
            if (mirrorProject != null) {
                gitLabApi.getProjectApi().deleteProject(mirrorProject.getId());
                try {
                    gitLabApi.getProjectApi().deleteProject(mirrorProject.getId());
                } catch (Exception exx) {

                }
            }
        } catch (Exception eex) {

        }
    }
}
