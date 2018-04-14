package org.uengine.cloud.app.deployjson;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
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
public class AppDeployJsonController {

    @Autowired
    private AppDeployJsonService deployJsonService;

    @Autowired
    private AppLogService logService;

    /**
     * 앱의 주어진 스테이지의 deployJson 을 갱신한다.
     *
     * @param request
     * @param response
     * @param appName
     * @param stage
     * @param deployJson
     * @throws Exception
     */
    @RequestMapping(value = "/{appName}/deployJson", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public Map updateDeployJson(HttpServletRequest request,
                                HttpServletResponse response,
                                @PathVariable("appName") String appName,
                                @RequestParam(value = "stage") String stage,
                                @RequestBody Map deployJson
    ) throws Exception {
        Map log = new HashMap();
        log.put("stage", stage);
        try {
            deployJson = deployJsonService.updateDeployJson(appName, stage, deployJson);
            logService.addHistory(appName, AppLogAction.UPDATE_APP, AppLogStatus.SUCCESS, log);
            return deployJson;
        } catch (Exception ex) {
            logService.addHistory(appName, AppLogAction.UPDATE_APP, AppLogStatus.FAILED, log);
            throw ex;
        }
    }

    /**
     * 앱의 주어진 스테이지의 deployJson 을 가져온다.
     * 스테이지가 업다면 모두 반환.
     *
     * @param request
     * @param response
     * @param appName
     * @param stage
     * @throws Exception
     */
    @RequestMapping(value = "/{appName}/deployJson", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Map getDeployJson(HttpServletRequest request,
                             HttpServletResponse response,
                             @PathVariable("appName") String appName,
                             @RequestParam(required = false, value = "stage") String stage
    ) throws Exception {
        if (StringUtils.isEmpty(stage)) {
            return deployJsonService.getAllDeployJson(appName);
        } else {
            return deployJsonService.getDeployJson(appName, stage);
        }
    }
}

