package org.uengine.cloud.app.git;

import org.apache.http.util.Asserts;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.uengine.cloud.app.AppEntity;
import org.uengine.cloud.app.AppEntityRepository;
import org.uengine.cloud.app.AppWebCacheService;
import org.uengine.cloud.app.config.AppConfigService;
import org.uengine.cloud.app.emitter.AppEntityBaseMessageHandler;
import org.uengine.cloud.app.emitter.AppEntityBaseMessageTopic;
import org.uengine.cloud.app.log.AppLogAction;
import org.uengine.cloud.app.log.AppLogService;
import org.uengine.cloud.app.log.AppLogStatus;
import org.uengine.cloud.app.pipeline.AppLastPipeLine;
import org.uengine.cloud.app.pipeline.AppPipeLineCacheService;
import org.uengine.cloud.app.pipeline.AppPipeLineService;
import org.uengine.cloud.catalog.CatalogCacheService;
import org.uengine.iam.client.IamClient;
import org.uengine.iam.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HookService {

    @Autowired
    private AppConfigService appConfigService;

    @Autowired
    private GitlabExtentApi gitlabExtentApi;

    @Autowired
    private AppLogService logService;

    @Autowired
    private AppPipeLineService pipeLineService;

    @Autowired
    private AppPipeLineCacheService pipeLineCacheService;

    @Autowired
    private AppEntityBaseMessageHandler messageHandler;

    @Autowired
    private AppWebCacheService appWebCacheService;

    @Autowired
    private AppEntityRepository appEntityRepository;

    @Autowired
    private GitLabApi gitLabApi;

    @Autowired
    private CatalogCacheService catalogCacheService;

    @Autowired
    private GitMirrorService gitMirrorService;

    private static final String RESERVED_STAGE = "RESERVED_STAGE";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private HashOperations hashOperations;

    private static final Logger LOGGER = LoggerFactory.getLogger(HookService.class);

    @PostConstruct
    private void init() {
        hashOperations = redisTemplate.opsForHash();
    }

    public void addReservedStage(String pipelineId, String stage) {
        LOGGER.info("addReservedStage {} {}", pipelineId, stage);

        hashOperations.put(RESERVED_STAGE, pipelineId, stage);
    }

    public void removeReservedStage(String pipelineId) {
        LOGGER.info("removeReservedStage {}", pipelineId);

        try {
            hashOperations.delete(RESERVED_STAGE, pipelineId);
        } catch (Exception ex) {

        }
    }

    public String getReservedStage(String pipeline) {
        try {
            return (String) hashOperations.get(RESERVED_STAGE, pipeline);
        } catch (Exception ex) {
            return null;
        }
    }

    public void receiveGithubPushEventHook(Map payloads) throws Exception {
        Integer id = (Integer) ((Map) payloads.get("repository")).get("id");
        Long repoId = Long.valueOf(id);

        String commit = payloads.get("after").toString();

        LOGGER.info("receiveGithubPushEventHook {}, commit {} ", repoId, commit);

        AppEntity appEntity = appEntityRepository.findByGithubRepoId(repoId);
        Assert.notNull(appEntity, "Not found appEntity for given github repo.");

        gitMirrorService.syncGithubToGitlab(appEntity.getName(), commit);
    }

    public void receivePipeLineEventHook(Map payloads) throws Exception {
        //running, pending 은 진행중
        //success, failed, canceled, skipped 인 경우는 이력에 저장.
        int projectId = (int) ((Map) payloads.get("project")).get("id");
        AppEntity appEntity = appEntityRepository.findByProjectId(projectId);
        String appName = appEntity.getName();
        String pipelineId = ((Map) payloads.get("object_attributes")).get("id").toString();
        String status = ((Map) payloads.get("object_attributes")).get("status").toString();

        LOGGER.info("receivePipeLineEventHook {}, {}", projectId, appName);

        //파이프라인 캐쉬 업데이트
        AppLastPipeLine lastPipeLine = pipeLineCacheService.updateLastPipeline(appName);

        //파이프라인 변경 알림
        messageHandler.publish(AppEntityBaseMessageTopic.pipeline, appEntity, null, lastPipeLine);

        //이력 저장
        logService.addHistory(appName, AppLogAction.PIPELINE, AppLogStatus.valueOf(status.toUpperCase()), null);

        Map pipeLineJson = pipeLineService.getPipeLineJson(appName);
        List<String> autoDeploys = (List<String>) pipeLineJson.get("auto-deploy");

        List<Map> builds = (List<Map>) payloads.get("builds");
        for (Map build : builds) {

            String reservedStage = this.getReservedStage(pipelineId);
            //예약된 스테이지 배포인 경우
            if (reservedStage != null && build.get("status").equals("manual")) {

                LOGGER.info("reservedStage {} found for manual job name {}", reservedStage, build.get("name").toString());

                if (build.get("name").toString().equals(reservedStage)) {
                    int jobId = (int) build.get("id");

                    LOGGER.info("continue playJob {}", jobId);
                    gitlabExtentApi.playJob(projectId, jobId);

                    //예약된 스테이지 배포 삭제
                    this.removeReservedStage(pipelineId);

                    //후처리 작업
                    this.afterDeployedByCI(appName, build.get("name").toString());
                }
            }
            //매뉴얼 단계이면서 auto 빌드에 속한 경우
            else if (autoDeploys.indexOf(build.get("name").toString()) != -1 && build.get("status").equals("manual")) {

                LOGGER.info("autoDeploys found for manual job name {}", build.get("name").toString());

                int jobId = (int) build.get("id");
                LOGGER.info("continue playJob {}", jobId);
                gitlabExtentApi.playJob(projectId, jobId);

                //후처리 작업
                this.afterDeployedByCI(appName, build.get("name").toString());
            }
        }
    }

    public void receiveMirrorPipeLineEventHook(Map payloads) throws Exception {
        //running, pending 은 진행중
        //success, failed, canceled, skipped 인 경우는 이력에 저장.
        String mirrorProjectName = (String) ((Map) payloads.get("project")).get("name");
        int mirrorProjectId = (int) ((Map) payloads.get("project")).get("id");
        String appName = mirrorProjectName.replace(gitMirrorService.getMirrorProjectPrefix(), "");
        AppEntity appEntity = appEntityRepository.findOne(appName);

        String status = ((Map) payloads.get("object_attributes")).get("status").toString();

        LOGGER.info("receiveMirrorPipeLineEventHook {}, {}", mirrorProjectId, appName);

        //파이프라인 캐쉬 업데이트
        AppLastPipeLine lastPipeLine = pipeLineCacheService.updateLastMirrorPipeline(appName);

        //파이프라인 변경 알림
        messageHandler.publish(AppEntityBaseMessageTopic.mirrorPipeline, appEntity, null, lastPipeLine);

        //이력 저장
        logService.addHistory(appName, AppLogAction.MIRROR_PIPELINE, AppLogStatus.valueOf(status.toUpperCase()), null);
    }

    public void receivePushEventHook(Map payloads) throws Exception {
        int projectId = (int) ((Map) payloads.get("project")).get("id");
        AppEntity appEntity = appEntityRepository.findByProjectId(projectId);
        String appName = appEntity.getName();

        LOGGER.info("receivePushEventHook {} , {}", projectId, appName);

        //푸시 이력 남기기
        logService.addHistory(appName, AppLogAction.PUSH, AppLogStatus.SUCCESS, null);

        Map pipeLineJson = pipeLineService.getPipeLineJson(appName);
        List<String> refs = (List<String>) pipeLineJson.get("refs");
        String commitRef = payloads.get("ref").toString();

        //when 이 commit 일때는 푸쉬때마다 허용. manual 일때는 ui 로만 가능.
        if (pipeLineJson.get("when").toString().equals("commit")) {

            //커밋된 ref 가 허용 refs 에 들어있는 항목인 경우 수락.
            boolean enable = false;
            for (String ref : refs) {
                if (payloads.get("ref").toString().indexOf(ref) != -1) {
                    enable = true;
                }
            }
            if (enable) {
                pipeLineService.excutePipelineTrigger(appName, commitRef, null);
            }
        }
    }

    /**
     * 깃랩 프로젝트에 유저가 추가/삭제 되었을 때 핸들러.
     *
     * @param payloads
     */
    public void receiveUserTeamEventHook(Map payloads) {
        int project_id = (int) payloads.get("project_id");
        AppEntity appEntity = appEntityRepository.findByProjectId(project_id);
        if (appEntity != null) {
            appWebCacheService.updateAppMemberCache(appEntity.getName());
        }
    }

    /**
     * 깃랩 그룹에 유저가 추가/삭제 되었을 때 핸들러.
     *
     * @param payloads
     * @throws Exception
     */
    public void receiveUserGroupEventHook(Map payloads) throws Exception {
        int group_id = (int) payloads.get("group_id");
        List<Project> projects = gitLabApi.getGroupApi().getProjects(group_id);
        for (int i = 0; i < projects.size(); i++) {
            AppEntity appEntity = appEntityRepository.findByProjectId(projects.get(i).getId());
            if (appEntity != null) {
                appWebCacheService.updateAppMemberCache(appEntity.getName());
            }
        }
    }

    public void receiveProjectCreateHook(Map payloads) throws Exception {
        if (payloads.get("name").toString().startsWith("template")) {
            catalogCacheService.updateCategoriesCache();
        }
    }

    public void receiveRepositoryUpdateHook(Map payloads) throws Exception {
        Map project = (Map) payloads.get("project");
        if (project.get("name").toString().startsWith("template")) {
            catalogCacheService.updateCategoriesCache();
        }
    }

    //앱 변경 적용
    private void afterDeployedByCI(String appName, String buildName) throws Exception {

        if (StringUtils.isEmpty(buildName)) {
            return;
        }
        String stage = "";
        switch (buildName) {
            case "dev":
                stage = "dev";
                break;
            case "staging":
                stage = "stg";
                break;
            case "production":
                stage = "prod";
                break;
        }

        //로그 추가
        Map map = new HashMap();
        map.put("stage", stage);
        logService.addHistory(appName, AppLogAction.START_DEPLOYED_BY_CI, AppLogStatus.SUCCESS, map);

        //콘피드 변경 해제
        appConfigService.updateAppConfigChanged(appName, stage, false);
    }
}
