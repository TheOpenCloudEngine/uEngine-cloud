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
}
