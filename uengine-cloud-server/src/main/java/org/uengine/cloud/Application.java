package org.uengine.cloud;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.uengine.cloud.app.AppEntity;
import org.uengine.cloud.app.AppWebCacheService;
import org.uengine.cloud.app.AppWebService;
import org.uengine.cloud.migration.MigrationService;
import org.uengine.iam.util.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
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
@EnableRetry
@EnableAsync
@EnableCaching
public class Application {

    @Autowired
    private MigrationService migrationService;

    @Autowired
    private AppWebCacheService appWebCacheService;

    //findAll
    @RequestMapping(value = "/fetchLBData", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public Map fetchLBData(HttpServletRequest request,
                           HttpServletResponse response
    ) throws Exception {

        List<AppEntity> appEntities = appWebCacheService.findAllAppsCache();

        Map apps = new HashMap();
        for (int i = 0; i < appEntities.size(); i++) {
            Map<String, Object> map = JsonUtils.convertClassToMap(appEntities.get(i));
            map.put("memberIds", null);
            map.put("accessLevel", 0);
            apps.put(appEntities.get(i).getName(), map);
        }
        return apps;
    }

    @RequestMapping(value = "/migration", method = RequestMethod.GET, produces = "application/json;charset=UTF-8")
    public void migration(HttpServletRequest request,
                          HttpServletResponse response
    ) throws Exception {
        migrationService.migration();
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
