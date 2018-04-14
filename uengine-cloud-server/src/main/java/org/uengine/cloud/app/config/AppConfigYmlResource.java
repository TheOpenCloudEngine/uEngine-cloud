package org.uengine.cloud.app.config;

import java.util.Map;

public class AppConfigYmlResource {

    private Map mesosDev;

    private Map mesosStg;

    private Map mesosProd;

    private String application;

    private String commonYml;

    private String devYml;

    private String stgYml;

    private String prodYml;

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getCommonYml() {
        return commonYml;
    }

    public void setCommonYml(String commonYml) {
        this.commonYml = commonYml;
    }

    public String getDevYml() {
        return devYml;
    }

    public void setDevYml(String devYml) {
        this.devYml = devYml;
    }

    public String getStgYml() {
        return stgYml;
    }

    public void setStgYml(String stgYml) {
        this.stgYml = stgYml;
    }

    public String getProdYml() {
        return prodYml;
    }

    public void setProdYml(String prodYml) {
        this.prodYml = prodYml;
    }

    public Map getMesosDev() {
        return mesosDev;
    }

    public void setMesosDev(Map mesosDev) {
        this.mesosDev = mesosDev;
    }

    public Map getMesosStg() {
        return mesosStg;
    }

    public void setMesosStg(Map mesosStg) {
        this.mesosStg = mesosStg;
    }

    public Map getMesosProd() {
        return mesosProd;
    }

    public void setMesosProd(Map mesosProd) {
        this.mesosProd = mesosProd;
    }
}
