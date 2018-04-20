package org.uengine.cloud.app.deployjson;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Pipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uengine.cloud.app.AppDeployJson;
import org.uengine.cloud.app.AppEntity;
import org.uengine.cloud.app.AppWebCacheService;
import org.uengine.cloud.app.pipeline.AppLastPipeLine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by uengine on 2017. 11. 16..
 */
@Service
public class AppDeployJsonCacheService {

    @Autowired
    private AppDeployJsonRepository deployJsonRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(AppDeployJsonCacheService.class);

    /**
     * 어플리케이션의 배포 정보 캐쉬를 가져온다.
     *
     * @param appName
     * @param stage   prod,stg,dev
     * @return
     * @throws Exception
     */
    @Cacheable(value = "deployJson", key = "#appName + #stage + '-deployJson-'")
    public Map getDeployJsonCache(String appName, String stage) throws Exception {
        return deployJsonRepository.findByAppNameAndStage(appName, stage).getJson();
    }


    /**
     * 어플리케이션의 배포 정보 캐쉬를 업데이트 한다.
     *
     * @param appName
     * @param stage
     * @param deployJson
     * @throws Exception
     */
    @CachePut(value = "deployJson", key = "#appName + #stage + '-deployJson-'")
    @Transactional
    public Map updateDeployJsonCache(String appName, String stage, Map deployJson) throws Exception {
        AppDeployJson appDeployJson = deployJsonRepository.findByAppNameAndStage(appName, stage);
        if (appDeployJson == null) {
            appDeployJson = new AppDeployJson();
            appDeployJson.setAppName(appName);
            appDeployJson.setStage(stage);
            appDeployJson.setJson(deployJson);
        } else {
            appDeployJson.setJson(deployJson);
        }
        return deployJsonRepository.save(appDeployJson).getJson();
    }
}
