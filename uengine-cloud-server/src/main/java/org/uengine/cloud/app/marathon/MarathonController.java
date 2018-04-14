package org.uengine.cloud.app.marathon;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Created by uengine on 2017. 11. 14..
 */
@RestController
@RequestMapping("/marathon")
public class MarathonController {

    @Autowired
    private MarathonService marathonService;

    @Autowired
    private MarathonCacheService marathonCacheService;

    @Autowired
    private DcosApi dcosApi;


    /**
     * 앱의 마라톤 앱들을 가져온다.
     *
     * @param request
     * @param response
     * @param appName
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/app/{appName}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Map getMarathonAppsByAppName(HttpServletRequest request,
                                        HttpServletResponse response,
                                        @PathVariable("appName") String appName
    ) throws Exception {

        return marathonService.getMarathonAppsByAppName(appName);
    }

    /**
     * 아이디에 해당하는 마라톤 앱을 가져온다.
     *
     * @param request
     * @param response
     * @param marathonAppId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/service/{marathonAppId}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Map getMarathonAppById(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @PathVariable("marathonAppId") String marathonAppId
    ) throws Exception {
        if (!marathonAppId.startsWith("/")) {
            marathonAppId = "/" + marathonAppId;
        }
        return marathonCacheService.getMarathonAppByIdCache(marathonAppId);
    }

    /**
     * 모든 서비스 앱을 가져온다.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/service", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public List<Map> getServiceApps(HttpServletRequest request,
                                    HttpServletResponse response
    ) throws Exception {
        return marathonCacheService.getServiceAppsCache();
    }

    /**
     * 메소스 타스크를 가져온다.
     *
     * @param request
     * @param response
     * @param taskId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/task/{taskId}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Map getMesosTaskById(HttpServletRequest request,
                                HttpServletResponse response,
                                @PathVariable("taskId") String taskId
    ) throws Exception {
        return dcosApi.getMesosTaskById(taskId);
    }

    /**
     * Dcos last 정보를 가져온다.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/last", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Map getDcosLast(HttpServletRequest request,
                           HttpServletResponse response
    ) throws Exception {
        return marathonCacheService.getDcosLastCache();
    }
}

