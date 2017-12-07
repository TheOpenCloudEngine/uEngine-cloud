package org.uengine.eureka.server;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

import java.text.NumberFormat;

/**
 * Created by uengine on 2017. 10. 5..
 */
@SpringBootApplication
@EnableEurekaServer
public class Application {

    private static Log logger = LogFactory.getLog(Application.class);

    public static void main(String[] args) {
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
