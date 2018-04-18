package org.uengine.cloud.app.deployment;

public class DeployAppMessage {

    private String appName;

    private String stage;

    private String commit;

    private Long snapshotId;

    private String name;

    private String description;

    public DeployAppMessage(String appName, String stage, String commit, Long snapshotId, String name, String description) {
        this.appName = appName;
        this.stage = stage;
        this.commit = commit;
        this.snapshotId = snapshotId;
        this.name = name;
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

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public Long getSnapshotId() {
        return snapshotId;
    }

    public void setSnapshotId(Long snapshotId) {
        this.snapshotId = snapshotId;
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
}
