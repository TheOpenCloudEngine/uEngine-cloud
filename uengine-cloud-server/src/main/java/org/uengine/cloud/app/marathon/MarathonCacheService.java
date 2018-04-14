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
    //TODO need redis -> websocket
    @CachePut(value = "marathonApp", key = "#marathonAppId")
    @Transactional
    public Map updateMarathonAppByIdCache(String marathonAppId) throws Exception {
        LOGGER.info("update Marathon App to redis, {}", marathonAppId);
        return dcosApi.getApp(marathonAppId);
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
        return dcosApi.getApp(marathonAppId);
    }

    /**
     * 서비스 앱 리스트 캐쉬를 가져온다.
     *
     * @return
     * @throws Exception
     */
    @Cacheable(value = "serviceApps", key = "'serviceApps'")
    public List<Map> getServiceAppsCache() throws Exception {
        LOGGER.info("get Marathon service Apps from dcos");
        return getServiceApps();
    }

    /**
     * 서비스 앱 리스트 캐쉬를 갱신한다.
     *
     * @return
     * @throws Exception
     */
    //From AppScheduler
    @CachePut(value = "serviceApps", key = "'serviceApps'")
    @Transactional
    public List<Map> updateServiceAppsCache() throws Exception {
        LOGGER.info("update Marathon service Apps to redis");
        return getServiceApps();
    }

    /**
     * 서비스 앱 리스트를 가져온다.
     *
     * @return
     * @throws Exception
     */
    private List<Map> getServiceApps() throws Exception {
        Map groups = dcosApi.getGroups();
        List<String> allAppNames = appWebCacheService.findAllAppNamesCache();
        List<Map> apps = (List<Map>) groups.get("apps");
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
}
