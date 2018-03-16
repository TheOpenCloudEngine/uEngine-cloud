package org.uengine.cloud.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.gitlab4j.api.models.Member;
import org.uengine.cloud.group.AppGroup;
import org.uengine.iam.util.JsonUtils;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    //클라우드 콘피그 서버에서 dcos 로 앱정보 받으려면...? 마스터 키가 필요...?
    //마스터키는 dcos 서버 프리패스 토큰

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
    private List<Member> members;

    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
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

    @ManyToMany(fetch = FetchType.LAZY, mappedBy = "apps")
    private Set<AppGroup> groups = new HashSet<>();

    public Set<AppGroup> getGroups() {
        return groups;
    }

    public void setGroups(Set<AppGroup> groups) {
        this.groups = groups;
    }
}
