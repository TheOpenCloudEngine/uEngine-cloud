package org.uengine.cloud.app.git;

import org.gitlab4j.api.GitLabApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class GitConfig {

    @Autowired
    private Environment environment;

    @Bean
    public GitLabApi gitLabApi() {
        String host = environment.getProperty("gitlab.host");
        String token = environment.getProperty("gitlab.token");
        GitLabApi gitLabApi = new GitLabApi(host, token);
        gitLabApi.setDefaultPerPage(10000);
        return gitLabApi;
    }
}
