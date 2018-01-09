package org.uengine.iam;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.uengine.iam.oauthuser.OauthUserRepository;
import org.uengine.iam.provider.MyUserRepositoryImpl;

/**
 * Created by uengine on 2017. 11. 1..
 */
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    @Bean
    public OauthUserRepository oauthUserRepository() throws Exception {
        return new MyUserRepositoryImpl();
    }
}
