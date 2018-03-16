package org.uengine.cloud.app;

public class AppConfigYmlResource {

    private String application;

    private String common;

    private String dev;

    private String stg;

    private String prod;

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getCommon() {
        return common;
    }

    public void setCommon(String common) {
        this.common = common;
    }

    public String getDev() {
        return dev;
    }

    public void setDev(String dev) {
        this.dev = dev;
    }

    public String getStg() {
        return stg;
    }

    public void setStg(String stg) {
        this.stg = stg;
    }

    public String getProd() {
        return prod;
    }

    public void setProd(String prod) {
        this.prod = prod;
    }
}
