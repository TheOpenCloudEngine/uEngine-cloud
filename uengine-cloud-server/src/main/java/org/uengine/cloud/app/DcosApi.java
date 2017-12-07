package org.uengine.cloud.app;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.gitlab4j.api.GitLabApi;
import org.opencloudengine.garuda.util.HttpUtils;
import org.opencloudengine.garuda.util.JsonUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by uengine on 2017. 11. 21..
 */
@Service
public class DcosApi implements InitializingBean {

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
                    host + "/service/marathon/v2/apps/" + appId,
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
        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity);
        return JsonUtils.unmarshal(json);
    }

    public Map updateApp(String appId, String deployString) throws Exception {
        HttpResponse response = new HttpUtils().makeRequest("PUT",
                host + "/service/marathon/v2/apps/" + appId + "?force=true",
                deployString,
                this.addHeaders()
        );
        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity);
        return JsonUtils.unmarshal(json);
    }

    public void refreshRouter() throws Exception {
        HttpResponse response = new HttpUtils().makeRequest("GET",
                "http://" + environment.getProperty("vcap.services.zuul-prod-server.external") + "/refreshRoute",
                null,
                new HashMap<>()
        );
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
                host + "/service/marathon/v2/groups?embed=group.groups&embed=group.apps&embed=group.pods&embed=group.apps.deployments&embed=group.apps.counts&embed=group.apps.tasks&embed=group.apps.taskStats&embed=group.apps.lastTaskFailur",
                null,
                this.addHeaders()
        );
        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity);
        return JsonUtils.unmarshal(json);
    }
}
