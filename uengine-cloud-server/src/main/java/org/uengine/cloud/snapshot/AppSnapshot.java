package org.uengine.cloud.snapshot;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.uengine.cloud.app.AppConfigYmlResource;
import org.uengine.cloud.app.AppEntity;
import org.uengine.iam.util.JsonUtils;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "app_snapshot")
public class AppSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String appName;

    private String iam;

    @Column(name="app_group_snapshot_id")
    private Long appGroupSnapshotId;

    @JsonIgnore
    @Column(columnDefinition = "TEXT")
    private String appString;

    public AppEntity getApp() {
        try {
            return JsonUtils.convertValue(JsonUtils.unmarshal(this.appString), AppEntity.class);
        } catch (Exception ex) {
            return new AppEntity();
        }
    }

    public void setApp(AppEntity appEntity) {
        try {
            this.appString = JsonUtils.marshal(appEntity);
        } catch (Exception ex) {
            this.appString = "{}";
        }
    }

    @JsonIgnore
    @Column(columnDefinition = "TEXT")
    private String configYmlResourceString;

    public AppConfigYmlResource getAppConfigYmlResource() {
        try {
            return JsonUtils.convertValue(JsonUtils.unmarshal(this.configYmlResourceString), AppConfigYmlResource.class);
        } catch (Exception ex) {
            return new AppConfigYmlResource();
        }
    }

    public void setAppConfigYmlResource(AppConfigYmlResource configYmlResource) {
        try {
            this.configYmlResourceString = JsonUtils.marshal(configYmlResource);
        } catch (Exception ex) {
            this.configYmlResourceString = "{}";
        }
    }

    @Column(name = "regDate", nullable = false, updatable = false, insertable = true)
    private long regDate;

    @PrePersist
    void preInsert() {
        this.regDate = new Date().getTime();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getIam() {
        return iam;
    }

    public void setIam(String iam) {
        this.iam = iam;
    }

    public long getRegDate() {
        return regDate;
    }

    public void setRegDate(long regDate) {
        this.regDate = regDate;
    }

    public Long getAppGroupSnapshotId() {
        return appGroupSnapshotId;
    }

    public void setAppGroupSnapshotId(Long appGroupSnapshotId) {
        this.appGroupSnapshotId = appGroupSnapshotId;
    }
}
