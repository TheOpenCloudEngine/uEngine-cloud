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

    @RequestMapping(value = "/{appName}/tags", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Map getAppResitryTags(HttpServletRequest request,
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

    @RequestMapping(value = "/{appName}/status", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Map getAppCreateStatus(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @PathVariable("appName") String appName
    ) throws Exception {
        return appService.getAppCreateStatus(appName);
    }

    @RequestMapping(value = "/{appName}/pipeline", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public Map excutePipelineTrigger(HttpServletRequest request,
                                     HttpServletResponse response,
                                     @PathVariable("appName") String appName,
                                     @RequestParam(value = "ref", defaultValue = "master") String ref,
                                     @RequestParam(value = "stage", required = false) String stage
    ) throws Exception {
        return appService.excutePipelineTrigger(appName, ref, stage);
    }

    @RequestMapping(value = "/{appName}/pipeline/info", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Map getAppPipeLineJson(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @PathVariable("appName") String appName
    ) throws Exception {
        return appService.getPipeLineJson(appName);
    }

    @RequestMapping(value = "/{appName}/pipeline/info", method = RequestMethod.PUT, produces = "application/json;charset=UTF-8")
    public Map updateAppPipeLineJson(HttpServletRequest request,
                                     HttpServletResponse response,
                                     @PathVariable("appName") String appName,
                                     @RequestBody Map pipelineJson
    ) throws Exception {
        return appService.updatePipeLineJson(appName, pipelineJson);
    }

    @RequestMapping(value = "/{appName}/deploy", method = RequestMethod.DELETE, produces = "application/json;charset=UTF-8")
    public void removeDeployedApp(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @PathVariable("appName") String appName,
                                  @RequestParam(value = "stage") String stage
    ) throws Exception {
        appService.removeDeployedApp(appName, stage);
        response.setStatus(200);
    }

    @RequestMapping(value = "/{appName}/rollback", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public void rollbackDeployedApp(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @PathVariable("appName") String appName
    ) throws Exception {
        appService.rollbackDeployedApp(appName);
        response.setStatus(200);
    }

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

    @RequestMapping(value = "/{appName}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Map getApp(HttpServletRequest request,
                      HttpServletResponse response,
                      @PathVariable("appName") String appName
    ) throws Exception {

        return appService.getAppIncludDeployJson(appName);
    }

    @RequestMapping(value = "/{appName}", method = RequestMethod.PUT, produces = "application/json;charset=UTF-8")
    public Map updateApp(HttpServletRequest request,
                         HttpServletResponse response,
                         @PathVariable("appName") String appName,
                         @RequestBody Map appMap
    ) throws Exception {

        return appService.updateAppIncludDeployJson(appName, appMap);
    }

    @RequestMapping(value = "/{appName}", method = RequestMethod.DELETE, produces = "application/json;charset=UTF-8")
    public void deleteApp(HttpServletRequest request,
                          HttpServletResponse response,
                          @PathVariable("appName") String appName,
                          @RequestParam(value = "removeRepository", defaultValue = "true") boolean removeRepository
    ) throws Exception {

        appService.deleteApp(appName, removeRepository);
        response.setStatus(200);
    }

    // deployment/<appName> 삭제. 이때 모든 파일들을 다 지워야 한다. => 삭제 api 에서 하기.
    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public String createApp(HttpServletRequest request,
                            HttpServletResponse response,
                            @RequestBody AppCreate appCreate) throws Exception {

        return JsonUtils.marshal(appService.createApp(appCreate));
    }

    //getAppConfigYml
    //updateAppConfigYml
    //getApplicationYml
    @RequestMapping(value = "/{appName}/vcap", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
    public String getVcapServices(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @PathVariable("appName") String appName
    ) throws Exception {
        return appService.getApplicationYml();
    }

    @RequestMapping(value = "/{appName}/config", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
    public String getAppConfigYml(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @PathVariable("appName") String appName
    ) throws Exception {
        return appService.getAppConfigYml(appName);
    }

    @RequestMapping(value = "/{appName}/config", method = RequestMethod.PUT, produces = "text/plain;charset=UTF-8")
    public String updateAppConfigYml(HttpServletRequest request,
                                     HttpServletResponse response,
                                     @PathVariable("appName") String appName,
                                     @RequestBody String content) throws Exception {
        return appService.updateAppConfigYml(appName, content);
    }
}

