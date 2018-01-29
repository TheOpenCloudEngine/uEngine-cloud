package org.uengine.uenginecloudconfig;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

/**
 * Created by uengine on 2018. 1. 22..
 */
@Configuration
public class CustomWebSecurityConfigurerAdapter
        extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.addFilterAfter(
                new CustomFilter(), BasicAuthenticationFilter.class);
    }
}
