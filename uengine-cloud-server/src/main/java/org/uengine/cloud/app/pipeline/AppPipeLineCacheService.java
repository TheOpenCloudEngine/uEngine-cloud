package org.uengine.cloud.app.pipeline;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Pipeline;
import org.gitlab4j.api.models.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.uengine.cloud.app.AppEntity;
import org.uengine.cloud.app.AppWebCacheService;
import org.uengine.cloud.app.git.GitMirrorService;
import org.uengine.cloud.app.marathon.DcosApi;
import org.uengine.iam.util.JsonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by uengine on 2017. 11. 16..
 */
@Service
public class AppPipeLineCacheService {

    @Autowired
    private GitLabApi gitLabApi;

    @Autowired
    private GitMirrorService gitMirrorService;

    @Autowired
    private AppWebCacheService appWebCacheService;

    private static final Logger LOGGER = LoggerFactory.getLogger(AppPipeLineCacheService.class);

    /**
     * 앱의 마지막 파이프라인 결과를 저장한다.
     *
     * @param appName
     * @return
     * @throws Exception
     */
    @CachePut(value = "pipeline", key = "#appName + '-pipeline'")
    @Transactional
    public AppLastPipeLine updateLastPipeline(String appName) throws Exception {
        LOGGER.info("updateLastPipeline to redis, {}", appName);
        return this.getLastPipelineFromGit(appName);
    }

    /**
     * 앱의 마지막 파이프라인 결과를 가져온다.
     *
     * @param appName
     * @return
     * @throws Exception
     */
    @Cacheable(value = "pipeline", key = "#appName + '-pipeline'")
    public AppLastPipeLine getLastPipeline(String appName) throws Exception {
        LOGGER.info("getLastPipeline from gitlab, {}", appName);
        return this.getLastPipelineFromGit(appName);
    }

    private AppLastPipeLine getLastPipelineFromGit(String appName) {
        try {
            AppEntity appEntity = appWebCacheService.findOneCache(appName);

            List<Pipeline> pipelines = gitLabApi.getPipelineApi().getPipelines(appEntity.getProjectId(), 1, 1);
            if (!pipelines.isEmpty()) {
                AppLastPipeLine lastPipeLine = new AppLastPipeLine(appName, pipelines.get(0));
                return lastPipeLine;
            }
            return new AppLastPipeLine();
        } catch (Exception ex) {
            return new AppLastPipeLine();
        }
    }

    /**
     * 앱의 마지막 미러 파이프라인 결과를 저장한다.
     *
     * @param appName
     * @return
     * @throws Exception
     */
    @CachePut(value = "pipeline", key = "#appName + '-mirror-pipeline'")
    @Transactional
    public AppLastPipeLine updateLastMirrorPipeline(String appName) throws Exception {
        LOGGER.info("updateLastMirrorPipeline to redis, {}", appName);
        return this.getLastMirrorPipelineFromGit(appName);
    }

    /**
     * 앱의 마지막 미러 파이프라인 결과를 가져온다.
     *
     * @param appName
     * @return
     * @throws Exception
     */
    @Cacheable(value = "pipeline", key = "#appName + '-mirror-pipeline'")
    public AppLastPipeLine getLastMirrorPipeline(String appName) throws Exception {
        LOGGER.info("getLastMirrorPipeline from gitlab, {}", appName);
        return this.getLastMirrorPipelineFromGit(appName);
    }

    private AppLastPipeLine getLastMirrorPipelineFromGit(String appName) {
        try {
            Project mirrorProject = gitMirrorService.getMirrorProject(appName);

            List<Pipeline> pipelines = gitLabApi.getPipelineApi().getPipelines(mirrorProject.getId(), 1, 1);
            if (!pipelines.isEmpty()) {
                AppLastPipeLine lastPipeLine = new AppLastPipeLine(appName, pipelines.get(0));
                return lastPipeLine;
            }
            return new AppLastPipeLine();
        } catch (Exception ex) {
            return new AppLastPipeLine();
        }
    }
}
