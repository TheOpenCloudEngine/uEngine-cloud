package org.uengine.cloud.app.snapshot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import org.uengine.cloud.app.AppWebService;
import org.uengine.cloud.app.config.AppConfigYmlResource;
import org.uengine.cloud.app.log.AppLogAction;
import org.uengine.cloud.app.log.AppLogService;
import org.uengine.cloud.app.log.AppLogStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by uengine on 2017. 11. 14..
 */
@RestController
@RequestMapping("/app")
public class AppSnapshotController {

    @Autowired
    private AppWebService appWebService;

    @Autowired
    private Environment environment;

    @Autowired
    private AppLogService logService;

    @Autowired
    private AppSnapshotService snapshotService;

    @Autowired
    private AppSnapshotKafkaService snapshotKafkaService;

    /**
     * 앱의 스냅샷을 생성한다.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/{appName}/snapshot", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public void createAppSnapshot(HttpServletRequest request,
                                  HttpServletResponse response,
                                  @PathVariable("appName") String appName,
                                  @RequestParam(required = false, value = "name") String name) throws Exception {

        try {
            snapshotKafkaService.createSnapshotSend(appName, name, null);
            response.setStatus(200);

            logService.addHistory(appName, AppLogAction.CREATE_APP_SNAPSHOT, AppLogStatus.SUCCESS, new HashMap());
        } catch (Exception ex) {
            logService.addHistory(appName, AppLogAction.CREATE_APP_REQUEST, AppLogStatus.FAILED, new HashMap());
            throw ex;
        }
    }

    /**
     * 앱의 주어진 스테이지에 스냅샷을 복원한다.
     *
     * @param request
     * @param response
     * @param appName
     * @param snapshotId
     * @param stages
     * @param overrideResource
     * @throws Exception
     */
    @RequestMapping(value = "/{appName}/snapshot/{snapshotId}/deploy", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public void restoreSnapshot(HttpServletRequest request,
                                HttpServletResponse response,
                                @PathVariable("appName") String appName,
                                @PathVariable("snapshotId") Long snapshotId,
                                @RequestParam(value = "stages") String stages,
                                @RequestParam(defaultValue = "true", value = "redeploy") boolean redeploy,
                                @RequestBody(required = false) AppConfigYmlResource overrideResource
    ) throws Exception {
        Map log = new HashMap();
        log.put("snapshotId", snapshotId);
        log.put("stages", stages);
        try {
            String[] split = stages.split(",");
            snapshotKafkaService.restoreSnapshotSend(snapshotId, Arrays.asList(split), overrideResource, redeploy);
            response.setStatus(200);

            logService.addHistory(appName, AppLogAction.RESTORE_APP_SNAPSHOT, AppLogStatus.SUCCESS, log);
        } catch (Exception ex) {
            logService.addHistory(appName, AppLogAction.RESTORE_APP_SNAPSHOT, AppLogStatus.FAILED, log);
            throw ex;
        }
    }
}

