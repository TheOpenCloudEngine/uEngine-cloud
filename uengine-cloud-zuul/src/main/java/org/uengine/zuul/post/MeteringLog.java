package org.uengine.zuul.post;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.uengine.zuul.billing.UserSubscriptions;

import java.sql.Timestamp;
import java.util.List;

@Data
public class MeteringLog {
    String userName;
    String accountId;
    String clientKey;
    String remoteAddr;
    String requestURI;
    String method;
    String serviceId;
    int responseStatus;
    List<Subscription> subscriptions;
    String organizationId;
    Timestamp timestamp;

    @Data
    @NoArgsConstructor
    public static class Subscription {
        private String id;
        private String plan;
        private String product;
    }
}
