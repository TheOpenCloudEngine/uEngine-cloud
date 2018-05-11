package org.uengine.cloud.app.git;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryHook;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.ProjectHook;
import org.gitlab4j.api.models.Visibility;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private static String GITHUB_MIRROR_SERVICE = "github-mirror-service";
    private static String SYNC_TO_GITHUB = "github";
    private static String SYNC_TO_GITLAB = "gitlab";

    private static final Logger LOGGER = LoggerFactory.getLogger(GitMirrorService.class);

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
        this.syncGitlabToGithub(appEntity.getName());
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

        //create github webhook.
        String url = triggerVariables.get("UENGINE_CLOUD_URL").toString() + "/githubhook";
        RepositoryHook repositoryHook = githubExtentApi.createRepositoryHook(githubToken, repository, url);

        //push github source codes to gitlab.
        this.syncGithubToGitlab(appEntity.getName());
    }

    public void syncGithubToGitlab(String appName) throws Exception {
        this.executeMirrorPipelineTrigger(appName, SYNC_TO_GITLAB);
    }

    public void syncGitlabToGithub(String appName) throws Exception {
        this.executeMirrorPipelineTrigger(appName, SYNC_TO_GITHUB);
    }

    /**
     * 미러 프로젝트의 파이프라인을 실행시킨다.
     *
     * @param appName
     * @return
     * @throws Exception
     */
    public Map executeMirrorPipelineTrigger(String appName, String syncTo) throws Exception {

        LOGGER.info("executeMirrorPipelineTrigger {}, {}", appName, syncTo);

        //check mirror ci project, and create if not exist.
        Project mirrorProject = this.getMirrorProject();
        if (mirrorProject == null) {
            mirrorProject = this.createMirrorProject();
        }

        int mirrorProjectId = mirrorProject.getId();
        String token = gitlabExtentApi.getProjectDcosTriggerToken(mirrorProjectId);

        //콘텐트 교체.
        //교체할 파라미터 셋
        Map<String, Object> variables = this.getMirrorTriggerVariables(appName, syncTo);
        Map pipeline = gitlabExtentApi.triggerPipeline(mirrorProjectId, token, "master", variables);
        return pipeline;
    }

    private Map<String, Object> getMirrorTriggerVariables(String appName, String syncTo) throws Exception {

        Map<String, Object> data = new HashMap<>();
        String APP_NAME = appName;
        String UENGINE_CLOUD_URL = environment.getProperty("vcap.services.uengine-cloud-server.external");
        int CONFIG_REPO_ID = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
        String PACKAGE_URL = environment.getProperty("registry.package");

        AppEntity appEntity = appWebCacheService.findOneCache(appName);
        String accessToken = null;
        OauthUser oauthUser = null;
        try {
            String userName = appEntity.getIam();
            oauthUser = iamClient.getUser(userName);
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
        Map repository = githubExtentApi.getRepositoryById(githubToken, appEntity.getGithubRepoId());
        String GITHUB_REPO_OWNER = ((Map) repository.get("owner")).get("login").toString();
        String GITHUB_REPO_NAME = repository.get("name").toString();
        String GITHUB_TOKEN = githubToken;

        Project gitlabProject = gitLabApi.getProjectApi().getProject(appEntity.getProjectId());
        String GITLAB_REPO_OWNER = gitlabProject.getNamespace().getPath();
        String GITLAB_REPO_NAME = gitlabProject.getPath();
        String GITLAB_TOKEN = environment.getProperty("gitlab.token");
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

    private Project getMirrorProject() throws Exception {
        Project mirrorProject = null;
        List<Project> projects = gitLabApi.getProjectApi().getProjects(GITHUB_MIRROR_SERVICE);
        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).getName().equals(GITHUB_MIRROR_SERVICE)) {
                mirrorProject = projects.get(i);
            }
        }
        return mirrorProject;
    }

    private Project createMirrorProject() throws Exception {
        Project projectToBe = new Project();
        projectToBe.setPublic(true);
        projectToBe.setVisibility(Visibility.PUBLIC);
        projectToBe.setPath(GITHUB_MIRROR_SERVICE);
        projectToBe.setName(GITHUB_MIRROR_SERVICE);

        Project project = gitLabApi.getProjectApi().createProject(projectToBe);
        Integer projectId = project.getId();

        //러너 등록.
        int dockerRunnerId = gitlabExtentApi.getDockerRunnerId();
        gitlabExtentApi.enableRunner(projectId, dockerRunnerId);

        //트리거 등록
        gitlabExtentApi.createTrigger(projectId, null, "dcosTrigger");

        //ci 파일 복사
        int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
        String ciText = gitlabExtentApi.getRepositoryFile(repoId, "master", "template/common/.gitlab-ci-mirror.yml");
        gitlabExtentApi.updateOrCraeteRepositoryFile(
                projectId, "master", ".gitlab-ci.yml", ciText);

        return project;
    }
}
