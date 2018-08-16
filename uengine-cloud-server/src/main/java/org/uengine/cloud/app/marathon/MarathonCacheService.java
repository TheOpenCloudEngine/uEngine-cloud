package org.uengine.cloud.app.marathon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uengine.cloud.app.AppEntity;
import org.uengine.cloud.app.AppWebCacheService;
import org.uengine.cloud.app.AppWebService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by uengine on 2017. 11. 16..
 */
@Service
public class MarathonCacheService {

    @Autowired
    private DcosApi dcosApi;

    @Autowired
    private AppWebCacheService appWebCacheService;

    private static final Logger LOGGER = LoggerFactory.getLogger(MarathonCacheService.class);

    /**
     * dcosLast 정보를 업데이트 한다.
     *
     * @return
     * @throws Exception
     */
    //From AppScheduler
    @CachePut(value = "dcosLast", key = "'dcosLast'")
    @Transactional
    public Map updateDcosLastCache() throws Exception {
        LOGGER.info("update dcosLast redis");
        return dcosApi.getLast();
    }

    /**
     * dcosLast 정보를 가져온다.
     *
     * @return
     * @throws Exception
     */
    @Cacheable(value = "dcosLast", key = "'dcosLast'")
    public Map getDcosLastCache() throws Exception {
        LOGGER.info("get dcosLast from dcos");
        return dcosApi.getLast();
    }

    /**
     * 마라톤 캐쉬 앱을 업데이트 한다.
     *
     * @param marathonAppId
     * @return
     * @throws Exception
     */
    //TODO need from kafka event
    //status_update_event
    //health_status_changed_event
    //TODO need redis -> sse
    @CachePut(value = "marathonApp", key = "#marathonAppId")
    @Transactional
    public Map updateMarathonAppByIdCache(String marathonAppId) throws Exception {
        LOGGER.info("update Marathon App to redis, {}", marathonAppId);
        Map app = dcosApi.getApp(marathonAppId);
        if(app == null){
            return new HashMap();
        }else{
            return app;
        }
    }

    /**
     * 마라톤 캐쉬 앱을 가져온다.
     *
     * @param marathonAppId
     * @return
     * @throws Exception
     */
    @Cacheable(value = "marathonApp", key = "#marathonAppId")
    public Map getMarathonAppByIdCache(String marathonAppId) throws Exception {
        LOGGER.info("get Marathon App from dcos, {}", marathonAppId);
        return dcosApi.getApp(marathonAppId) == null ? new HashMap() : dcosApi.getApp(marathonAppId);
    }

    /**
     * 마라톤 앱 리스트 캐쉬를 가져온다.
     *
     * @return
     * @throws Exception
     */
    @Cacheable(value = "marathonApps", key = "'serviceApps'")
    public List<Map> getMarathonAppsCache() throws Exception {
        LOGGER.info("get Marathon Apps from dcos");
        Map groups = dcosApi.getGroups();
        return (List<Map>) groups.get("apps");
    }

    /**
     * 마라톤 앱 리스트 캐쉬를 갱신한다.
     *
     * @return
     * @throws Exception
     */
    //From AppScheduler
    @CachePut(value = "serviceApps", key = "'serviceApps'")
    @Transactional
    public List<Map> updateMarathonAppsCache() throws Exception {
        LOGGER.info("update Marathon service Apps to redis");
        Map groups = dcosApi.getGroups();
        return (List<Map>) groups.get("apps");
    }


    /**
     * 슬레이브 별 타스크 리스트를 업데이트 한다.
     *
     * @param salveId
     * @param tasks
     * @return
     * @throws Exception
     */
    @CachePut(value = "marathonAppsPerNode", key = "#salveId")
    @Transactional
    public List<Map> updateTasksPerNodeCache(String salveId, List<Map> tasks) throws Exception {
        LOGGER.info("update Tasks per node, {}", salveId);
        return tasks;
    }

    /**
     * 슬레이브 별 타스크 리스트를 가져온다.
     *
     * @param salveId
     * @return
     * @throws Exception
     */
    @Cacheable(value = "marathonAppsPerNode", key = "#salveId")
    public List<Map> getTasksPerNodeCache(String salveId) {
        LOGGER.info("get tasks per node return empty list cause by redis empty.");
        return new ArrayList<>();
    }

}
