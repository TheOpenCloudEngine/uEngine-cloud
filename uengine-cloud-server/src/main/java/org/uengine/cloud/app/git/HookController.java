package org.uengine.cloud.app.git;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.uengine.cloud.app.AppEntity;
import org.uengine.cloud.app.AppWebCacheService;
import org.uengine.cloud.app.AppWebService;
import org.uengine.cloud.app.config.AppConfigService;
import org.uengine.cloud.app.log.AppLogAction;
import org.uengine.cloud.app.log.AppLogService;
import org.uengine.cloud.app.log.AppLogStatus;
import org.uengine.cloud.app.pipeline.AppPipeLineService;
import org.uengine.iam.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by uengine on 2017. 11. 20..
 */
@RestController
public class HookController {


    @Autowired
    private HookService hookService;

    @RequestMapping(value = "/hook", method = RequestMethod.POST)
    public void receiveWebHook(HttpServletRequest request,
                               HttpServletResponse response,
                               @RequestBody Map payloads) throws Exception {

        try {
            if (payloads.get("object_kind").toString().equals("pipeline")) {
                hookService.receivePipeLineEventHook(payloads);
            } else if (payloads.get("object_kind").toString().equals("push")) {
                hookService.receivePushEventHook(payloads);
            }
        } finally {
            response.setStatus(200);
        }
    }

    @RequestMapping(value = "/systemhook", method = RequestMethod.POST)
    public void receiveSystemWebHook(HttpServletRequest request,
                                     HttpServletResponse response,
                                     @RequestBody Map payloads) throws Exception {

        //user_add_to_team
        //user_remove_from_team
        //user_add_to_group
        //user_remove_from_group
        try {
            String event_name = payloads.get("event_name").toString();
            if (event_name.equals("user_add_to_team") || event_name.equals("user_remove_from_team")) {
                hookService.receiveUserTeamEventHook(payloads);
            } else if (event_name.equals("user_add_to_group") || event_name.equals("user_remove_from_group")) {
                hookService.receiveUserGroupEventHook(payloads);
            } else if (event_name.equals("project_create")) {
                hookService.receiveProjectCreateHook(payloads);
            } else if (event_name.equals("repository_update")) {
                hookService.receiveRepositoryUpdateHook(payloads);
            }
        } finally {
            response.setStatus(200);
        }
    }
}
