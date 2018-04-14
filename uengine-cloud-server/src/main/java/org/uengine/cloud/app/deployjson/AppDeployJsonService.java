package org.uengine.cloud.app.deployjson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.uengine.cloud.app.*;

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

    /**
     * 어플리케이션의 주어진 스테이지에 따른 배포 정보를 반환한다.
     *
     * @param appName
     * @param stage   prod,stg,dev
     * @return
     * @throws Exception
     */
    public Map getDeployJson(String appName, String stage) throws Exception {
        return deployJsonRepository.findByAppNameAndStage(appName, stage).getJson();
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
        map.put("dev", deployJsonRepository.findByAppNameAndStage(appName, "dev").getJson());
        map.put("stg", deployJsonRepository.findByAppNameAndStage(appName, "stg").getJson());
        map.put("prod", deployJsonRepository.findByAppNameAndStage(appName, "prod").getJson());
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
        AppDeployJson appDeployJson = deployJsonRepository.findByAppNameAndStage(appName, stage);
        if (appDeployJson == null) {
            appDeployJson = new AppDeployJson();
            appDeployJson.setAppName(appName);
            appDeployJson.setStage(stage);
            appDeployJson.setJson(deployJson);
        }
        return deployJsonRepository.save(appDeployJson).getJson();
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
