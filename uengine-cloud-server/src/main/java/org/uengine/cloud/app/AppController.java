package org.uengine.cloud.app;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.uengine.cloud.entity.AppLogEntity;
import org.uengine.cloud.repository.AppLogRepository;
import org.uengine.cloud.tenant.TenantContext;
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

    @Autowired
    AppLogRepository appLogRepository;

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
        try {
            Map<String, Object> content = new HashMap<String, Object>();
            content.put("action", "excutePipelineTrigger");
            content.put("appName", appName);
            content.put("ref", ref);
            content.put("stage", stage);

            appHistory(appName, TenantContext.getThreadLocalInstance().getUserId(), TenantContext.getThreadLocalInstance().getUserId(), JsonUtils.marshal(content));
        }
        //이력 저장에 실패해도 결과물은 리턴해야 한다.
        catch (Exception ex) {
            ex.printStackTrace();
        }
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
        try {
            Map<String, Object> content = new HashMap<String, Object>();
            content.put("action", "updateAppPipeLineJson");
            content.put("appName", appName);

            appHistory(appName, TenantContext.getThreadLocalInstance().getUserId(), TenantContext.getThreadLocalInstance().getUserId(), JsonUtils.marshal(content));
        }
        //이력 저장에 실패해도 결과물은 리턴해야 한다.
        catch (Exception ex) {
            ex.printStackTrace();
        }
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
        try {
            Map<String, Object> content = new HashMap<String, Object>();
            content.put("action", "removeDeployedApp");
            content.put("stage", stage);
            content.put("appName", appName);

            appHistory(appName, TenantContext.getThreadLocalInstance().getUserId(), TenantContext.getThreadLocalInstance().getUserId(), JsonUtils.marshal(content));
        }
        //이력 저장에 실패해도 결과물은 리턴해야 한다.
        catch (Exception ex) {
            ex.printStackTrace();
        }

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
        try {
            Map<String, Object> content = new HashMap<String, Object>();
            content.put("action", "rollbackDeployedApp");
            content.put("appName", appName);

            appHistory(appName, TenantContext.getThreadLocalInstance().getUserId(), TenantContext.getThreadLocalInstance().getUserId(), JsonUtils.marshal(content));
        }
        //이력 저장에 실패해도 결과물은 리턴해야 한다.
        catch (Exception ex) {
            ex.printStackTrace();
        }
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
        try {
            Map<String, Object> content = new HashMap<String, Object>();
            content.put("action", "runDeployedApp");
            content.put("stage", stage);
            content.put("commit", commit);

            appHistory(appName, TenantContext.getThreadLocalInstance().getUserId(), TenantContext.getThreadLocalInstance().getUserId(), JsonUtils.marshal(content));
        }
        //이력 저장에 실패해도 결과물은 리턴해야 한다.
        catch (Exception ex) {
            ex.printStackTrace();
        }
        appService.runDeployedApp(appName, stage, commit);

        Map app = appService.getAppByName(appName);
        Map stageMap = (Map)app.get(stage);
        stageMap.remove("config-changed");
        app.put(stage,stageMap);
        appService.updateAppExcludDeployJson(appName, app);

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
                         @RequestBody Map appMap,
                         @RequestParam(value = "excludeDeploy", defaultValue = "false") boolean excludeDeploy
    ) throws Exception {
        try {
            Map<String, Object> content = new HashMap<String, Object>();
            content.put("action", "updateApp");
            //(appName, 사용자, 변경자, 변경일시, JsonUtils.marshal(content))
            appHistory(appName, appMap.get("iam").toString(), TenantContext.getThreadLocalInstance().getUserId(), JsonUtils.marshal(content));
        }
        //이력 저장에 실패해도 결과물은 리턴해야 한다.
        catch (Exception ex) {
            ex.printStackTrace();
        }
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
        try {
            Map<String, Object> content = new HashMap<String, Object>();
            content.put("action", "deleteApp");
            content.put("appName", appName);
            content.put("removeRepository", removeRepository);
            appHistory(appName, TenantContext.getThreadLocalInstance().getUserId(), TenantContext.getThreadLocalInstance().getUserId(), JsonUtils.marshal(content));
        }
        //이력 저장에 실패해도 결과물은 리턴해야 한다.
        catch (Exception ex) {
            ex.printStackTrace();
        }
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

        try {
            Map<String, Object> content = new HashMap<String, Object>();
            content.put("action", "createApp");
            appHistory(appCreate.getAppName(), TenantContext.getThreadLocalInstance().getUserId(), TenantContext.getThreadLocalInstance().getUserId(), JsonUtils.marshal(content));

            //appHistory 는 jpa 레파지토리라는 것을 알겠지요?
        }
        //이력 저장에 실패해도 결과물은 리턴해야 한다.
        catch (Exception ex) {
            ex.printStackTrace();
        }

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
        try {
            Map<String, Object> contentMap = new HashMap<String, Object>();
            contentMap.put("action", "updateAppConfigYml");
            contentMap.put("appName", appName);
            contentMap.put("stage", stage);
            appHistory(appName, TenantContext.getThreadLocalInstance().getUserId(), TenantContext.getThreadLocalInstance().getUserId(), JsonUtils.marshal(contentMap));

            //appHistory 는 jpa 레파지토리라는 것을 알겠지요?
        }
        //이력 저장에 실패해도 결과물은 리턴해야 한다.
        catch (Exception ex) {
            ex.printStackTrace();
        }

        Map app = appService.getAppByName(appName);
        Map stageMap = (Map) app.get(stage);
        stageMap.put("config-changed", true);
        app.put(stage,stageMap);
        appService.updateAppExcludDeployJson(appName, app);

        return appService.updateAppConfigYml(appName, content, stage);
    }

    public void appHistory(String appName, String ownerUserName, String updateUserName, String appInfo) throws Exception {
        //업데이트날짜는 현재날짜의 시간형식으로 넣어준다
//        Date date = new Date();

        AppLogEntity appLogEntity = new AppLogEntity();
        appLogEntity.setAppName(appName);
//        appLogEntity.setUpdateDate(date.getTime());
        appLogEntity.setAppInfo(appInfo);
        appLogEntity.setOwnerName(ownerUserName);
        appLogEntity.setUpdateUserName(updateUserName);
        appLogRepository.save(appLogEntity);

//        String USER_AGENT = "Mozilla/5.0";
//        URL urlObj = new URL("http://localhost:8080/appLogs");
//        HttpURLConnection con = (HttpURLConnection) urlObj.openConnection();
//        con.setRequestMethod("POST");
//        con.setRequestProperty("User-Agent", USER_AGENT);
//        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
//        con.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
//
//        // Send post request
//        con.setDoOutput(true);
//        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
//        wr.writeBytes(jsonObj.toJSONString());
//        wr.flush();
//        wr.close();
//
//        int responseCode = con.getResponseCode();
//        System.out.println("\nSending 'POST' request to URL : " + urlObj);
//        System.out.println("Post parameters : " + jsonObj.toJSONString());
//        System.out.println("Response Code : " + responseCode);
//
//        BufferedReader in = new BufferedReader(
//                new InputStreamReader(con.getInputStream()));
//        String inputLine;
//        StringBuffer response = new StringBuffer();
//
//        while ((inputLine = in.readLine()) != null) {
//            response.append(inputLine);
//        }
//        in.close();
//
//        //print result
//        System.out.println(response.toString());


    }
}

