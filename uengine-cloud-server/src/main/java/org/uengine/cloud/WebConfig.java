package org.uengine.cloud;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gitlab4j.api.GitLabApi;
import org.uengine.iam.client.IamClient;
import org.uengine.iam.util.ApplicationContextRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.util.UrlPathHelper;

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
    public IamClient iamClient() {
        String host = environment.getProperty("iam.host");
        int port = Integer.parseInt(environment.getProperty("iam.port"));
        String clientId = environment.getProperty("iam.clientId");
        String clientSecret = environment.getProperty("iam.clientSecret");
        IamClient iamClient = new IamClient(host, port, clientId, clientSecret);
        return iamClient;
    }

    @Bean
    public ApplicationContextRegistry applicationContextRegistry() {
        ApplicationContextRegistry contextRegistry = new ApplicationContextRegistry();
        contextRegistry.setApplicationContext(applicationContext);
        return contextRegistry;
    }


    @Bean
    public ObjectMapper objectMapper(){
        return createTypedJsonObjectMapper();
    }


    public static ObjectMapper createTypedJsonObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();

        objectMapper.setVisibilityChecker(objectMapper.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // ignore null
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_DEFAULT); // ignore zero and false when it is int or boolean
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        objectMapper.enableDefaultTypingAsProperty(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE, "_type");
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

        return objectMapper;
    }
}
