package org.uengine.cloud.app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

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
                String appName = ((Map) payloads.get("project")).get("name").toString();
                String pipelineId = ((Map) payloads.get("object_attributes")).get("id").toString();

                Map app = appService.getAppByName(appName);
                int projectId = (int) ((Map) app.get("gitlab")).get("projectId");
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
                        }
                    }
                    //매뉴얼 단계이면서 auto 빌드에 속한 경우
                    else if (autoDeploys.indexOf(build.get("name").toString()) != -1 && build.get("status").equals("manual")) {
                        int jobId = (int) build.get("id");
                        gitlabExtentApi.playJob(projectId, jobId);
                    }
                }
            } else if (payloads.get("object_kind").toString().equals("push")) {
                String appName = ((Map) payloads.get("project")).get("name").toString();
                Map app = appService.getAppByName(appName);
                int projectId = (int) ((Map) app.get("gitlab")).get("projectId");
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
            response.setStatus(200);
        }
    }
}
