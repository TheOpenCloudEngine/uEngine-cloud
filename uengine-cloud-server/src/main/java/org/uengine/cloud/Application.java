package org.uengine.cloud;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.uengine.iam.util.HttpUtils;
import org.uengine.iam.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.uengine.cloud.app.DcosApi;
import org.uengine.cloud.scheduler.CronTable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by uengine on 2017. 10. 5..
 */
@SpringBootApplication
@RestController
@Configuration
@ComponentScan
@EnableEurekaClient
@EnableAutoConfiguration
@EnableScheduling
public class Application {

    @Autowired
    private CronTable cronTable;

    @Autowired
    private DcosApi dcosApi;

    @RequestMapping(value = "/fetchData", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Map fetchData(HttpServletRequest request,
                         HttpServletResponse response
    ) throws Exception {
        return cronTable.getDcosData();
    }

    @RequestMapping(value = "/refreshRoute", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public void refreshRoute(HttpServletRequest request,
                             HttpServletResponse response
    ) throws Exception {
        dcosApi.refreshRoute();
        response.setStatus(200);
    }

    @RequestMapping("/health")
    public String health() {
        return "";
    }

    public static void main(String[] args) {
        System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");
        new SpringApplicationBuilder(Application.class).web(true).run(args);
    }

}
