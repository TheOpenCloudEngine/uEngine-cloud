package org.uengine.cloud.ssh;

import org.gitlab4j.api.GitLabApi;
import org.opencloudengine.garuda.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.uengine.cloud.app.DcosApi;
import org.uengine.cloud.app.GitlabExtentApi;
import org.uengine.cloud.scheduler.JobScheduler;
import org.uengine.cloud.templates.MustacheTemplateEngine;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by uengine on 2017. 11. 16..
 */
@Service
public class SshService {
    @Autowired
    Environment environment;

    @Autowired
    private GitLabApi gitLabApi;

    @Autowired
    private GitlabExtentApi gitlabExtentApi;

    @Autowired
    private JobScheduler jobScheduler;

    @Autowired
    private DcosApi dcosApi;

    public Map<String, Date> heartbeatMapper = new HashMap();

    public Map createSshMarathonApp(String taskId, String shell) throws Exception {
        //이미 있어도 shell 이 틀리면 죽이고 다시 살려야함.
        //이미 있고 shell 이 같으면 그냥 보내줌.

        //public-agent-ip

        Map marathonApp = this.getSshMarathonApp(taskId);
        if (marathonApp != null) {
            String appShell = ((Map) marathonApp.get("env")).get("CONTAINER_SHELL").toString();

            //이미 있어도 shell 이 틀리면 죽이고 다시 살려야함.
            if (!shell.equals(appShell)) {
                dcosApi.deleteApp("/ssh-" + taskId);
            }
            //이미 있고 shell 이 같으면 그냥 보내줌.
            else {
                return marathonApp;
            }
        }

        String containerName = null;
        String containerHost = null;
        String containerSlaveId = null;
        Map state = dcosApi.getState();
        List<Map> frameworks = (List<Map>) state.get("frameworks");
        for (Map framework : frameworks) {
            List<Map> tasks = (List<Map>) framework.get("tasks");
            for (int i = 0; i < tasks.size(); i++) {
                Map task = tasks.get(i);
                if (task.get("id").toString().equals(taskId)) {
                    List<Map> statuses = (List<Map>) task.get("statuses");
                    for (int i1 = 0; i1 < statuses.size(); i1++) {
                        Map status = statuses.get(i1);
                        if (status.get("state").toString().equals("TASK_RUNNING")) {
                            String value = ((Map) ((Map) status.get("container_status")).get("container_id")).get("value").toString();
                            containerSlaveId = task.get("slave_id").toString();
                            containerName = "mesos-" + value;
                            containerHost = this.getHostBySlaveId(containerSlaveId, state);
                        }
                    }
                }
            }
        }
        if (StringUtils.isEmpty(containerName)) {
            throw new Exception("Not found container for task: " + taskId);
        }

        double cpus = new Double(0.2);
        double mem = 128;
        boolean enableUseResource = this.isEnableUseResource(cpus, mem, containerSlaveId, state);
        if (!enableUseResource) {
            throw new Exception("Not enough resources for ssh container task: " + taskId);
        }

        int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));

        String sshContainerJson = gitlabExtentApi.getRepositoryFile(repoId, "master", "template/common/ssh-container.json");
        Map data = new HashMap();
        data.put("APP_ID", "ssh-" + taskId);
        data.put("HOST_NAME", containerHost);
        data.put("CPUS", cpus);
        data.put("MEM", mem);
        data.put("CONTAINER_NAME", containerName);
        data.put("CONTAINER_SHELL", shell);
        MustacheTemplateEngine templateEngine = new MustacheTemplateEngine();
        String body = templateEngine.executeTemplateText(sshContainerJson, data);
        Map sshApp = dcosApi.createApp(body);

        this.heartbeatSshContainer(taskId);

        int max_count = 30;
        int count = 0;
        while (true) {
            try {
                count++;
                sshApp = (Map) dcosApi.getApp("/ssh-" + taskId).get("app");
                int servicePort = (int) ((List<Map>) ((Map) sshApp.get("container")).get("portMappings")).get(0).get("servicePort");
                int tasksHealthy = 0;
                if (sshApp.containsKey("tasksHealthy")) {
                    tasksHealthy = (int) sshApp.get("tasksHealthy");
                }
                if (servicePort != 0 && tasksHealthy == 1) {
                    break;
                }
                if (count >= max_count) {
                    //실패시 삭제.
                    try {
                        dcosApi.deleteApp("/ssh-" + taskId);
                    } catch (Exception e) {
                    }
                    throw new Exception("Timeout for service port bind to ssh-task: " + taskId);
                }
                this.heartbeatSshContainer(taskId);
                Thread.sleep(1000);
            } catch (Exception ex) {
                ex.printStackTrace();
                //실패시 삭제.
                try {
                    dcosApi.deleteApp("/ssh-" + taskId);
                } catch (Exception e) {
                }
                break;
            }
        }

        return sshApp;
    }

    private boolean isEnableUseResource(double cpus, double mem, String slaveId, Map state) {
        boolean enableUse = false;
        List<Map> slaves = (List<Map>) state.get("slaves");
        for (int i = 0; i < slaves.size(); i++) {
            Map slave = slaves.get(i);
            if (slave.get("id").toString().equals(slaveId)) {
                Map resources = (Map) slave.get("resources");
                Map usedResources = (Map) slave.get("used_resources");
                double unusedMem = (double) resources.get("mem") - (double) usedResources.get("mem");
                double unusedCpus = (double) resources.get("cpus") - (double) usedResources.get("cpus");
                if (unusedMem >= mem && unusedCpus >= cpus) {
                    enableUse = true;
                }
            }
        }
        return enableUse;
    }

    private String getHostBySlaveId(String slaveId, Map state) {
        String hostname = null;
        List<Map> slaves = (List<Map>) state.get("slaves");
        for (int i = 0; i < slaves.size(); i++) {
            Map slave = slaves.get(i);
            if (slave.get("id").toString().equals(slaveId)) {
                hostname = slave.get("hostname").toString();
            }
        }
        return hostname;
    }

    public Map getSshMarathonApp(String taskId) {
        try {
            Map marathonApp = (Map) dcosApi.getApp("/ssh-" + taskId).get("app");
            return marathonApp;
        } catch (Exception ex) {
            return null;
        }
    }

    public void heartbeatSshContainer(String taskId) {
        heartbeatMapper.put(taskId, new Date());
    }

    public void deleteSshContainer(String taskId) {
        try {
            dcosApi.deleteApp("/ssh-" + taskId);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (heartbeatMapper.containsKey(taskId)) {
                heartbeatMapper.remove(taskId);
            }
        }
    }
}
