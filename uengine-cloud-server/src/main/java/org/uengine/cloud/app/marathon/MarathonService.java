package org.uengine.cloud.app.marathon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uengine.cloud.app.*;

import java.util.*;

/**
 * Created by uengine on 2017. 11. 16..
 */
@Service
public class MarathonService {
    @Autowired
    private Environment environment;

    @Autowired
    private DcosApi dcosApi;

    @Autowired
    private MarathonCacheService marathonCacheService;

    @Autowired
    private AppWebCacheService appWebCacheService;

    private static final Logger LOGGER = LoggerFactory.getLogger(MarathonService.class);


    /**
     * 앱의 모든 마라톤 어플리케이션을 가져온다.
     *
     * @param appName
     * @return
     * @throws Exception
     */
    public Map getMarathonAppsByAppName(String appName) throws Exception {
        Map map = new HashMap();
        AppEntity appEntity = appWebCacheService.findOneCache(appName);
        map.put("dev", marathonCacheService.getMarathonAppByIdCache(appEntity.getDev().getMarathonAppId()));
        map.put("stg", marathonCacheService.getMarathonAppByIdCache(appEntity.getStg().getMarathonAppId()));
        map.put("prod", marathonCacheService.getMarathonAppByIdCache(appEntity.getProd().getMarathonAppId()));
        map.put("oldProd", marathonCacheService.getMarathonAppByIdCache(appEntity.getProd().getMarathonAppIdOld()));
        return map;
    }

    /**
     * 디플로이먼트 아이디로 마라톤 앱을 가져온다.
     *
     * @param deploymentId
     * @return
     * @throws Exception
     */
    public Map getMarathonDeploymentById(String deploymentId) throws Exception {

        List<Map> deployments = dcosApi.getDeployments();
        if (!deployments.isEmpty()) {
            for (Map deployment : deployments) {
                if (deployment.get("id").toString().equals(deploymentId)) {
                    return deployment;
                }
            }
        }
        return null;
    }

    /**
     * 서비스 앱 리스트를 가져온다.
     *
     * @return
     * @throws Exception
     */
    public List<Map> getServiceApps() throws Exception {
        List<String> allAppNames = appWebCacheService.findAllAppNamesCache();
        List<Map> apps = marathonCacheService.getMarathonAppsCache();
        List<Map> serviceApps = new ArrayList<>();
        if (!apps.isEmpty()) {
            for (Map app : apps) {
                if (isServiceApp(app, allAppNames)) {
                    serviceApps.add(app);
                }
            }
        }
        return serviceApps;
    }

    /**
     * 마라톤 앱아이디로 앱을 찾는다.
     *
     * @param marathonAppId
     * @return
     * @throws Exception
     */
    public AppEntity getAppEntityFromMarathonAppId(String marathonAppId) throws Exception {
        String id = marathonAppId.replaceFirst("/", "");
        id = replaceLast(id, "-dev", "");
        id = replaceLast(id, "-stg", "");
        id = replaceLast(id, "-blue", "");
        id = replaceLast(id, "-green", "");
        return appWebCacheService.findOneCache(id);
    }

    /**
     * 마라톤 앱이 서비스 앱인지 확인한다.
     *
     * @param marathonApp
     * @return
     * @throws Exception
     */
    public boolean isServiceApp(Map marathonApp, List<String> allAppNames) throws Exception {
        String id = marathonApp.get("id").toString();
        id = id.replaceFirst("/", "");
        id = replaceLast(id, "-dev", "");
        id = replaceLast(id, "-stg", "");
        id = replaceLast(id, "-blue", "");
        id = replaceLast(id, "-green", "");
        return !allAppNames.contains(id);
    }

    private String replaceLast(String text, String regex, String replacement) {
        return text.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }

    public void saveTasksByNode(List<Map> apps) throws Exception {
        if (apps == null) {
            return;
        }
        Map<String, List<Map>> map = new HashMap();

        for (Map app : apps) {
            List<Map> tasks = (List<Map>) app.get("tasks");
            if (!tasks.isEmpty()) {
                for (Map task : tasks) {
                    String slaveId = task.get("slaveId").toString();
                    if (!map.containsKey(slaveId)) {
                        map.put(slaveId, new ArrayList<>());
                    }
                    task.put("cpus", app.get("cpus"));
                    task.put("mem", app.get("mem"));
                    task.put("disk", app.get("disk"));
                    map.get(slaveId).add(task);
                }
            }
        }
        if (!map.isEmpty()) {
            for (Map.Entry<String, List<Map>> entry : map.entrySet()) {
                String slaveId = entry.getKey();
                marathonCacheService.updateTasksPerNodeCache(slaveId, entry.getValue());
            }
        }
    }
}
