package org.uengine.cloud.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.uengine.cloud.app.*;
import org.uengine.cloud.app.git.GitlabExtentApi;
import org.uengine.iam.util.HttpUtils;
import org.uengine.iam.util.JsonUtils;
import org.uengine.iam.util.StringUtils;

import java.util.*;

/**
 * Created by uengine on 2017. 11. 16..
 */
@Service
public class AppConfigService {
    @Autowired
    private Environment environment;

    @Autowired
    private GitlabExtentApi gitlabExtentApi;

    @Autowired
    private AppWebService appWebService;

    private static final Logger LOGGER = LoggerFactory.getLogger(AppConfigService.class);

    /**
     * vcap 서비스에 앱의 주소를 등록한다. 요청된 스테이지에 한해 수행한다.
     *
     * @param appEntity
     * @param stages
     * @throws Exception
     */
    public void addAppToVcapService(AppEntity appEntity, List<String> stages) throws Exception {
        String applicationYml = this.getApplicationYml();

        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Map vcapMap = yamlReader.readValue(applicationYml, Map.class);
        Map vcaps = (Map) ((Map) vcapMap.get("vcap")).get("services");
        Map service = new HashMap();

        //기존 vcap 서비스에 서비스가 있다면, 기존 서비스맵으로 대체한다.
        if (vcaps.containsKey(appEntity.getName())) {
            service = (Map) vcaps.get(appEntity.getName());
        }

        for (String stage : stages) {
            Map map = new HashMap();
            switch (stage) {
                case "dev":
                    map.put("external", appEntity.getDev().getExternal());
                    map.put("internal", appEntity.getDev().getInternal());
                    break;
                case "stg":
                    map.put("external", appEntity.getStg().getExternal());
                    map.put("internal", appEntity.getStg().getInternal());
                    break;
                case "prod":
                    map.put("external", appEntity.getProd().getExternal());
                    map.put("internal", appEntity.getProd().getInternal());
                    break;
            }
            service.put(stage, map);
        }

        vcaps.put(appEntity.getName(), service);

        applicationYml = yamlReader.writeValueAsString(vcapMap);
        gitlabExtentApi.updateOrCraeteRepositoryFile(
                Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId")),
                "master", "application.yml", applicationYml);
    }

    /**
     * vcap 서비스에 앱을 추가한다.
     *
     * @param appName
     * @throws Exception
     */
    public void addAppToVcapService(String appName) throws Exception {
        String[] stages = {"dev", "stg", "prod"};
        this.addAppToVcapService(appWebService.findOne(appName), Arrays.asList(stages));
    }

    /**
     * vcap 서비스에서 앱을 삭제한다.
     *
     * @param appName
     * @throws Exception
     */
    public void removeAppToVcapService(String appName) throws Exception {
        String applicationYml = this.getApplicationYml();
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Map vcapMap = yamlReader.readValue(applicationYml, Map.class);
        Map vcaps = (Map) ((Map) vcapMap.get("vcap")).get("services");
        vcaps.remove(appName);

        applicationYml = yamlReader.writeValueAsString(vcapMap);
        gitlabExtentApi.updateOrCraeteRepositoryFile(
                Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId")),
                "master", "application.yml", applicationYml);
    }

    /**
     * vcap 서비스 파일을 가져온다.
     *
     * @return
     * @throws Exception
     */
    public String getApplicationYml() throws Exception {
        int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
        String filePath = "application.yml";
        return gitlabExtentApi.getRepositoryFile(repoId, "master", filePath);
    }

    public void createAppConfigYml(String appName, String common, String dev, String stg, String prod) throws Exception {
        String defaultString = "---\n" +
                "# =================================================\n" +
                "# The common configuration file will be overwritten\n" +
                "# =================================================";

        int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
        gitlabExtentApi.updateOrCraeteRepositoryFile(repoId,
                "master", appName + ".yml", common);

        gitlabExtentApi.updateOrCraeteRepositoryFile(repoId,
                "master", appName + "-dev.yml", dev);

        gitlabExtentApi.updateOrCraeteRepositoryFile(repoId,
                "master", appName + "-stg.yml", stg);

        gitlabExtentApi.updateOrCraeteRepositoryFile(repoId,
                "master", appName + "-prod.yml", prod);
    }

    public String updateAppConfigYml(String appName, String content, String stage) throws Exception {
        int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
        if (StringUtils.isEmpty(stage)) {
            gitlabExtentApi.updateOrCraeteRepositoryFile(repoId,
                    "master", appName + ".yml", content);
        } else {
            gitlabExtentApi.updateOrCraeteRepositoryFile(repoId,
                    "master", appName + "-" + stage + ".yml", content);
        }
        this.updateAppConfigChanged(appName, stage, true);
        return content;
    }

    public String getAppConfigYml(String appName, String stage) throws Exception {
        int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
        if (StringUtils.isEmpty(stage)) {
            return gitlabExtentApi.getRepositoryFile(repoId,
                    "master", appName + ".yml");
        } else {
            return gitlabExtentApi.getRepositoryFile(repoId,
                    "master", appName + "-" + stage + ".yml");
        }
    }

    public void removeAppConfigYml(String appName) throws Exception {
        int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
        gitlabExtentApi.removeRepositoryFile(repoId,
                "master", appName + ".yml");
        gitlabExtentApi.removeRepositoryFile(repoId,
                "master", appName + "-dev.yml");
        gitlabExtentApi.removeRepositoryFile(repoId,
                "master", appName + "-stg.yml");
        gitlabExtentApi.removeRepositoryFile(repoId,
                "master", appName + "-prod.yml");
    }


    public void updateAppConfigChanged(String appName, String stage, boolean isChanged) throws Exception {
        //스테이지가 없으면 모든 스테이지가 변화된 것.
        AppEntity appEntity = appWebService.findOne(appName);
        if (StringUtils.isEmpty(stage)) {
            AppStage dev = appEntity.getDev();
            dev.setConfigChanged(true);

            AppStage stg = appEntity.getStg();
            stg.setConfigChanged(true);

            AppStage prod = appEntity.getProd();
            prod.setConfigChanged(true);

            appEntity.setDev(dev);
            appEntity.setStg(stg);
            appEntity.setProd(prod);
        } else {
            AppStage appStage = null;
            switch (stage) {
                case "dev":
                    appStage = appEntity.getDev();
                    if (isChanged) {
                        appStage.setConfigChanged(true);
                    } else {
                        appStage.setConfigChanged(false);
                    }
                    appEntity.setDev(appStage);
                    break;
                case "stg":
                    appStage = appEntity.getStg();
                    if (isChanged) {
                        appStage.setConfigChanged(true);
                    } else {
                        appStage.setConfigChanged(false);
                    }
                    appEntity.setStg(appStage);
                    break;
                case "prod":
                    appStage = appEntity.getProd();
                    if (isChanged) {
                        appStage.setConfigChanged(true);
                    } else {
                        appStage.setConfigChanged(false);
                    }
                    appEntity.setProd(appStage);
                    break;
            }
        }
        appWebService.save(appEntity);
    }

    public Map getOriginalCloudConfigJson(String appName, String stage) throws Exception {
        HttpResponse response = new HttpUtils().makeRequest("GET",
                "http://" + environment.getProperty("vcap.services.uengine-cloud-config.external") + "/" + appName + "-" + stage + ".json",
                null,
                new HashMap<>()
        );
        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity);
        return JsonUtils.unmarshal(json);
    }

}
