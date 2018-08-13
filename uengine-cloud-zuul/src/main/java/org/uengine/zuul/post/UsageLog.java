package org.uengine.zuul.post;

import lombok.Data;
import org.uengine.zuul.billing.UserSubscriptions;

import java.sql.Timestamp;

@Data
public class UsageLog {
    String userName;
    String accountId;
    String clientKey;
    String plan;
    String subscriptionId;
    String unit;
    Timestamp timestamp;
    String organizationId;
    String product;
}
