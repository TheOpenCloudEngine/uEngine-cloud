package org.uengine.zuul;

import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard;
import org.springframework.cloud.netflix.turbine.EnableTurbine;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.uengine.iam.util.ApplicationContextRegistry;
import org.uengine.zuul.post.LogFilter;
import org.uengine.zuul.pre.*;

/**
 * Created by uengine on 2017. 10. 5..
 */
@SpringBootApplication
@EnableZuulProxy
@EnableDiscoveryClient
@EnableCircuitBreaker
@EnableHystrix
@EnableHystrixDashboard
@EnableTurbine
@EnableScheduling
@RestController
public class Application {

    @Autowired
    ApplicationContext applicationContext;

    @RequestMapping("/health")
    public String home() throws Exception {
        return "";
    }

    @Bean
    public AddFallbackProvider addResponseHeaderFilter() {
        return new AddFallbackProvider();
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class).web(true).run(args);
    }

    @Bean
    public CorsFilter corsFilter() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        final CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("OPTIONS");
        config.addAllowedMethod("HEAD");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("PATCH");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    @Bean
    public BaseFilter tokenFilter(RouteLocator routeLocator) {
        return new BaseFilter(routeLocator);
    }

    @Bean
    public IAMFilter iamFilter() {
        return new IAMFilter();
    }

    @Bean
    public CustomHeaderFilter addingCustomHeaderFilter() {
        return new CustomHeaderFilter();
    }

    @Bean
    public BillingRouterFilter billingRouterFilter() {
        return new BillingRouterFilter();
    }

    @Bean
    public BillingUsageFilter billingUsageFilter() {
        return new BillingUsageFilter();
    }

    @Bean
    public LogFilter logFilter() {
        return new LogFilter();
    }

    @Bean
    public ApplicationContextRegistry applicationContextRegistry(ApplicationContext applicationContext){
        ApplicationContextRegistry contextRegistry = new ApplicationContextRegistry();
        contextRegistry.setApplicationContext(applicationContext);
        return contextRegistry;
    }
}
