package org.uengine.cloud.app.deployment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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
public class AppDeploymentController {

    @Autowired
    private AppDeploymentKafkaService deploymentKafkaService;

    @Autowired
    private AppDeploymentService deploymentService;

    @Autowired
    private Environment environment;

    @Autowired
    private AppLogService logService;

    /**
     * 앱의 주어진 스테이지(dev,stg,prod) 의 인스턴스들을 삭제한다.
     *
     * @param request
     * @param response
     * @param appName  앱 이름
     * @param stage    스테이지
     * @throws Exception
     */
    @RequestMapping(value = "/{appName}/deploy", method = RequestMethod.DELETE, produces = "application/json;charset=UTF-8")
    public void removeDeployedApp(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @PathVariable("appName") String appName,
                                  @RequestParam(value = "stage") String stage
    ) throws Exception {
        Map log = new HashMap();
        log.put("stage", stage);
        try {
            deploymentService.removeDeployedApp(appName, stage);
            response.setStatus(200);

            logService.addHistory(appName, AppLogAction.REMOVE_DEPLOYED_APP, AppLogStatus.SUCCESS, log);
        } catch (Exception ex) {
            logService.addHistory(appName, AppLogAction.REMOVE_DEPLOYED_APP, AppLogStatus.FAILED, log);
            throw ex;
        }
    }

    /**
     * Rollback App which is on deployment.
     *
     * @param request
     * @param response
     * @param appName  앱 이름
     * @throws Exception
     */
    @RequestMapping(value = "/{appName}/rollback", method = RequestMethod.DELETE, produces = "application/json;charset=UTF-8")
    public void rollbackApp(HttpServletRequest request,
                            HttpServletResponse response,
                            @PathVariable("appName") String appName,
                            @RequestParam(value = "stage") String stage
    ) throws Exception {
        try {
            deploymentKafkaService.rollbackAppSend(appName, stage);
            response.setStatus(200);

            logService.addHistory(appName, AppLogAction.ROLLBACK_APP, AppLogStatus.SUCCESS, null);
        } catch (Exception ex) {
            logService.addHistory(appName, AppLogAction.ROLLBACK_APP, AppLogStatus.FAILED, null);
            throw ex;
        }
    }

    /**
     * Finish app deployment which is on manual canary deployment.
     *
     * @param request
     * @param response
     * @param appName  앱 이름
     * @throws Exception
     */
    @RequestMapping(value = "/{appName}/finishManualCanaryDeployment", method = RequestMethod.DELETE, produces = "application/json;charset=UTF-8")
    public void finishManualCanaryDeployment(HttpServletRequest request,
                                             HttpServletResponse response,
                                             @PathVariable("appName") String appName,
                                             @RequestParam(value = "stage") String stage
    ) throws Exception {
        try {
            deploymentService.finishManualCanaryDeployment(appName, stage);
            response.setStatus(200);

            logService.addHistory(appName, AppLogAction.FINISH_MANUAL_CANARY_DEPLOYMENT, AppLogStatus.SUCCESS, null);
        } catch (Exception ex) {
            logService.addHistory(appName, AppLogAction.FINISH_MANUAL_CANARY_DEPLOYMENT, AppLogStatus.FAILED, null);
            throw ex;
        }
    }

    /**
     * Convert manual canary deployment which is on auto canary deployment.
     *
     * @param request
     * @param response
     * @param appName  앱 이름
     * @throws Exception
     */
    @RequestMapping(value = "/{appName}/convertManualCanaryDeployment", method = RequestMethod.PUT, produces = "application/json;charset=UTF-8")
    public void convertManualCanaryDeployment(HttpServletRequest request,
                                              HttpServletResponse response,
                                              @PathVariable("appName") String appName,
                                              @RequestParam(value = "stage") String stage
    ) throws Exception {
        try {
            deploymentService.convertManualCanaryDeployment(appName, stage);
            response.setStatus(200);
        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * 앱의 주어진 스테이지에 인스턴스를 생성한다.
     *
     * @param request
     * @param response
     * @param appName  앱 이름
     * @param stage    스테이지
     * @param commit   커밋 아이디
     * @throws Exception
     */
    @RequestMapping(value = "/{appName}/deploy", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public void deployApp(HttpServletRequest request,
                               HttpServletResponse response,
                               @PathVariable("appName") String appName,
                               @RequestParam(value = "stage") String stage,
                               @RequestParam(value = "commit", required = false) String commit,
                               @RequestBody(required = false) Map params
    ) throws Exception {
        Map log = new HashMap();
        log.put("commit", commit);
        log.put("stage", stage);
        try {
            String name = null;
            String description = null;
            if (params != null) {
                name = params.containsKey("name") ? params.get("name").toString() : null;
                description = params.containsKey("description") ? params.get("description").toString() : null;
            }
            deploymentKafkaService.deployAppSend(appName, stage, commit, null, name, description);
            response.setStatus(200);

            logService.addHistory(appName, AppLogAction.RUN_DEPLOYED_APP_REQUEST, AppLogStatus.SUCCESS, log);
        } catch (Exception ex) {
            logService.addHistory(appName, AppLogAction.RUN_DEPLOYED_APP_REQUEST, AppLogStatus.FAILED, log);
            throw ex;
        }
    }
}

