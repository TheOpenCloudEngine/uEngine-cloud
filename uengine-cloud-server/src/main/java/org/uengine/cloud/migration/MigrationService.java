package org.uengine.cloud.migration;

import org.gitlab4j.api.GitLabApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.uengine.cloud.app.*;
import org.uengine.cloud.app.deployjson.AppDeployJsonService;
import org.uengine.cloud.app.git.GitlabExtentApi;
import org.uengine.cloud.app.git.HookController;
import org.uengine.cloud.app.marathon.DcosApi;
import org.uengine.cloud.app.pipeline.AppPipeLineService;
import org.uengine.iam.client.IamClient;
import org.uengine.iam.util.JsonUtils;

import java.util.*;

/**
 * Created by uengine on 2017. 11. 16..
 */
@Service
public class MigrationService {

    @Autowired
    private Environment environment;

    @Autowired
    private GitLabApi gitLabApi;

    @Autowired
    private GitlabExtentApi gitlabExtentApi;

    @Autowired
    private AppWebCacheService appWebCacheService;

    @Autowired
    private DcosApi dcosApi;

    @Autowired
    private HookController hookController;

    @Autowired
    private AppEntityRepository appEntityRepository;

    @Autowired
    private AppDeployJsonService deployJsonService;

    @Autowired
    private AppPipeLineService appPipeLineService;

    @Autowired
    private IamClient iamClient;

    public void migration() throws Exception {

        List<AppEntity> appEntities = appEntityRepository.findAll();

        for (AppEntity appEntity : appEntities) {

            //디플로이 파일
            String[] stages = new String[]{"dev", "stg", "prod"};
            int repoId = Integer.parseInt(environment.getProperty("gitlab.config-repo.projectId"));
            for (String stage : stages) {
                String deployJsonfilePath = "";
                if (stage.equals("dev")) {
                    deployJsonfilePath = "deployment/" + appEntity.getName() + "/ci-deploy-dev.json";
                } else if (stage.equals("stg")) {
                    deployJsonfilePath = "deployment/" + appEntity.getName() + "/ci-deploy-staging.json";
                } else if (stage.equals("prod")) {
                    deployJsonfilePath = "deployment/" + appEntity.getName() + "/ci-deploy-production.json";
                }

                String content = gitlabExtentApi.getRepositoryFile(repoId, "master", deployJsonfilePath);
                Map map = JsonUtils.unmarshal(content);
                deployJsonService.updateDeployJson(appEntity.getName(), stage, map);
            }

            //파이프라인 파일
            String pipelineFilePath = "deployment/" + appEntity.getName() + "/ci-pipeline.json";
            String content = gitlabExtentApi.getRepositoryFile(repoId, "master", pipelineFilePath);
            Map map = JsonUtils.unmarshal(content);
            appPipeLineService.updatePipeLineJson(appEntity.getName(), map);


            //멤버 업데이트
            appWebCacheService.updateAppMemberCache(appEntity.getName());
        }
    }
}
