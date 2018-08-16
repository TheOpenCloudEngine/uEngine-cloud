package org.uengine.zuul.billing;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Data
@ConfigurationProperties(BillingConfig.PREFIX)
public class BillingConfig {

    public static final String PREFIX = "zuul.billing";

    private boolean enable;
    private String url;
    private String authentication;
    private String organization;
}
