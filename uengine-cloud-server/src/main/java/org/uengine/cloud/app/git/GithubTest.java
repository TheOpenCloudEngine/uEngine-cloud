package org.uengine.cloud.app.git;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryHook;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GithubTest {

    public static void main(String[] args) throws Exception {
        GitHubClient gitHubClient = new GitHubClient();
        gitHubClient.setOAuth2Token("28ff1613a3e88a7bf25f4614a040c7488e62dff4");

        RepositoryHook hook = new RepositoryHook();
        hook.setName("web");
        hook.setActive(true);
        Map<String, String> config = new HashMap<>();
        config.put("url", "http://example.com/webhook");
        config.put("content_type", "json");
        hook.setConfig(config);
        RepositoryService repositoryService = new RepositoryService(gitHubClient);
        Repository repository = repositoryService.getRepository("SeungpilPark", "mirror-test");


        repositoryService.createHook(repository, hook);
        System.out.println(repository.getId());

//        UserService userService = new UserService(gitHubClient);
//        org.eclipse.egit.github.core.User user = userService.getUser();
//        String email = user.getEmail();
//        System.out.println(email);

//        String host = "http://gitlab.pas-mini.io";
//        String token = "-arWnfRY7S4h6oyRthNy";
//        GitLabApi gitLabApi = new GitLabApi(host, token);
//        gitLabApi.setDefaultPerPage(10000);
//
//        Project projectToBe = new Project();
//        projectToBe.setPublic(true);
//        projectToBe.setVisibility(Visibility.PUBLIC);
//        projectToBe.setPath("test-a");
//        projectToBe.setName("test-a");
////
////        //유저 네임스페이스는 됨.
//        List<Namespace> namespaces = gitLabApi.getNamespaceApi().findNamespaces("sanghoon");
//        //List<Namespace> uengine = gitLabApi.getNamespaceApi().findNamespaces("uengine");
////        projectToBe.setNamespace(uengine.get(0));
////
////
////        //그룹 네임스페이스는? 안되는것 같음.
//
//        projectToBe.setNamespace(namespaces.get(0));
//        gitLabApi.getProjectApi().createProject(projectToBe, "https://github.com/TheOpenCloudEngine/uEngine-cloud-elk-compose");

//
//        System.out.println("end");

        //User user = gitLabApi.getUserApi().getUser(6);
//
//        //341
        //gitLabApi.getProjectApi().forkProject(341, uengine.get(0).getPath());

    }
}
