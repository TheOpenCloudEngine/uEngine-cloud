package org.uengine.eureka.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.NumberFormat;
import java.util.Map;
import java.util.Set;

/**
 * Created by uengine on 2017. 10. 5..
 */
@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableEurekaClient
@RestController
@EnableCircuitBreaker
@EnableHystrixDashboard
public class Application {

    @Autowired
    private Environment environment;

    @Autowired
    private FooService fooService;

    @Autowired
    private HomeService homeService;

    @RequestMapping("/foo/{id}")
    public Object foo(@PathVariable String id) throws Exception {
        return fooService.getFoo(id);
    }

    @RequestMapping("/")
    public String home() throws Exception {
        return homeService.getHome();
    }

    private static Log logger = LogFactory.getLog(Application.class);

    public static void main(String[] args) {

        Map<String, String> stringStringMap = System.getenv();
        Set<Map.Entry<String, String>> entries = stringStringMap.entrySet();
        for (Map.Entry<String, String> entry : entries) {
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }

        new SpringApplicationBuilder(Application.class).web(true).run(args);

        Runtime runtime = Runtime.getRuntime();
        final NumberFormat format = NumberFormat.getInstance();
        final long maxMemory = runtime.maxMemory();
        final long allocatedMemory = runtime.totalMemory();
        final long freeMemory = runtime.freeMemory();
        final long mb = 1024 * 1024;
        final String mega = "MB";
        logger.info("========================== Memory Info ==========================");
        logger.info("Free memory: " + format.format(freeMemory / mb) + mega);
        logger.info("Allocated memory: " + format.format(allocatedMemory / mb) + mega);
        logger.info("Max memory: " + format.format(maxMemory / mb) + mega);
        logger.info("=================================================================\n");
    }

}