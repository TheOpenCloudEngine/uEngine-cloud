package org.uengine.eureka.client;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Created by uengine on 2017. 10. 6..
 */
@Component
public class HomeService {

    @Autowired
    private EurekaClient discoveryClient;

    @Autowired
    private Environment environment;

    @HystrixCommand
    public String getHome() {
        InstanceInfo info = discoveryClient.getApplicationInfoManager().getInfo();
        String deployment = "";
        if (info.getMetadata().get("deployment") != null) {
            deployment = info.getMetadata().get("deployment").toString();
        }
        return "I am " + deployment + ",   instanceId:" + info.getInstanceId() + "profile:" + environment.getProperty("spring.profiles.active");
    }
}
