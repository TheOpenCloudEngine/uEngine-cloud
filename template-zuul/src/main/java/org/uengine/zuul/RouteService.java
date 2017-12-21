package org.uengine.zuul;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.uengine.iam.util.ServiceException;
import org.uengine.iam.util.HttpUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by uengine on 2017. 10. 29..
 */
@Service
public class RouteService implements InitializingBean {

    @Autowired
    Environment environment;

    private Map apps = null;

    public Map getApps() {
        return apps;
    }

    public void setApps(Map apps) {
        this.apps = apps;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String role = null;
        try {
            role = environment.getProperty("dcos.deployment.role");
        } catch (Exception ex) {
            role = "local";
        }
        if (!role.equals("local")) {
            this.refreshApps();
        }
    }

    public void refreshApps() {
        //String type, String uri, String data, Map<String, String> headers
        String configUrl = environment.getProperty("spring.cloud.config.uri");
        String url = configUrl + "/" + environment.getProperty("dcos.apps-file");
        try {
            HttpResponse httpResponse = new HttpUtils().makeRequest("GET", url, null, new HashMap<>());
            HttpEntity entity = httpResponse.getEntity();
            String yaml = EntityUtils.toString(entity);

            ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
            Object obj = yamlReader.readValue(yaml, Object.class);

            this.setApps((Map) obj);

        } catch (Exception ex) {
            throw new ServiceException(ex);
        }
    }

}
