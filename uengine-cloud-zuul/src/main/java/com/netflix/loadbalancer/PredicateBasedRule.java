package com.netflix.loadbalancer;

/**
 * Created by uengine on 2017. 12. 20..
 */

import com.google.common.base.Optional;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;
import com.netflix.zuul.context.RequestContext;
import org.uengine.iam.util.ApplicationContextRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

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
        String serviceId = (String) currentContext.get("serviceId");

        ApplicationContext applicationContext = ApplicationContextRegistry.getApplicationContext();
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

        for (int i = 0; i < servers.size(); i++) {
            DiscoveryEnabledServer server = (DiscoveryEnabledServer) servers.get(i);
            InstanceInfo instanceInfo = server.getInstanceInfo();
            Map<String, String> metadata = instanceInfo.getMetadata();
            if (metadata != null && metadata.containsKey("profile")) {
                String profile = metadata.get("profile");
                if (role.equals(profile)) {
                    filteredServers.add(server);
                }
            }
        }
        return filteredServers;
    }
}
