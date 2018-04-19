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


    //앱별 타스크 => 앱에서 가져옴.
    //노드별 타스크 => 풀링.
    //서비스 앱 불러오는 로직에서 슬레이브 아이디별로 정리 후 레디스에 저장. 이때 cpus,mem,disk 함께 저장.

    //앱이 사라짐은 어찌 처리 => null 로 나옴.
    //앱 변경

    @Autowired
    private MarathonService marathonService;

    @Autowired
    private MarathonCacheService marathonCacheService;

    @Autowired
    private DcosApi dcosApi;


    /**
     * 앱에 소속된 마라톤 앱들을 가져온다.
     *
     * @param request
     * @param response
     * @param appName
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/app", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Map getMarathonAppsByAppName(HttpServletRequest request,
                                        HttpServletResponse response,
                                        @RequestParam("appName") String appName
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
    @RequestMapping(value = "/app/{marathonAppId}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
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
        return marathonService.getServiceApps();
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
    @RequestMapping(value = "/task", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Map getMesosTaskById(HttpServletRequest request,
                                HttpServletResponse response,
                                @RequestParam("taskId") String taskId
    ) throws Exception {
        return dcosApi.getMesosTaskById(taskId);
    }

    /**
     * 노드별 타스크 리스트를 가져온다.
     *
     * @param request
     * @param response
     * @param slaveId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/task/slave/{slaveId}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public List<Map> getTasksBySlaveId(HttpServletRequest request,
                                       HttpServletResponse response,
                                       @PathVariable("slaveId") String slaveId
    ) throws Exception {
        return marathonCacheService.getTasksPerNodeCache(slaveId);
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

