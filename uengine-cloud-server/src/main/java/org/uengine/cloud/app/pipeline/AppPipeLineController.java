package org.uengine.cloud.app.pipeline;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.uengine.cloud.app.log.AppLogAction;
import org.uengine.cloud.app.log.AppLogService;
import org.uengine.cloud.app.log.AppLogStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by uengine on 2017. 11. 14..
 */
@RestController
@RequestMapping("/app")
public class AppPipeLineController {

    @Autowired
    private AppPipeLineService pipeLineService;

    @Autowired
    private AppLogService logService;

    /**
     * 앱 배포 파이프라인을 실행한다. (깃랩 CI)
     *
     * @param request
     * @param response
     * @param appName  앱 이름
     * @param ref      커밋 레퍼런스
     * @param stage    스테이지
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{appName}/pipeline", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public Map excutePipelineTrigger(HttpServletRequest request,
                                     HttpServletResponse response,
                                     @PathVariable("appName") String appName,
                                     @RequestParam(value = "ref", defaultValue = "master") String ref,
                                     @RequestParam(value = "stage", required = false) String stage
    ) throws Exception {
        Map log = new HashMap();
        log.put("ref", ref);
        log.put("stage", stage);
        try {
            Map map = pipeLineService.excutePipelineTrigger(appName, ref, stage);
            logService.addHistory(appName, AppLogAction.EXCUTE_PIPELINE_TRIGGER, AppLogStatus.SUCCESS, log);
            return map;
        } catch (Exception ex) {
            logService.addHistory(appName, AppLogAction.EXCUTE_PIPELINE_TRIGGER, AppLogStatus.FAILED, log);
            throw ex;
        }
    }

    /**
     * 앱 자동 배포 설정을 가져온다.
     *
     * @param request
     * @param response
     * @param appName  앱 이름
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{appName}/pipeline/info", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Map getAppPipeLineJson(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @PathVariable("appName") String appName
    ) throws Exception {
        return pipeLineService.getPipeLineJson(appName);
    }

    /**
     * 앱 자동 배포 설정을 변경한다.
     *
     * @param request
     * @param response
     * @param appName      앱 이름
     * @param pipelineJson 설정 내용
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{appName}/pipeline/info", method = RequestMethod.PUT, produces = "application/json;charset=UTF-8")
    public Map updateAppPipeLineJson(HttpServletRequest request,
                                     HttpServletResponse response,
                                     @PathVariable("appName") String appName,
                                     @RequestBody Map pipelineJson
    ) throws Exception {
        try {
            Map map = pipeLineService.updatePipeLineJson(appName, pipelineJson);
            logService.addHistory(appName, AppLogAction.UPDATE_APP_PIPELINE_JSON, AppLogStatus.SUCCESS, null);
            return map;
        } catch (Exception ex) {
            logService.addHistory(appName, AppLogAction.UPDATE_APP_PIPELINE_JSON, AppLogStatus.FAILED, null);
            throw ex;
        }
    }
}

