package org.uengine.cloud;

import org.gitlab4j.api.GitLabApi;
import org.uengine.iam.client.IamClient;
import org.uengine.iam.util.ApplicationContextRegistry;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.util.UrlPathHelper;
import org.uengine.cloud.tenant.TenantAwareFilter;

/**
 * Created by uengine on 2017. 11. 1..
 */
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    @Autowired
    Environment environment;

    @Autowired
    ApplicationContext applicationContext;

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        urlPathHelper.setUrlDecode(false);
        configurer.setUrlPathHelper(urlPathHelper);
    }

//    @Bean
//    public TenantAwareFilter tenantAwareFilter() {
//        return new TenantAwareFilter();
//    }
//
//    @Bean
//    public RestFilter restFilter() {
//        return new RestFilter();
//    }
//
//    @Bean
//    public CorsFilter corsFilter() {
//        return new CorsFilter();
//    }

    @Bean
    public GitLabApi gitLabApi() {
        String host = environment.getProperty("gitlab.host");
        String token = environment.getProperty("gitlab.token");
        GitLabApi gitLabApi = new GitLabApi(host, token);
        gitLabApi.setDefaultPerPage(10000);
        return gitLabApi;
    }

    @Bean
    public IamClient iamClient() {
        String host = environment.getProperty("iam.host");
        int port = Integer.parseInt(environment.getProperty("iam.port"));
        String clientId = environment.getProperty("iam.clientId");
        String clientSecret = environment.getProperty("iam.clientSecret");
        IamClient iamClient = new IamClient(host, port, clientId, clientSecret);
        return iamClient;
    }

    @Bean
    public TaskScheduler taskScheduler() {
        return new ConcurrentTaskScheduler();
    }

    @Bean
    public Scheduler scheduler() throws SchedulerException {
        SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        Scheduler scheduler = schedulerFactory.getScheduler();
        scheduler.start();
        return scheduler;
    }

    @Bean
    public ApplicationContextRegistry applicationContextRegistry() {
        ApplicationContextRegistry contextRegistry = new ApplicationContextRegistry();
        contextRegistry.setApplicationContext(applicationContext);
        return contextRegistry;
    }
}
