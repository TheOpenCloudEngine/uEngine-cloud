package org.uengine.cloud.app;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.RepositoryFile;
import org.uengine.iam.util.HttpUtils;
import org.uengine.iam.util.JsonUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by uengine on 2017. 11. 20..
 */
@Service
public class GitlabExtentApi implements InitializingBean {

    @Autowired
    Environment environment;

    @Autowired
    GitLabApi gitLabApi;

    private String host;
    private String token;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.host = environment.getProperty("gitlab.host");
        this.token = environment.getProperty("gitlab.token");
    }

    private Map<String, String> addHeaders() {
        Map headers = new HashMap();
        headers.put("Content-Type", "application/json");
        headers.put("PRIVATE-TOKEN", token);
        return headers;
    }

    public List<Map> getRunners() throws Exception {
        HttpResponse response = new HttpUtils().makeRequest("GET",
                host + "/api/v4/runners/all",
                null,
                this.addHeaders()
        );
        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity);
        return JsonUtils.unmarshalToList(json);
    }

    public int getDockerRunnerId() throws Exception {
        List<Map> runners = this.getRunners();
        for (Map runner : runners) {
            if (runner.get("description").equals("uengine docker runner:shared-socket")) {
                return Integer.parseInt(runner.get("id").toString());
            }
        }
        throw new Exception("Not Found uengine docker runner:shared-socket");
    }

    public String getRepositoryFile(int projectId, String branch, String path) throws Exception {
        InputStream inputStream = gitLabApi.getRepositoryFileApi().getRawFile(
                projectId, branch, path);
        String content = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
        return content;
    }

    public void removeRepositoryFile(int projectId, String branch, String path) throws Exception {
        try {
            gitLabApi.getRepositoryFileApi().deleteFile(path, projectId, branch, "System update");
        } catch (Exception ex) {

        }
    }

    //한번씩만 보완.
    public void updateOrCraeteRepositoryFile(int projectId, String branch, String path, String content) throws Exception {
        RepositoryFile file = null;
        try {
            file = gitLabApi.getRepositoryFileApi().getFile(path, projectId, branch);
        } catch (Exception e) {
        }

        if (file != null) {
            file.setContent(content);
            file.setEncoding(null);
            try {
                gitLabApi.getRepositoryFileApi().updateFile(file, projectId, branch, "System update");
            } catch (Exception ex) {

            }
        } else {
            file = new RepositoryFile();
            file.setFilePath(path);
            file.setContent(content);
            file.setEncoding(null);
            try {
                gitLabApi.getRepositoryFileApi().createFile(file, projectId, branch, "System update");
            } catch (Exception ex) {

            }
        }
    }

    public Map createUser(Map user) throws Exception {
        HttpResponse response = new HttpUtils().makeRequest("POST",
                host + "/api/v4/users",
                JsonUtils.marshal(user),
                this.addHeaders()
        );
        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 201) {
            throw new Exception(json);
        }
        return JsonUtils.unmarshal(json);
    }

    public Map forkProject(int projectId, String namespace) throws Exception {
        Map params = new HashedMap();
        params.put("namespace", namespace);
        HttpResponse response = new HttpUtils().makeRequest("POST",
                host + "/api/v4/projects/" + projectId + "/fork",
                JsonUtils.marshal(params),
                this.addHeaders()
        );
        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity);
        return JsonUtils.unmarshal(json);
    }

    public void deleteForkRelation(int projectId) throws Exception {
        HttpResponse response = new HttpUtils().makeRequest("DELETE",
                host + "/api/v4/projects/" + projectId + "/fork",
                null,
                this.addHeaders()
        );
    }

    public Map enableRunner(int projectId, int runnerId) throws Exception {
        Map params = new HashedMap();
        params.put("runner_id", runnerId);
        HttpResponse response = new HttpUtils().makeRequest("POST",
                host + "/api/v4/projects/" + projectId + "/runners",
                JsonUtils.marshal(params),
                this.addHeaders()
        );
        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity);
        return JsonUtils.unmarshal(json);
    }

    public Map createTrigger(int projectId, String gitlabUsername, String description) throws Exception {
        Map params = new HashedMap();
        params.put("description", description);

        Map<String, String> headers = this.addHeaders();
        headers.put("Sudo", gitlabUsername);
        HttpResponse response = new HttpUtils().makeRequest("POST",
                host + "/api/v4/projects/" + projectId + "/triggers",
                JsonUtils.marshal(params),
                headers
        );
        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity);
        return JsonUtils.unmarshal(json);
    }

    public String getProjectDcosTriggerToken(int projectId) throws Exception {
        HttpResponse response = new HttpUtils().makeRequest("GET",
                host + "/api/v4/projects/" + projectId + "/triggers",
                null,
                this.addHeaders()
        );
        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity);
        List<Map> list = JsonUtils.unmarshalToList(json);
        String token = null;
        for (int i = 0; i < list.size(); i++) {
            Map map = list.get(i);
            if (map.get("description").equals("dcosTrigger")) {
                token = map.get("token").toString();
            }
        }
        return token;
    }

    public Map triggerPipeline(int projectId, String token, String ref, Map<String, Object> variables) throws Exception {
        Map headers = new HashMap();
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        headers.put("PRIVATE-TOKEN", this.token);

        Map params = new HashMap();
        if (!variables.isEmpty()) {
            for (Map.Entry<String, Object> entry : variables.entrySet()) {
                params.put("variables[" + entry.getKey() + "]", entry.getValue());
            }
        }
        params.put("token", token);
        params.put("ref", ref);
        String postQueryString = HttpUtils.createPOSTQueryString(params);

        HttpResponse response = new HttpUtils().makeRequest("POST",
                host + "/api/v4/projects/" + projectId + "/trigger/pipeline",
                postQueryString,
                headers
        );
        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity);
        return JsonUtils.unmarshal(json);
    }

    public void playJob(int projectId, int jobId) throws Exception {
        HttpResponse response = new HttpUtils().makeRequest("POST",
                host + "/api/v4/projects/" + projectId + "/jobs/" + jobId + "/play",
                null,
                this.addHeaders()
        );
    }
}
