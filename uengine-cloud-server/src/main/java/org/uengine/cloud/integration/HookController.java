package org.uengine.cloud.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.uengine.cloud.app.AppEntity;
import org.uengine.cloud.app.AppJpaRepository;
import org.uengine.cloud.app.AppService;
import org.uengine.cloud.integration.GitlabExtentApi;
import org.uengine.cloud.log.AppLogAction;
import org.uengine.cloud.log.AppLogService;
import org.uengine.cloud.log.AppLogStatus;
import org.uengine.iam.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by uengine on 2017. 11. 20..
 */
@RestController
public class HookController {

    @Autowired
    private AppService appService;

    @Autowired
    private GitlabExtentApi gitlabExtentApi;

    @Autowired
    private AppLogService logService;

    @Autowired
    private AppJpaRepository appJpaRepository;

    private Map<String, String> reservedStages = new HashMap<>();

    public void addReservedStage(String pipelineId, String stage) {
        reservedStages.put(pipelineId, stage);
    }

    public void removeReservedStage(String pipelineId) {
        reservedStages.remove("pipelineId");
    }

    @RequestMapping(value = "/hook", method = RequestMethod.POST)
    public void getGitlabHook(HttpServletRequest request,
                              HttpServletResponse response,
                              @RequestBody Map payloads) throws Exception {
        //커밋이 올 경우 파이프라인을 실행한다.

        //파이프라인이 올 경우, 빌드 별로 메뉴얼을 추린다.

        //일단, 오는 것들을 보자.
        try {
            if (payloads.get("object_kind").toString().equals("pipeline")) {

                //running, pending 은 진행중
                //success, failed, canceled, skipped 인 경우는 이력에 저장.
                String appName = ((Map) payloads.get("project")).get("name").toString();
                String pipelineId = ((Map) payloads.get("object_attributes")).get("id").toString();
                String status = ((Map) payloads.get("object_attributes")).get("status").toString();

                AppEntity appEntity = appJpaRepository.findOne(appName);

                //이력 저장
                logService.addHistory(appName, AppLogAction.PIPELINE, AppLogStatus.valueOf(status.toUpperCase()), null);

                int projectId = appEntity.getProjectId();
                Map pipeLineJson = appService.getPipeLineJson(appName);
                List<String> autoDeploys = (List<String>) pipeLineJson.get("auto-deploy");

                List<Map> builds = (List<Map>) payloads.get("builds");
                for (Map build : builds) {

                    //예약된 스테이지 배포인 경우
                    if (reservedStages.containsKey(pipelineId) && build.get("status").equals("manual")) {
                        if (build.get("name").toString().equals(reservedStages.get(pipelineId))) {
                            int jobId = (int) build.get("id");
                            gitlabExtentApi.playJob(projectId, jobId);

                            //예약된 스테이지 배포 삭제
                            this.removeReservedStage(pipelineId);

                            //후처리 작업
                            this.afterDeployedByCI(appName, build.get("name").toString());
                        }
                    }
                    //매뉴얼 단계이면서 auto 빌드에 속한 경우
                    else if (autoDeploys.indexOf(build.get("name").toString()) != -1 && build.get("status").equals("manual")) {
                        int jobId = (int) build.get("id");
                        gitlabExtentApi.playJob(projectId, jobId);

                        //후처리 작업
                        this.afterDeployedByCI(appName, build.get("name").toString());
                    }
                }
            } else if (payloads.get("object_kind").toString().equals("push")) {
                String appName = ((Map) payloads.get("project")).get("name").toString();
                AppEntity appEntity = appJpaRepository.findOne(appName);
                int projectId = appEntity.getProjectId();

                //푸시 이력 남기기
                logService.addHistory(appName, AppLogAction.PUSH, AppLogStatus.SUCCESS, null);

                Map pipeLineJson = appService.getPipeLineJson(appName);
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
                        appService.excutePipelineTrigger(appName, commitRef, null);
                    }
                }
            }

            response.setStatus(200);
        } catch (Exception ex) {
            ex.printStackTrace();
            response.setStatus(200);
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
        appService.updateAppConfigChanged(appName, stage, false);
    }
}
