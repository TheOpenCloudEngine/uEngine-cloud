package org.uengine.cloud.app.git;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryHook;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;
import org.springframework.stereotype.Service;
import org.uengine.iam.util.HttpUtils;
import org.uengine.iam.util.JsonUtils;
import org.uengine.iam.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by uengine on 2017. 11. 20..
 */
@Service
public class GithubExtentApi {

    private Map<String, String> addHeaders(String token) {
        Map headers = new HashMap();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "token " + token);
        return headers;
    }

    public Map getRepositoryById(String token, Long repoId) throws Exception {
        HttpResponse response = new HttpUtils().makeRequest("GET",
                "https://api.github.com/repositories/" + repoId,
                null,
                this.addHeaders(token)
        );
        if (response.getStatusLine().getStatusCode() == 200) {
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity);
            return JsonUtils.unmarshal(json);
        } else {
            return null;
        }
    }

    public Repository getRepositoryByOwnerAndName(String token, String owner, String name) throws Exception {

        try {
            return this.getRepositoryService(token).getRepository(owner, name);
        } catch (Exception ex) {
            return null;
        }
    }

    private UserService getUserService(String token) {
        GitHubClient gitHubClient = new GitHubClient();
        gitHubClient.setOAuth2Token(token);

        return new UserService(gitHubClient);
    }

    private RepositoryService getRepositoryService(String token) {
        GitHubClient gitHubClient = new GitHubClient();
        gitHubClient.setOAuth2Token(token);

        return new RepositoryService(gitHubClient);
    }

    public User getUser(String token) {
        try {
            return this.getUserService(token).getUser();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public Repository createRepository(String token, String orgName, String repoName) throws Exception {
        Repository repository = new Repository();
        repository.setName(repoName);

        if (StringUtils.isEmpty(orgName)) {
            return this.getRepositoryService(token).createRepository(repository);
        } else {
            return this.getRepositoryService(token).createRepository(orgName, repository);
        }
    }

    public RepositoryHook createRepositoryHook(String token, Repository repository, String url) throws Exception {

        RepositoryHook hook = new RepositoryHook();
        hook.setName("web");
        hook.setActive(true);
        Map<String, String> config = new HashMap<>();
        config.put("url", url);
        config.put("content_type", "json");
        hook.setConfig(config);

        return this.getRepositoryService(token).createHook(repository, hook);
    }

    public List<RepositoryHook> listRepositoryHook(String token, Repository repository) throws Exception {

        try {
            return this.getRepositoryService(token).getHooks(repository);
        } catch (Exception ex) {
            return new ArrayList<RepositoryHook>();
        }
    }
}
