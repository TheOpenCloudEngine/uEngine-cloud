package org.uengine.zuul.model;

/**
 * Created by uengine on 2017. 10. 30..
 */
public class App {
    private AppType appType;
    private String owner;
    private Gitlab gitlab;
    private DcosState prod;
    private DcosState stg;
    private DcosState dev;

    public AppType getAppType() {
        return appType;
    }

    public void setAppType(AppType appType) {
        this.appType = appType;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Gitlab getGitlab() {
        return gitlab;
    }

    public void setGitlab(Gitlab gitlab) {
        this.gitlab = gitlab;
    }

    public DcosState getProd() {
        return prod;
    }

    public void setProd(DcosState prod) {
        this.prod = prod;
    }

    public DcosState getStg() {
        return stg;
    }

    public void setStg(DcosState stg) {
        this.stg = stg;
    }

    public DcosState getDev() {
        return dev;
    }

    public void setDev(DcosState dev) {
        this.dev = dev;
    }
}
