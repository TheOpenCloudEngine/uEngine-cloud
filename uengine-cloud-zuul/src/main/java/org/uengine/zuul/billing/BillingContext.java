package org.uengine.zuul.billing;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class BillingContext {
    private RouteLog routeLog;
    private UsageLog usageLog;

    @Data
    @NoArgsConstructor
    public static class RouteLog {
        private String plan;
        private String subscriptionId;
        private String product;
    }

    @Data
    @NoArgsConstructor
    public static class UsageLog {
        private String plan;
        private String product;
        private String subscriptionId;
        private String unit;
        private Integer[] when;
    }
}
