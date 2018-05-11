package org.uengine.cloud.app;

import org.uengine.iam.client.model.OauthUser;

/**
 * Created by uengine on 2017. 11. 16..
 */
public class AppCreate {
    private String categoryItemId;
    private Double cpu;
    private int mem;
    private int instances;
    private int appNumber;
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
    private OauthUser user;
    private String repoType;
    private String namespace;
    private String repositoryName;
    private int gitlabProjectId;
    private Long githubRepoId;
    private String importGitUrl;
    private AppManageMethod appManageMethod;

    TemplateSpecific templateSpecific;

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

    public OauthUser getUser() {
        return user;
    }

    public void setUser(OauthUser user) {
        this.user = user;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public TemplateSpecific getTemplateSpecific() {
        return templateSpecific;
    }

    public void setTemplateSpecific(TemplateSpecific templateSpecific) {
        this.templateSpecific = templateSpecific;
    }


    public String getRepoType() {
        return repoType;
    }

    public void setRepoType(String repoType) {
        this.repoType = repoType;
    }

    public Long getGithubRepoId() {
        return githubRepoId;
    }

    public void setGithubRepoId(Long githubRepoId) {
        this.githubRepoId = githubRepoId;
    }

    public String getImportGitUrl() {
        return importGitUrl;
    }

    public void setImportGitUrl(String importGitUrl) {
        this.importGitUrl = importGitUrl;
    }

    public AppManageMethod getAppManageMethod() {
        return appManageMethod;
    }

    public void setAppManageMethod(AppManageMethod appManageMethod) {
        this.appManageMethod = appManageMethod;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public int getGitlabProjectId() {
        return gitlabProjectId;
    }

    public void setGitlabProjectId(int gitlabProjectId) {
        this.gitlabProjectId = gitlabProjectId;
    }
}
