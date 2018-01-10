package org.uengine.cloud.scheduler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.uengine.iam.util.HttpUtils;
import org.uengine.iam.util.JsonUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.uengine.cloud.app.AppService;
import org.uengine.cloud.app.DcosApi;
import org.uengine.cloud.ssh.SshService;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by uengine on 2017. 12. 4..
 */
@Component
public class CronTable implements InitializingBean {

    @Autowired
    private SshService sshService;

    @Autowired
    private DcosApi dcosApi;

    @Autowired
    private AppService appService;

    @Autowired
    private Environment environment;

    private String host;
    private String token;

    public Map dcosData;

    public Map getDcosData() {
        return dcosData;
    }

    public void setDcosData(Map dcosData) {
        this.dcosData = dcosData;
    }

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

    // 애플리케이션 시작 후 1초 후에 첫 실행, 그 후 매 2초마다 주기적으로 실행한다.
    @Scheduled(initialDelay = 1000, fixedDelay = 2000)
    public void fetchDcosData() throws Exception {
        Map dcosData = new HashMap();

        //last
        try {
            HttpResponse response = new HttpUtils().makeRequest("GET",
                    host + "dcos-history-service/history/last",
                    null,
                    this.addHeaders()
            );
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity);
            dcosData.put("last", JsonUtils.unmarshal(json));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //jobs
        try {
            HttpResponse response = new HttpUtils().makeRequest("GET",
                    host + "service/metronome/v1/jobs?embed=activeRuns&embed=schedules&embed=historySummary",
                    null,
                    this.addHeaders()
            );
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity);
            dcosData.put("jobs", JsonUtils.unmarshalToList(json));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //groups
        try {
            HttpResponse response = new HttpUtils().makeRequest("GET",
                    host + "service/marathon/v2/groups?embed=group.groups&embed=group.apps&embed=group.pods&embed=group.apps.deployments&embed=group.apps.counts&embed=group.apps.tasks&embed=group.apps.taskStats&embed=group.apps.lastTaskFailur",
                    null,
                    this.addHeaders()
            );
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity);
            dcosData.put("groups", JsonUtils.unmarshal(json));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //queue
        try {
            HttpResponse response = new HttpUtils().makeRequest("GET",
                    host + "service/marathon/v2/queue",
                    null,
                    this.addHeaders()
            );
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity);
            dcosData.put("queue", JsonUtils.unmarshal(json));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //deployments
        try {
            HttpResponse response = new HttpUtils().makeRequest("GET",
                    host + "service/marathon/v2/deployments",
                    null,
                    this.addHeaders()
            );
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity);
            dcosData.put("deployments", JsonUtils.unmarshalToList(json));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //units
        try {
            HttpResponse response = new HttpUtils().makeRequest("GET",
                    host + "system/health/v1/units",
                    null,
                    this.addHeaders()
            );
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity);
            dcosData.put("units", JsonUtils.unmarshal(json));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //state
        try {
            HttpResponse response = new HttpUtils().makeRequest("GET",
                    host + "mesos/master/state",
                    null,
                    this.addHeaders()
            );
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity);
            dcosData.put("state", JsonUtils.unmarshal(json));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //devopsApps
        try {
            HttpResponse response = new HttpUtils().makeRequest("GET",
                    environment.getProperty("spring.cloud.config.uri") + "/dcos-apps.json",
                    null,
                    new HashMap<>()
            );
            HttpEntity entity = response.getEntity();
            String json = EntityUtils.toString(entity);
            dcosData.put("devopsApps", JsonUtils.unmarshal(json));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.setDcosData(dcosData);
    }

    // 애플리케이션 시작 후 10초 후에 첫 실행, 그 후 매 10초마다 주기적으로 실행한다.
    @Scheduled(initialDelay = 10000, fixedDelay = 10000)
    public void removeSshContainer() throws Exception {
        Date currentDate = new Date();
        Map groups = dcosApi.getGroups();
        List<Map> apps = (List<Map>) groups.get("apps");
        for (int i = 0; i < apps.size(); i++) {
            Map marathonApp = apps.get(i);
            String marathonAppId = marathonApp.get("id").toString();
            if (marathonAppId.startsWith("/ssh-")) {
                String taskId = marathonAppId.replace("/ssh-", "");

                //허트비트 매퍼에 마라톤아이디가 없으면 삭제.
                if (!sshService.heartbeatMapper.containsKey(taskId)) {
                    sshService.deleteSshContainer(taskId);
                }
                //허트비트 매퍼에, 10초 이상된 taskId 삭제.
                else {
                    Date lastDate = sshService.heartbeatMapper.get(taskId);
                    long diff = currentDate.getTime() - lastDate.getTime();
                    if (diff > 1000 * 10) {
                        sshService.deleteSshContainer(taskId);
                    }
                }
            }
        }
    }
}
