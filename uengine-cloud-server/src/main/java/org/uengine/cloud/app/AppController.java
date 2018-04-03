package org.uengine.cloud.app;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.uengine.cloud.log.AppLogAction;
import org.uengine.cloud.log.AppLogService;
import org.uengine.cloud.log.AppLogStatus;
import org.uengine.cloud.tenant.TenantContext;
import org.uengine.iam.client.model.OauthUser;
import org.uengine.iam.util.HttpUtils;
import org.uengine.iam.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import org.uengine.iam.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by uengine on 2017. 11. 14..
 */
@RestController
@RequestMapping("/app")
public class AppController {

    @Autowired
    private AppService appService;

    @Autowired
    private Environment environment;

    @Autowired
    private AppLogService logService;

    @Autowired
    private AppAccessLevelRepository appAccessLevelRepository;

    /**
     * 앱의 도커 이미지 목록을 가져온다.
     *
     * @param request
     * @param response
     * @param appName  앱 이름
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{appName}/tags", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Map getAppRegistryTags(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @PathVariable("appName") String appName
    ) throws Exception {
        return appService.getAppRegistryTags(appName);
    }

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
            Map map = appService.excutePipelineTrigger(appName, ref, stage);
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
        return appService.getPipeLineJson(appName);
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
            Map map = appService.updatePipeLineJson(appName, pipelineJson);
            logService.addHistory(appName, AppLogAction.UPDATE_APP_PIPELINE_JSON, AppLogStatus.SUCCESS, null);
            return map;
        } catch (Exception ex) {
            logService.addHistory(appName, AppLogAction.UPDATE_APP_PIPELINE_JSON, AppLogStatus.FAILED, null);
            throw ex;
        }
    }

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
            appService.removeDeployedApp(appName, stage);
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
            appService.rollbackApp(appName, stage);
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
            appService.finishManualCanaryDeployment(appName, stage);
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
            appService.convertManualCanaryDeployment(appName, stage);
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
    public void runDeployedApp(HttpServletRequest request,
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
            appService.runDeployedApp(appName, stage, commit, null, name, description);
            response.setStatus(200);

            logService.addHistory(appName, AppLogAction.RUN_DEPLOYED_APP_REQUEST, AppLogStatus.SUCCESS, log);
        } catch (Exception ex) {
            logService.addHistory(appName, AppLogAction.RUN_DEPLOYED_APP_REQUEST, AppLogStatus.FAILED, log);
            throw ex;
        }
    }

    /**
     * 앱 정보를 가져온다.
     *
     * @param request
     * @param response
     * @param appName  앱 이름
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{appName}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Map getApp(HttpServletRequest request,
                      HttpServletResponse response,
                      @PathVariable("appName") String appName
    ) throws Exception {
        return JsonUtils.convertClassToMap(appService.getAppIncludeDeployJson(appName));
    }

    /**
     * 앱 정보를 업데이트한다.
     *
     * @param request
     * @param response
     * @param appName   앱 이름
     * @param appEntity 앱 정보 내용
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{appName}", method = RequestMethod.PUT, produces = "application/json;charset=UTF-8")
    public Map updateApp(HttpServletRequest request,
                         HttpServletResponse response,
                         @PathVariable("appName") String appName,
                         @RequestBody Map appEntity,
                         @RequestParam(value = "excludeDeploy", defaultValue = "false") boolean excludeDeploy
    ) throws Exception {
        try {
            AppEntity entity = JsonUtils.convertValue(appEntity, AppEntity.class);
            if (excludeDeploy) {
                entity = appService.updateAppExcludeDeployJson(appName, entity);
            } else {
                entity = appService.updateAppIncludeDeployJson(appName, entity);
            }
            logService.addHistory(appName, AppLogAction.UPDATE_APP, AppLogStatus.SUCCESS, null);
            return JsonUtils.convertClassToMap(entity);
        } catch (Exception ex) {
            logService.addHistory(appName, AppLogAction.UPDATE_APP, AppLogStatus.FAILED, null);
            throw ex;
        }
    }

    /**
     * 앱을 삭제한다.
     *
     * @param request
     * @param response
     * @param appName          앱 이름
     * @param removeRepository 깃랩 프로젝트 삭제 여부
     * @throws Exception
     */
    @RequestMapping(value = "/{appName}", method = RequestMethod.DELETE, produces = "application/json;charset=UTF-8")
    public void deleteApp(HttpServletRequest request,
                          HttpServletResponse response,
                          @PathVariable("appName") String appName,
                          @RequestParam(value = "removeRepository", defaultValue = "true") boolean removeRepository
    ) throws Exception {
        Map log = new HashMap();
        log.put("removeRepository", removeRepository);

        try {
            appService.deleteApp(appName, removeRepository);
            response.setStatus(200);

            logService.addHistory(appName, AppLogAction.DELETE_APP, AppLogStatus.SUCCESS, log);
        } catch (Exception ex) {
            logService.addHistory(appName, AppLogAction.DELETE_APP, AppLogStatus.FAILED, log);
            throw ex;
        }
    }

    /**
     * 앱을 생성한다.
     *
     * @param request
     * @param response
     * @param appCreate 앱 생성 내용
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public Map createApp(HttpServletRequest request,
                         HttpServletResponse response,
                         @RequestBody AppCreate appCreate) throws Exception {

        Map<String, Object> log = JsonUtils.convertClassToMap(appCreate);
        try {
            AppEntity appEntity = appService.createApp(appCreate);
            logService.addHistory(appCreate.getAppName(), AppLogAction.CREATE_APP_REQUEST, AppLogStatus.SUCCESS, log);
            return JsonUtils.convertClassToMap(appEntity);
        } catch (Exception ex) {
            logService.addHistory(appCreate.getAppName(), AppLogAction.CREATE_APP_REQUEST, AppLogStatus.FAILED, log);
            throw ex;
        }
    }

    /**
     * 앱의 vcap(연결정보) 를 가져온다.
     *
     * @param request
     * @param response
     * @param appName  앱 이름
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{appName}/vcap", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
    public String getVcapServices(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @PathVariable("appName") String appName
    ) throws Exception {
        return appService.getApplicationYml();
    }

    /**
     * 앱의 주어진 스테이지의 클라우드 콘피그 yml 을 가져온다.
     *
     * @param request
     * @param response
     * @param appName  앱 이름
     * @param stage    스테이지
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{appName}/config", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
    public String getAppConfigYml(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @PathVariable("appName") String appName,
                                  @RequestParam(required = false, value = "stage") String stage
    ) throws Exception {

        return appService.getAppConfigYml(appName, stage);
    }

    /**
     * 앱의 주어진 스테이지에 클라우드 콘피크 yml 을 업데이트한다.
     *
     * @param request
     * @param response
     * @param appName  앱 이름
     * @param stage    스테이지
     * @param content  yml 내용
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{appName}/config", method = RequestMethod.PUT, produces = "text/plain;charset=UTF-8")
    public String updateAppConfigYml(HttpServletRequest request,
                                     HttpServletResponse response,
                                     @PathVariable("appName") String appName,
                                     @RequestParam(required = false, value = "stage") String stage,
                                     @RequestBody String content) throws Exception {
        Map log = new HashMap();
        log.put("stage", stage);
        try {
            String yml = appService.updateAppConfigYml(appName, content, stage);

            logService.addHistory(appName, AppLogAction.UPDATE_APP_CONFIGYML, AppLogStatus.SUCCESS, log);

            return yml;
        } catch (Exception ex) {
            logService.addHistory(appName, AppLogAction.UPDATE_APP_CONFIGYML, AppLogStatus.FAILED, log);
            throw ex;
        }
    }
}

