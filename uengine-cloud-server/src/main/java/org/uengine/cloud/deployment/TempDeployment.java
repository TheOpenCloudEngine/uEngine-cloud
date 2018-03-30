package org.uengine.cloud.deployment;

public class TempDeployment {
    private String name;
    private String description;
    private DeploymentStatus status;
    private Long startTime;
    private Long deploymentEndTime;
    private Long endTime;
    private String deploymentId;

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

    public DeploymentStatus getStatus() {
        return status;
    }

    public void setStatus(DeploymentStatus status) {
        this.status = status;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getDeploymentEndTime() {
        return deploymentEndTime;
    }

    public void setDeploymentEndTime(Long deploymentEndTime) {
        this.deploymentEndTime = deploymentEndTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }
}
