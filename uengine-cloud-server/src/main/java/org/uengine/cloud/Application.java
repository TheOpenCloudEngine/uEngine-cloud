package org.uengine.cloud;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by uengine on 2017. 10. 5..
 */
@SpringBootApplication
@RestController
@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableScheduling
public class Application {

    @RequestMapping("/health")
    public String health() {
        return "";
    }

    public static void main(String[] args) {
        System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");
        new SpringApplicationBuilder(Application.class).web(true).run(args);
    }

}
