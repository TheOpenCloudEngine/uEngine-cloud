package org.uengine.zuul.billing;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class UserSubscriptions {
    private String accountId;
    private List<Subscription> subscriptions;

    @Data
    @NoArgsConstructor
    public static class Subscription {
        private String id;
        private String plan;
        private String product;
    }
}
