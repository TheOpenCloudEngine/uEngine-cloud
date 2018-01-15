package org.uengine.iam.provider;

import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.models.RepositoryFile;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.uengine.iam.util.HttpUtils;
import org.uengine.iam.util.JsonUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by uengine on 2017. 11. 20..
 */
@Service
public class GitlabExtentApi implements InitializingBean {

    @Autowired
    Environment environment;

    @Autowired
    GitLabApi gitLabApi;

    private String host;
    private String token;

    @Override
    public void afterPropertiesSet() throws Exception {
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

        this.host = ((Map) cloudServerConfigJson.get("gitlab")).get("host").toString();
        this.token = ((Map) cloudServerConfigJson.get("gitlab")).get("token").toString();
    }

    private Map<String, String> addHeaders() {
        Map headers = new HashMap();
        headers.put("Content-Type", "application/json");
        headers.put("PRIVATE-TOKEN", token);
        return headers;
    }

    public Map createUser(Map user) throws Exception {
        HttpResponse response = new HttpUtils().makeRequest("POST",
                host + "/api/v4/users",
                JsonUtils.marshal(user),
                this.addHeaders()
        );
        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 201) {
            throw new Exception(json);
        }
        return JsonUtils.unmarshal(json);
    }

    public Map createUserCustomAttributes(Map user) throws Exception {

        Map iamUserCustomAttribute = new HashMap();
        iamUserCustomAttribute.put("value",user.get("userName").toString());

        HttpResponse response = new HttpUtils().makeRequest("PUT",
                host + "/api/v4/users/"+user.get("id")+"/custom_attributes/iam",
                JsonUtils.marshal(iamUserCustomAttribute),
                this.addHeaders()
        );
        HttpEntity entity = response.getEntity();
        String json = EntityUtils.toString(entity);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != 200) {
            throw new Exception(json);
        }
        return JsonUtils.unmarshal(json);
    }
}
