package org.uengine.cloud.app.marathon;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.gitlab4j.api.GitLabApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.uengine.iam.util.HttpUtils;
import org.uengine.iam.util.JsonUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by uengine on 2017. 11. 21..
 */
@Service
public class DcosApi implements InitializingBean {

    private Logger logger = LoggerFactory.getLogger(DcosApi.class);

    @Autowired
    Environment environment;

    @Autowired
    GitLabApi gitLabApi;

    private String host;
    private String token;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.host = environment.getProperty("dcos.host");
        this.token = environment.getProperty("dcos.token");
    }

    private Map<String, String> addHeaders() {
        Map headers = new HashMap();
        headers.put("Content-Type", "application/json");
        headers.put("Authorization", "token=" + token);
        return headers;
    }
    //service/marathon/v2/deployments/


    public Map deleteDeployment(String deploymentId) throws Exception {
        HttpResponse response = new HttpUtils().makeRequest("DELETE",
                host + "/service/marathon/v2/deployments/" + deploymentId,
                null,
                this.addHeaders()
        );
        if (response.getStatusLine().getStatusCode() == 200) {
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity);
            return JsonUtils.unmarshal(json);
        } else {
            return null;
        }
    }

    public void deleteApp(String appId) throws Exception {
        HttpResponse response = new HttpUtils().makeRequest("DELETE",
                host + "/service/marathon/v2/apps/" + appId + "?force=true",
                null,
                this.addHeaders()
        );
    }

    public Map getApp(String appId) throws Exception {
        try {
            HttpResponse response = new HttpUtils().makeRequest("GET",
                    host + "/service/marathon/v2/apps/" + appId + "?embed=app.taskStats&embed=app.lastTaskFailure",
                    null,
                    this.addHeaders()
            );
            if (response.getStatusLine().getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                String json = EntityUtils.toString(entity);
                return JsonUtils.unmarshal(json);
            } else {
                return null;
            }
        } catch (Exception ex) {
            return null;
        }
    }


    public Map createApp(String deployString) throws Exception {
        HttpResponse response = new HttpUtils().makeRequest("POST",
                host + "/service/marathon/v2/apps?force=true",
                deployString,
                this.addHeaders()
        );
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity);
        System.out.println("Create: " + json);
        if (statusCode != 200 && statusCode != 201) {
            throw new Exception(json);
        }
        return JsonUtils.unmarshal(json);
    }

    public Map updateApp(String appId, String deployString) throws Exception {
        HttpResponse response = new HttpUtils().makeRequest("PUT",
                host + "/service/marathon/v2/apps/" + appId + "?force=true",
                deployString,
                this.addHeaders()
        );
        int statusCode = response.getStatusLine().getStatusCode();
        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity);
        System.out.println("Update: " + json);
        if (statusCode != 200) {
            throw new Exception(json);
        }
        return JsonUtils.unmarshal(json);
    }

    public Map getState() throws Exception {
        HttpResponse response = new HttpUtils().makeRequest("GET",
                host + "/mesos/master/state",
                null,
                this.addHeaders()
        );
        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity);
        return JsonUtils.unmarshal(json);
    }

    public Map getGroups() throws Exception {
        HttpResponse response = new HttpUtils().makeRequest("GET",
                host + "/service/marathon/v2/groups?embed=group.groups&embed=group.apps&embed=group.pods&embed=group.apps.deployments&embed=group.apps.counts&embed=group.apps.tasks&embed=group.apps.taskStats&embed=group.apps.lastTaskFailure",
                null,
                this.addHeaders()
        );
        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity);
        return JsonUtils.unmarshal(json);
    }

    public List<Map> getDeployments() throws Exception {
        HttpResponse response = new HttpUtils().makeRequest("GET",
                host + "/service/marathon/v2/deployments",
                null,
                this.addHeaders()
        );
        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity);
        return JsonUtils.unmarshalToList(json);
    }

    public Map getLast() throws Exception {
        HttpResponse response = new HttpUtils().makeRequest("GET",
                host + "/dcos-history-service/history/last",
                null,
                this.addHeaders()
        );
        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity);
        return JsonUtils.unmarshal(json);
    }

    public Map getMesosTaskById(String taskId) throws Exception {
        HttpResponse response = new HttpUtils().makeRequest("GET",
                host + "/mesos/master/tasks?task_id=" + taskId,
                null,
                this.addHeaders()
        );
        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity);
        Map unmarshal = JsonUtils.unmarshal(json);
        List<Map> tasks = (List<Map>) unmarshal.get("tasks");
        if (!tasks.isEmpty()) {
            return tasks.get(0);
        } else {
            return null;
        }
    }
}
