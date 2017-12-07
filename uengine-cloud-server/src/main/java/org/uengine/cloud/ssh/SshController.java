package org.uengine.cloud.ssh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.*;
import org.uengine.cloud.app.AppService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by uengine on 2017. 11. 14..
 */
@RestController
@RequestMapping("/ssh")
public class SshController {

    @Autowired
    AppService appService;

    @Autowired
    Environment environment;

    @Autowired
    SshService sshService;

    @RequestMapping(value = "/{taskId:.+}", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Map getSshMarathonApp(HttpServletRequest request,
                                 HttpServletResponse response,
                                 @PathVariable("taskId") String taskId
    ) throws Exception {
        return sshService.getSshMarathonApp(taskId);
    }

    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public Map createSshMarathonApp(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @RequestBody Map params
    ) throws Exception {
        return sshService.createSshMarathonApp(params.get("taskId").toString(), params.get("shell").toString());
    }

    @RequestMapping(value = "/{taskId:.+}/heartbeat", method = RequestMethod.POST, produces = "application/json;charset=UTF-8")
    public void heartbeatSshContainer(HttpServletRequest request,
                                      HttpServletResponse response,
                                      @PathVariable("taskId") String taskId
    ) throws Exception {
        sshService.heartbeatSshContainer(taskId);
        response.setStatus(200);
    }

    @RequestMapping(value = "/{taskId:.+}", method = RequestMethod.DELETE, produces = "application/json;charset=UTF-8")
    public void deleteSshContainer(HttpServletRequest request,
                                   HttpServletResponse response,
                                   @PathVariable("taskId") String taskId
    ) throws Exception {
        sshService.deleteSshContainer(taskId);
        response.setStatus(200);
    }
}

