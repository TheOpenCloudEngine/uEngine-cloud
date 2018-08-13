package org.uengine.zuul;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Data
@ConfigurationProperties(prefix = "zuul")
public class RuleService {

    private Map<String, Map> routes;

    private boolean iamAuthentication;
    private String iamJwtKey;
    private String metadataEncoderSecret1;
    private String metadataEncoderSecret2;
    private String[] secureMetadataFields;

    private Usage[] usages;

    @Data
    @NoArgsConstructor
    public static class Usage {
        private String path;
        private String[] billingPlans;
        private String unit;
        private Integer[] when;
    }
}
