package org.uengine.cloud.app;

import org.uengine.cloud.deployment.DeploymentStrategy;
import org.uengine.cloud.deployment.TempDeployment;

import java.util.Map;

/**
 * Created by uengine on 2018. 1. 23..
 */
public class AppStage {
    private Long snapshot;
    private String deployment;
    private String external;
    private String internal;
    private String marathonAppId;
    private String commit;
    private int servicePort;
    private Map deployJson;
    private Map mesos;
    private boolean configChanged;
    private DeploymentStrategy deploymentStrategy;

    private Long snapshotOld;
    private String deploymentOld;
    private String marathonAppIdOld;
    private String commitOld;

    private TempDeployment tempDeployment;

    public AppStage() {
        this.snapshot = new Long(0);
        this.snapshotOld = new Long(0);
        this.deploymentStrategy = new DeploymentStrategy();
        this.tempDeployment = new TempDeployment();
    }

    public String getDeployment() {
        return deployment;
    }

    public void setDeployment(String deployment) {
        this.deployment = deployment;
    }

    public String getExternal() {
        return external;
    }

    public void setExternal(String external) {
        this.external = external;
    }

    public String getInternal() {
        return internal;
    }

    public void setInternal(String internal) {
        this.internal = internal;
    }

    public String getMarathonAppId() {
        return marathonAppId;
    }

    public void setMarathonAppId(String marathonAppId) {
        this.marathonAppId = marathonAppId;
    }

    public int getServicePort() {
        return servicePort;
    }

    public void setServicePort(int servicePort) {
        this.servicePort = servicePort;
    }

    public Map getDeployJson() {
        return deployJson;
    }

    public void setDeployJson(Map deployJson) {
        this.deployJson = deployJson;
    }

    public boolean getConfigChanged() {
        return configChanged;
    }

    public void setConfigChanged(boolean configChanged) {
        this.configChanged = configChanged;
    }

    public DeploymentStrategy getDeploymentStrategy() {
        if (deploymentStrategy == null) {
            deploymentStrategy = new DeploymentStrategy();
        }
        return deploymentStrategy;
    }

    public void setDeploymentStrategy(DeploymentStrategy deploymentStrategy) {
        this.deploymentStrategy = deploymentStrategy;
    }

    public Map getMesos() {
        return mesos;
    }

    public void setMesos(Map mesos) {
        this.mesos = mesos;
    }

    public Long getSnapshot() {
        if (snapshot == null) {
            snapshot = new Long(0);
        }
        return snapshot;
    }

    public void setSnapshot(Long snapshot) {
        this.snapshot = snapshot;
    }

    public Long getSnapshotOld() {
        if (snapshotOld == null) {
            snapshotOld = new Long(0);
        }
        return snapshotOld;
    }

    public void setSnapshotOld(Long snapshotOld) {
        this.snapshotOld = snapshotOld;
    }

    public String getDeploymentOld() {
        return deploymentOld;
    }

    public void setDeploymentOld(String deploymentOld) {
        this.deploymentOld = deploymentOld;
    }

    public String getMarathonAppIdOld() {
        return marathonAppIdOld;
    }

    public void setMarathonAppIdOld(String marathonAppIdOld) {
        this.marathonAppIdOld = marathonAppIdOld;
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

    public TempDeployment getTempDeployment() {
        if (tempDeployment == null) {
            tempDeployment = new TempDeployment();
        }
        return tempDeployment;
    }

    public void setTempDeployment(TempDeployment tempDeployment) {
        this.tempDeployment = tempDeployment;
    }
}
