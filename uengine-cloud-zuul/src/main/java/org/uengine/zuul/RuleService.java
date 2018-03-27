package org.uengine.zuul;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@ConfigurationProperties(prefix = "zuul")
public class RuleService {

    private Map<String, Map> routes;

    private boolean iamAuthentication;
    private String iamJwtKey;
    private String metadataEncoderSecret1;
    private String metadataEncoderSecret2;
    private String[] secureMetadataFields;

    public boolean getIamAuthentication() {
        return iamAuthentication;
    }

    public void setIamAuthentication(boolean iamAuthentication) {
        this.iamAuthentication = iamAuthentication;
    }

    public String getIamJwtKey() {
        return iamJwtKey;
    }

    public void setIamJwtKey(String iamJwtKey) {
        this.iamJwtKey = iamJwtKey;
    }

    public Map<String, Map> getRoutes() {
        return routes;
    }

    public void setRoutes(Map<String, Map> routes) {
        this.routes = routes;
    }

    public String getMetadataEncoderSecret1() {
        return metadataEncoderSecret1;
    }

    public void setMetadataEncoderSecret1(String metadataEncoderSecret1) {
        this.metadataEncoderSecret1 = metadataEncoderSecret1;
    }

    public String getMetadataEncoderSecret2() {
        return metadataEncoderSecret2;
    }

    public void setMetadataEncoderSecret2(String metadataEncoderSecret2) {
        this.metadataEncoderSecret2 = metadataEncoderSecret2;
    }

    public String[] getSecureMetadataFields() {
        return secureMetadataFields;
    }

    public void setSecureMetadataFields(String[] secureMetadataFields) {
        this.secureMetadataFields = secureMetadataFields;
    }
}
