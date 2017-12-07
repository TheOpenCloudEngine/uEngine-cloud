package org.uengine.zuul;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractLoadBalancerRule;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;
import com.netflix.zuul.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.uengine.zuul.model.App;
import org.uengine.zuul.model.AppType;
import org.uengine.zuul.model.DcosState;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by uengine on 2017. 10. 9..
 */
public class BlueGreenRoundRobinRule extends AbstractLoadBalancerRule {

    @Autowired
    Environment environment;

    @Autowired
    RouteService routeService;

    private AtomicInteger nextServerCyclicCounter;
    private static final boolean AVAILABLE_ONLY_SERVERS = true;
    private static final boolean ALL_SERVERS = false;
    private static Logger log = LoggerFactory.getLogger(BlueGreenRoundRobinRule.class);

    public BlueGreenRoundRobinRule() {
        this.nextServerCyclicCounter = new AtomicInteger(0);
    }

    public BlueGreenRoundRobinRule(ILoadBalancer lb) {
        this();
        this.setLoadBalancer(lb);
    }

    private List getFilteredServers(List servers) {
        List<DiscoveryEnabledServer> filteredServers = new ArrayList();

        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();
        String serverName = request.getServerName();
        String serviceId = (String) currentContext.get("serviceId");

        String role = null;
        try {
            role = environment.getProperty("dcos.deployment.role");
        } catch (Exception ex) {
            role = "prod";
        }
        System.out.println("serviceId : " + serviceId + " role:" + role);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        Map apps = routeService.getApps();
        Map<String, Map> _apps = (Map) ((Map) apps.get("dcos")).get("apps");
        DcosState dcosState = null;

        if (!_apps.isEmpty()) {
            for (Map.Entry<String, Map> entry : _apps.entrySet()) {
                //서비스 아이디 붙일 것.
                App app = objectMapper.convertValue(entry.getValue(), App.class);
                //스프링 부트이고, serviceId 가 동일할 경우 경우
                if (AppType.springboot.toString().equals(app.getAppType().toString())
                        && serviceId.equals(entry.getKey())) {
                    if (role.equals("prod")) {
                        dcosState = app.getProd();
                    } else if (role.equals("stg")) {
                        dcosState = app.getStg();
                    } else {
                        dcosState = app.getDev();
                    }
                    if (dcosState != null) {
                        BlueGreen blueGreen = dcosState.getDeployment();
                        if (servers != null) {
                            for (int i = 0; i < servers.size(); i++) {
                                DiscoveryEnabledServer server = (DiscoveryEnabledServer) servers.get(i);
                                InstanceInfo instanceInfo = server.getInstanceInfo();
                                Map<String, String> metadata = instanceInfo.getMetadata();
                                if (metadata != null && metadata.containsKey("deployment")) {
                                    if (metadata.get("deployment").toString().equals(blueGreen.toString())) {
                                        filteredServers.add(server);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return filteredServers;
    }

    private List getFilteredReachableServers(ILoadBalancer lb) {
        return this.getFilteredServers(lb.getReachableServers());
    }

    private List getFilteredAllServers(ILoadBalancer lb) {
        return this.getFilteredServers(lb.getAllServers());
    }

    public Server choose(ILoadBalancer lb, Object key) {
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletRequest request = currentContext.getRequest();
        String serverName = request.getServerName();
        String serviceId = (String) currentContext.get("serviceId");

        if (lb == null) {
            log.warn("no load balancer");
            return null;
        } else {
            Server server = null;
            int count = 0;

            while (true) {
                if (server == null && count++ < 10) {
                    List reachableServers = this.getFilteredReachableServers(lb);
                    List allServers = this.getFilteredAllServers(lb);

                    int upCount = reachableServers.size();
                    int serverCount = allServers.size();
                    if (upCount != 0 && serverCount != 0) {
                        int nextServerIndex = this.incrementAndGetModulo(serverCount);
                        server = (Server) allServers.get(nextServerIndex);
                        if (server == null) {
                            Thread.yield();
                        } else {
                            if (server.isAlive() && server.isReadyToServe()) {
                                return server;
                            }

                            server = null;
                        }
                        continue;
                    }

                    log.warn("No up servers available from load balancer: " + lb);
                    return null;
                }

                if (count >= 10) {
                    log.warn("No available alive servers after 10 tries from load balancer: " + lb);
                }

                return server;
            }
        }
    }

    private int incrementAndGetModulo(int modulo) {
        int current;
        int next;
        do {
            current = this.nextServerCyclicCounter.get();
            next = (current + 1) % modulo;
        } while (!this.nextServerCyclicCounter.compareAndSet(current, next));

        return next;
    }

    public Server choose(Object key) {
        return this.choose(this.getLoadBalancer(), key);
    }

    public void initWithNiwsConfig(IClientConfig clientConfig) {
    }
}
