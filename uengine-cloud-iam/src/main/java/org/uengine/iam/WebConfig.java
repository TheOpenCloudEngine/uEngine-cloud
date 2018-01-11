package org.uengine.iam;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.gitlab4j.api.GitLabApi;
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
import org.uengine.iam.oauthuser.OauthUserRepository;
import org.uengine.iam.provider.MyUserRepositoryImpl;
import org.uengine.iam.security.CorsFilter;
import org.uengine.iam.util.ApplicationContextRegistry;
import org.uengine.iam.util.HttpUtils;
import org.uengine.iam.util.JsonUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by uengine on 2017. 11. 1..
 */
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

    @Autowired
    private Environment environment;

    @Bean
    public OauthUserRepository oauthUserRepository() throws Exception {
        return new MyUserRepositoryImpl();
    }

    @Bean
    public GitLabApi gitLabApi() throws Exception {

        //깃랩 호스트 정보 얻기
        //http://config.pas-mini.io/uengine-cloud-server.json 로 get 을 날려야함.
        String configServerUrl = environment.getProperty("spring.cloud.config.uri");
        HttpResponse res = new HttpUtils().makeRequest("GET",
                configServerUrl + "/uengine-cloud-server.json",
                null,
                new HashMap<>()
        );
        HttpEntity entity = res.getEntity();
        String json = EntityUtils.toString(entity);
        Map cloudServerConfigJson = JsonUtils.unmarshal(json);

        //깃랩 빈 등록.
        String host = ((Map) cloudServerConfigJson.get("gitlab")).get("host").toString();
        String token = ((Map) cloudServerConfigJson.get("gitlab")).get("token").toString();
        GitLabApi gitLabApi = new GitLabApi(host, token);
        gitLabApi.setDefaultPerPage(10000);
        return gitLabApi;
    }
}
