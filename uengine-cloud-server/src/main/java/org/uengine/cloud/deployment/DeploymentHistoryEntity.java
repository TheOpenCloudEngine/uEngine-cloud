package org.uengine.cloud.deployment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.uengine.cloud.app.AppEntity;
import org.uengine.cloud.app.AppStage;
import org.uengine.cloud.log.AppLogAction;
import org.uengine.cloud.log.AppLogStatus;
import org.uengine.cloud.strategies.InstanceStrategy;
import org.uengine.iam.util.JsonUtils;

import javax.persistence.*;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Entity
public class DeploymentHistoryEntity {

    public DeploymentHistoryEntity() {
    }

    public DeploymentHistoryEntity(AppEntity appEntity, String stage, DeploymentStatus status) {
        AppStage appStage = null;
        switch (stage) {
            case "dev":
                appStage = appEntity.getDev();
                break;
            case "stg":
                appStage = appEntity.getStg();
                break;
            case "prod":
                appStage = appEntity.getProd();
                break;
        }
        this.setAppName(appEntity.getName());
        this.setAppStage(appStage);
        this.setName(appStage.getTempDeployment().getName());
        this.setDescription(appStage.getTempDeployment().getDescription());
        this.setStartTime(appStage.getTempDeployment().getStartTime());
        this.setEndTime(new Date().getTime());
        this.setStage(stage);
        this.setStatus(status);
        this.setStrategy(appStage.getDeploymentStrategy().getInstanceStrategy());
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String name;
    private String description;
    private String appName;
    private String stage;

    @Enumerated(EnumType.STRING)
    private DeploymentStatus status;

    @Enumerated(EnumType.STRING)
    private InstanceStrategy strategy;

    @Column(name = "start_time", nullable = false, updatable = false, insertable = true)
    private Long startTime;

    @Column(name = "end_time", nullable = false, updatable = false, insertable = true)
    private Long endTime;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public DeploymentStatus getStatus() {
        return status;
    }

    public void setStatus(DeploymentStatus status) {
        this.status = status;
    }

    public InstanceStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(InstanceStrategy strategy) {
        this.strategy = strategy;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    @JsonIgnore
    @Column(columnDefinition = "TEXT")
    private String stageString;

    public AppStage getAppStage() {
        try {
            return JsonUtils.convertValue(JsonUtils.unmarshal(this.stageString), AppStage.class);
        } catch (Exception ex) {
            return new AppStage();
        }
    }

    public void setAppStage(AppStage stage) {
        try {
            this.stageString = JsonUtils.marshal(stage);
        } catch (Exception ex) {
            this.stageString = "{}";
        }
    }
}
