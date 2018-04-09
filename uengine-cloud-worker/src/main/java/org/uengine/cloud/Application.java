package org.uengine.cloud;

import com.launchdarkly.eventsource.EventSource;
import com.netflix.discovery.converters.Auto;
import okhttp3.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.uengine.cloud.listener.MesosChangeHandler;
import org.uengine.cloud.listener.MesosListenerStarter;
import org.uengine.cloud.producer.KafkaSender;

import java.net.URI;
import java.util.concurrent.TimeUnit;

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
@EnableAsync
public class Application {

    @RequestMapping("/health")
    public String health() {
        return "";
    }

    public static void main(String[] args) throws Exception {
        System.setProperty("org.apache.tomcat.util.buf.UDecoder.ALLOW_ENCODED_SLASH", "true");
        ConfigurableApplicationContext ctx = new SpringApplicationBuilder(Application.class).web(true).run(args);

        KafkaSender sender = ctx.getBean(KafkaSender.class);
        sender.send("mesos.topic", "aaaa");
        MesosListenerStarter listenerStarter = ctx.getBean(MesosListenerStarter.class);
        listenerStarter.run();
    }
}
