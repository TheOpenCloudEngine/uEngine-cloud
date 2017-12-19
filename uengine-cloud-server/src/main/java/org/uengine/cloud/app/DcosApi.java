package org.uengine.cloud.app;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Applications;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.gitlab4j.api.GitLabApi;
import org.opencloudengine.garuda.util.HttpUtils;
import org.opencloudengine.garuda.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

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
    private EurekaClient discoveryClient;

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

    /**
     * 줄 라우터들을 리프레쉬한다.
     *
     * @throws Exception
     */
    public void refreshRoute() throws Exception {
        //모든 줄 라우터들을 리프레쉬한다.
        //유레카 서버에서 라우터에 해당하는 앱 목록을 호출한 후, 리프레쉬 한다.

        logger.info("refreshRoute start.");

        Applications applications = discoveryClient.getApplications();
        List<Application> registeredApplications = applications.getRegisteredApplications();
        for (int i = 0; i < registeredApplications.size(); i++) {
            Application application = registeredApplications.get(i);
            List<InstanceInfo> instances = application.getInstances();
            for (int i1 = 0; i1 < instances.size(); i1++) {
                InstanceInfo instanceInfo = instances.get(i1);
                InstanceInfo.InstanceStatus status = instanceInfo.getStatus();
                if ("UP".equals(status.toString()) && "zuul".equals(instanceInfo.getMetadata().get("appType"))) {
                    String url = "http://" + instanceInfo.getHostName() + ":" + instanceInfo.getPort() + "/broadcastRefreshRoute";
                    try {
                        new HttpUtils().makeRequest("GET", url, null, new HashMap<>());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
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
