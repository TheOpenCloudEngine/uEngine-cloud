package org.uengine.cloud.app;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.opencloudengine.garuda.util.HttpUtils;
import org.opencloudengine.garuda.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by uengine on 2017. 11. 14..
 */
@RestController
@RequestMapping("/app")
public class AppController {

    @Autowired
    AppService appService;

    @Autowired
    Environment environment;

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
        HttpResponse res = new HttpUtils().makeRequest("GET",
                "http://" + environment.getProperty("registry.host") + "/v2/" + appName + "/tags/list",
                null,
                new HashMap<>()
        );
        HttpEntity entity = res.getEntity();
        String json = EntityUtils.toString(entity);
        return JsonUtils.unmarshal(json);
    }

    /**
     * 앱 생성 상태를 가져온다.
     *
     * @param request
     * @param response
     * @param appName  앱 이름
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{appName}/status", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Map getAppCreateStatus(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @PathVariable("appName") String appName
    ) throws Exception {
        return appService.getAppCreateStatus(appName);
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
        return appService.excutePipelineTrigger(appName, ref, stage);
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
        return appService.updatePipeLineJson(appName, pipelineJson);
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
        appService.removeDeployedApp(appName, stage);
        response.setStatus(200);
    }

    /**
     * 앱을 롤백한다.
     *
     * @param request
     * @param response
     * @param appName  앱 이름
     * @throws Exception
     */
    @RequestMapping(value = "/{appName}/rollback", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public void rollbackDeployedApp(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @PathVariable("appName") String appName
    ) throws Exception {
        appService.rollbackDeployedApp(appName);
        response.setStatus(200);
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
                               @RequestParam(value = "commit", required = false) String commit
    ) throws Exception {
        appService.runDeployedApp(appName, stage, commit);
        response.setStatus(200);
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

        return appService.getAppIncludDeployJson(appName);
    }

    /**
     * 앱 정보를 업데이트한다.
     *
     * @param request
     * @param response
     * @param appName  앱 이름
     * @param appMap   앱 정보 내용
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{appName}", method = RequestMethod.PUT, produces = "application/json;charset=UTF-8")
    public Map updateApp(HttpServletRequest request,
                         HttpServletResponse response,
                         @PathVariable("appName") String appName,
                         @RequestBody Map appMap
    ) throws Exception {

        return appService.updateAppIncludDeployJson(appName, appMap);
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

        appService.deleteApp(appName, removeRepository);
        response.setStatus(200);
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
    // deployment/<appName> 삭제. 이때 모든 파일들을 다 지워야 한다. => 삭제 api 에서 하기.
    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String createApp(HttpServletRequest request,
                            HttpServletResponse response,
                            @RequestBody AppCreate appCreate) throws Exception {

        return JsonUtils.marshal(appService.createApp(appCreate));
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
                                  @RequestParam(value = "stage") String stage
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
                                     @RequestParam(value = "stage") String stage,
                                     @RequestBody String content) throws Exception {
        return appService.updateAppConfigYml(appName, content, stage);
    }
}

