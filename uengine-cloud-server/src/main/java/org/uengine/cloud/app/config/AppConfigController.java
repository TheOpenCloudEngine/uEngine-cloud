package org.uengine.cloud.app.config;

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
public class AppConfigController {

    @Autowired
    private AppConfigService appConfigService;

    @Autowired
    private Environment environment;

    @Autowired
    private AppLogService logService;

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
        return appConfigService.getApplicationYml();
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

        return appConfigService.getAppConfigYml(appName, stage);
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
            String yml = appConfigService.updateAppConfigYml(appName, content, stage);

            logService.addHistory(appName, AppLogAction.UPDATE_APP_CONFIGYML, AppLogStatus.SUCCESS, log);

            return yml;
        } catch (Exception ex) {
            logService.addHistory(appName, AppLogAction.UPDATE_APP_CONFIGYML, AppLogStatus.FAILED, log);
            throw ex;
        }
    }
}

