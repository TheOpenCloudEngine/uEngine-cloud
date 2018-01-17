package com.netflix.loadbalancer;

/**
 * Created by uengine on 2017. 12. 20..
 */

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;
import com.netflix.zuul.context.RequestContext;
import org.uengine.iam.util.ApplicationContextRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.uengine.zuul.BlueGreen;
import org.uengine.zuul.RouteService;
import org.uengine.zuul.model.App;
import org.uengine.zuul.model.DcosState;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class PredicateBasedRule extends ClientConfigEnabledRoundRobinRule {
    public PredicateBasedRule() {
    }

    public abstract AbstractServerPredicate getPredicate();

    public Server choose(Object key) {
        ILoadBalancer lb = this.getLoadBalancer();
        Optional server = this.getPredicate().chooseRoundRobinAfterFiltering(this.getFilteredServers(lb.getAllServers()), key);
        return server.isPresent() ? (Server) server.get() : null;
    }

    private List getFilteredServers(List servers) {
        List<DiscoveryEnabledServer> filteredServers = new ArrayList();
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();
        String serverName = request.getServerName();
        String serviceId = (String) currentContext.get("serviceId");

        ApplicationContext applicationContext = ApplicationContextRegistry.getApplicationContext();
        RouteService routeService = applicationContext.getBean(RouteService.class);
        Environment environment = applicationContext.getBean(Environment.class);

        String role = null;
        try {
            role = environment.getProperty("dcos.deployment.role");
        } catch (Exception ex) {
            role = "local";
        }
        System.out.println("serviceId : " + serviceId + " role:" + role);

        //로컬 개발일 경우는 바로 통과시킨다.
        if (role.equals("local")) {
            return servers;
        }

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Map apps = routeService.getApps();
        Map<String, Map> _apps = (Map) ((Map) apps.get("dcos")).get("apps");
        DcosState dcosState = null;

        if (!_apps.isEmpty() && servers != null) {
            for (int i = 0; i < servers.size(); i++) {
                DiscoveryEnabledServer server = (DiscoveryEnabledServer) servers.get(i);
                InstanceInfo instanceInfo = server.getInstanceInfo();
                Map<String, String> metadata = instanceInfo.getMetadata();
                if (metadata != null && metadata.containsKey("deployment")) {
                    String deployment = metadata.get("deployment");
                    String appname = metadata.get("appname");

                    for (Map.Entry<String, Map> entry : _apps.entrySet()) {
                        App app = objectMapper.convertValue(entry.getValue(), App.class);

                        //appname 이 동일할 경우
                        if (appname.equals(entry.getKey())) {
                            if (role.equals("prod")) {
                                dcosState = app.getProd();
                            } else if (role.equals("stg")) {
                                dcosState = app.getStg();
                            } else {
                                dcosState = app.getDev();
                            }
                            if (dcosState != null) {
                                BlueGreen blueGreen = dcosState.getDeployment();
                                if (blueGreen.toString().equals(deployment)) {
                                    filteredServers.add(server);
                                }
                            }
                        }
                    }
                }
            }
        }
        return filteredServers;
    }
}
