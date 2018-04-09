package org.uengine.cloud.catalog;

import java.util.List;

/**
 * Created by uengine on 2018. 1. 15..
 */
public class CategoryItem {
    private String id;
    private String category;
    private String header;
    private String title;
    private String description;
    private String version;
    private String type;
    private List<FileMapping> mappings;
    private int projectId;
    private String projectUrl;
    private String logoSrc;

    private String deployDev;
    private String deployStg;
    private String deployProd;

    private String config;
    private String configDev;
    private String configStg;
    private String configProd;

//    @JsonIgnore
//    Map<String, String> templateFiles;
//        public Map<String, String> getTemplateFiles() {
//            return templateFiles;
//        }
//        public void setTemplateFiles(Map<String, String> templateFiles) {
//            this.templateFiles = templateFiles;
//        }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<FileMapping> getMappings() {
        return mappings;
    }

    public void setMappings(List<FileMapping> mappings) {
        this.mappings = mappings;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getProjectUrl() {
        return projectUrl;
    }

    public void setProjectUrl(String projectUrl) {
        this.projectUrl = projectUrl;
    }

    public String getLogoSrc() {
        return logoSrc;
    }

    public void setLogoSrc(String logoSrc) {
        this.logoSrc = logoSrc;
    }

    public String getDeployDev() {
        return deployDev;
    }

    public void setDeployDev(String deployDev) {
        this.deployDev = deployDev;
    }

    public String getDeployStg() {
        return deployStg;
    }

    public void setDeployStg(String deployStg) {
        this.deployStg = deployStg;
    }

    public String getDeployProd() {
        return deployProd;
    }

    public void setDeployProd(String deployProd) {
        this.deployProd = deployProd;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getConfigDev() {
        return configDev;
    }

    public void setConfigDev(String configDev) {
        this.configDev = configDev;
    }

    public String getConfigStg() {
        return configStg;
    }

    public void setConfigStg(String configStg) {
        this.configStg = configStg;
    }

    public String getConfigProd() {
        return configProd;
    }

    public void setConfigProd(String configProd) {
        this.configProd = configProd;
    }
}
