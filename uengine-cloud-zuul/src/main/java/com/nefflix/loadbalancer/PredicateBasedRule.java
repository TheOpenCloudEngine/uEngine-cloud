package com.nefflix.loadbalancer;

/**
 * Created by uengine on 2017. 12. 20..
 */

import com.google.common.base.Optional;
import com.netflix.loadbalancer.AbstractServerPredicate;
import com.netflix.loadbalancer.ClientConfigEnabledRoundRobinRule;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;

import java.util.List;

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
        return servers;
    }
}
