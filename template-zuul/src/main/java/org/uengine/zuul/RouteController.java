package org.uengine.zuul;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.*;
import org.opencloudengine.garuda.util.HttpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * Created by uengine on 2017. 10. 9..
 */
@RestController
public class RouteController {

    @Autowired
    private EurekaClient discoveryClient;

    @Autowired
    private Environment environment;

    @Autowired
    private RouteService routeService;

    //여기서 iam 룰을 적용하기 위해서... iam 인증 룰.

    @RequestMapping(value = "/refreshRoute", method = GET)
    public void refresh() {
        com.netflix.discovery.shared.Application application = discoveryClient.getApplication(environment.getProperty("spring.application.name"));
        List<InstanceInfo> instances = application.getInstances();
        if (!instances.isEmpty()) {
            for (InstanceInfo instance : instances) {
                String url = "http://" + instance.getHostName() + ":" + instance.getPort() + "/broadcastRefreshRoute";
                try {
                    new HttpUtils().makeRequest("GET", url, null, new HashMap<>());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @RequestMapping(value = "/broadcastRefreshRoute", method = GET)
    public void broadcastRefresh() {
        routeService.refreshApps();
    }
}
