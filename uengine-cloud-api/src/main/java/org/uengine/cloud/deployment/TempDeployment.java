package org.uengine.cloud.deployment;

public class TempDeployment {
    private String name;
    private String description;
    private DeploymentStatus status;
    private Long startTime;
    private Long rollbackStartTime;
    private Long deploymentEndTime;
    private Long endTime;
    private String deploymentId;
    private String currentStep;
    private int minuteFromDeployment;
    private String commit;
    private String commitOld;

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

    public String getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(String currentStep) {
        this.currentStep = currentStep;
    }

    public int getMinuteFromDeployment() {
        return minuteFromDeployment;
    }

    public void setMinuteFromDeployment(int minuteFromDeployment) {
        this.minuteFromDeployment = minuteFromDeployment;
    }

    public Long getRollbackStartTime() {
        return rollbackStartTime;
    }

    public void setRollbackStartTime(Long rollbackStartTime) {
        this.rollbackStartTime = rollbackStartTime;
    }

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public String getCommitOld() {
        return commitOld;
    }

    public void setCommitOld(String commitOld) {
        this.commitOld = commitOld;
    }
}
