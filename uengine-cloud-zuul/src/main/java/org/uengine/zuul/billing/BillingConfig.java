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
@ConfigurationProperties(prefix = "zuul.billing")
public class BillingConfig {
    private boolean enable;
    private String url;
    private String authentication;
    private String organization;
}
