package org.uengine.zuul;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@ConfigurationProperties(prefix = "zuul")
public class RuleService {

    @Autowired
    private Environment environment;

    private Map<String, Map> routes;

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public Map<String, Map> getRoutes() {
        return routes;
    }

    public void setRoutes(Map<String, Map> routes) {
        this.routes = routes;
    }
}
