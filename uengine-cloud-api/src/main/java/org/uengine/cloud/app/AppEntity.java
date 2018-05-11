package org.uengine.cloud.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.gitlab4j.api.models.Member;
import org.uengine.iam.util.JsonUtils;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "app_entity")
public class AppEntity {

    @Id
    private String name;

    private String appType;

    private int projectId;

    private String iam;

    private int number;

    private String createStatus;

    private boolean insecureConfig;

    private String configPassword;

    private Long groupId;

    @Column(columnDefinition = "TEXT")
    private String memberIds;

    @JsonIgnore
    @Column(columnDefinition = "TEXT")
    private String devString;

    @JsonIgnore
    @Column(columnDefinition = "TEXT")
    private String stgString;

    @JsonIgnore
    @Column(columnDefinition = "TEXT")
    private String prodString;

    @Column(name = "regDate", nullable = false, updatable = false, insertable = true)
    private long regDate;

    public AppStage getDev() {
        try {
            return JsonUtils.convertValue(JsonUtils.unmarshal(this.devString), AppStage.class);
        } catch (Exception ex) {
            return new AppStage();
        }
    }

    public void setDev(AppStage stage) {
        try {
            this.devString = JsonUtils.marshal(stage);
        } catch (Exception ex) {
            this.devString = "{}";
        }
    }

    public AppStage getStg() {
        try {
            return JsonUtils.convertValue(JsonUtils.unmarshal(this.stgString), AppStage.class);
        } catch (Exception ex) {
            return new AppStage();
        }
    }

    public void setStg(AppStage stage) {
        try {
            this.stgString = JsonUtils.marshal(stage);
        } catch (Exception ex) {
            this.stgString = "{}";
        }
    }

    public AppStage getProd() {
        try {
            return JsonUtils.convertValue(JsonUtils.unmarshal(this.prodString), AppStage.class);
        } catch (Exception ex) {
            return new AppStage();
        }
    }

    public void setProd(AppStage stage) {
        try {
            this.prodString = JsonUtils.marshal(stage);
        } catch (Exception ex) {
            this.prodString = "{}";
        }
    }

    @PrePersist
    void preInsert() {
        this.regDate = new Date().getTime();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public String getIam() {
        return iam;
    }

    public void setIam(String iam) {
        this.iam = iam;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public long getRegDate() {
        return regDate;
    }

    public void setRegDate(long regDate) {
        this.regDate = regDate;
    }

    public String getCreateStatus() {
        return createStatus;
    }

    public void setCreateStatus(String createStatus) {
        this.createStatus = createStatus;
    }

    @Transient
    private int accessLevel;

    public int getAccessLevel() {
        return accessLevel;
    }

    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }

    public boolean getInsecureConfig() {
        return insecureConfig;
    }

    public void setInsecureConfig(boolean insecureConfig) {
        this.insecureConfig = insecureConfig;
    }

    public String getConfigPassword() {
        return configPassword;
    }

    public void setConfigPassword(String configPassword) {
        this.configPassword = configPassword;
    }

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getMemberIds() {
        return memberIds;
    }

    public void setMemberIds(String memberIds) {
        this.memberIds = memberIds;
    }


    private String repoType;

    public String getRepoType() {
        return repoType;
    }

    public void setRepoType(String repoType) {
        this.repoType = repoType;
    }

    private Long githubRepoId;

    public Long getGithubRepoId() {
        return githubRepoId;
    }

    public void setGithubRepoId(Long githubRepoId) {
        this.githubRepoId = githubRepoId;
    }
}
