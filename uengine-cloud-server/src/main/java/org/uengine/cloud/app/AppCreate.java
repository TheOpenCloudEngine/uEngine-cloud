package org.uengine.cloud.app;

import java.util.Map;

/**
 * Created by uengine on 2017. 11. 16..
 */
public class AppCreate {
    private String categoryItemId;
    private Double cpu;
    private int mem;
    private int instances;
    private int appNumber;
    private int projectId;
    private String appName;
    private String externalProdDomain;
    private String externalStgDomain;
    private String externalDevDomain;
    private String internalProdDomain;
    private String internalStgDomain;
    private String internalDevDomain;
    private int prodPort;
    private int stgPort;
    private int devPort;
    private Map user;
    private String namespace;

    public String getCategoryItemId() {
        return categoryItemId;
    }

    public void setCategoryItemId(String categoryItemId) {
        this.categoryItemId = categoryItemId;
    }

    public Double getCpu() {
        return cpu;
    }

    public void setCpu(Double cpu) {
        this.cpu = cpu;
    }

    public int getMem() {
        return mem;
    }

    public void setMem(int mem) {
        this.mem = mem;
    }

    public int getInstances() {
        return instances;
    }

    public void setInstances(int instances) {
        this.instances = instances;
    }

    public int getAppNumber() {
        return appNumber;
    }

    public void setAppNumber(int appNumber) {
        this.appNumber = appNumber;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getExternalProdDomain() {
        return externalProdDomain;
    }

    public void setExternalProdDomain(String externalProdDomain) {
        this.externalProdDomain = externalProdDomain;
    }

    public String getExternalStgDomain() {
        return externalStgDomain;
    }

    public void setExternalStgDomain(String externalStgDomain) {
        this.externalStgDomain = externalStgDomain;
    }

    public String getExternalDevDomain() {
        return externalDevDomain;
    }

    public void setExternalDevDomain(String externalDevDomain) {
        this.externalDevDomain = externalDevDomain;
    }

    public String getInternalProdDomain() {
        return internalProdDomain;
    }

    public void setInternalProdDomain(String internalProdDomain) {
        this.internalProdDomain = internalProdDomain;
    }

    public String getInternalStgDomain() {
        return internalStgDomain;
    }

    public void setInternalStgDomain(String internalStgDomain) {
        this.internalStgDomain = internalStgDomain;
    }

    public String getInternalDevDomain() {
        return internalDevDomain;
    }

    public void setInternalDevDomain(String internalDevDomain) {
        this.internalDevDomain = internalDevDomain;
    }

    public int getProdPort() {
        return prodPort;
    }

    public void setProdPort(int prodPort) {
        this.prodPort = prodPort;
    }

    public int getStgPort() {
        return stgPort;
    }

    public void setStgPort(int stgPort) {
        this.stgPort = stgPort;
    }

    public int getDevPort() {
        return devPort;
    }

    public void setDevPort(int devPort) {
        this.devPort = devPort;
    }

    public Map getUser() {
        return user;
    }

    public void setUser(Map user) {
        this.user = user;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
