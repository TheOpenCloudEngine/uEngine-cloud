package org.uengine.cloud.snapshot;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.uengine.cloud.app.AppConfigYmlResource;
import org.uengine.cloud.app.AppEntity;
import org.uengine.iam.util.JsonUtils;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "app_group_snapshot")
public class AppGroupSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String iam;

    @Column(name = "app_group_id")
    private Long appGroupId;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "app_group_snapshot_id")
    private Set<AppSnapshot> snapshots = new HashSet<>();

    @Column(name = "regDate", nullable = false, updatable = false, insertable = true)
    private long regDate;

    @PrePersist
    void preInsert() {
        this.regDate = new Date().getTime();
    }

    @PostPersist
    void postInsert() {

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

    public String getIam() {
        return iam;
    }

    public void setIam(String iam) {
        this.iam = iam;
    }

    public Long getAppGroupId() {
        return appGroupId;
    }

    public void setAppGroupId(Long appGroupId) {
        this.appGroupId = appGroupId;
    }

    public Set<AppSnapshot> getSnapshots() {
        return snapshots;
    }

    public void setSnapshots(Set<AppSnapshot> snapshots) {
        this.snapshots = snapshots;
    }

    public long getRegDate() {
        return regDate;
    }

    public void setRegDate(long regDate) {
        this.regDate = regDate;
    }
}
