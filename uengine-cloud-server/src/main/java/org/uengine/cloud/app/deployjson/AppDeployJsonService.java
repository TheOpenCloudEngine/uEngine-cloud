package org.uengine.cloud.app.deployjson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.uengine.cloud.app.*;
import org.uengine.cloud.app.emitter.AppEntityBaseMessageHandler;
import org.uengine.cloud.app.emitter.AppEntityBaseMessageTopic;

import java.util.*;

/**
 * Created by uengine on 2017. 11. 16..
 */
@Service
public class AppDeployJsonService {

    @Autowired
    private Environment environment;

    @Autowired
    private AppDeployJsonRepository deployJsonRepository;

    @Autowired
    private AppDeployJsonCacheService deployJsonCacheService;

    @Autowired
    private AppEntityBaseMessageHandler messageHandler;

    @Autowired
    private AppWebCacheService appWebCacheService;

    /**
     * 어플리케이션의 주어진 스테이지에 따른 배포 정보를 반환한다.
     *
     * @param appName
     * @param stage   prod,stg,dev
     * @return
     * @throws Exception
     */
    public Map getDeployJson(String appName, String stage) throws Exception {
        return deployJsonCacheService.getDeployJsonCache(appName, stage);
    }

    /**
     * 어플리케이션의 모든 배포 정보를 반환한다.
     *
     * @param appName
     * @return
     * @throws Exception
     */
    public Map getAllDeployJson(String appName) throws Exception {
        Map map = new HashMap();
        map.put("dev", deployJsonCacheService.getDeployJsonCache(appName, "dev"));
        map.put("stg", deployJsonCacheService.getDeployJsonCache(appName, "stg"));
        map.put("prod", deployJsonCacheService.getDeployJsonCache(appName, "prod"));
        return map;
    }

    /**
     * 어플리케이션의 주어진 스테이지에 따른 배포 정보를 업데이트한다.
     *
     * @param appName
     * @param stage
     * @param deployJson
     * @throws Exception
     */
    public Map updateDeployJson(String appName, String stage, Map deployJson) throws Exception {
        Map json = deployJsonCacheService.updateDeployJsonCache(appName, stage, deployJson);

        //변경 알림
        AppEntity appEntity = appWebCacheService.findOneCache(appName);
        messageHandler.publish(AppEntityBaseMessageTopic.deployJson, appEntity, stage, json);
        return json;
    }

    /**
     * 어플리케이션의 배포 정보를 삭제한다.
     *
     * @param appName
     * @throws Exception
     */
    public void deleteAppDeployJson(String appName) throws Exception {
        deployJsonRepository.removeByAppName(appName);
    }
}
