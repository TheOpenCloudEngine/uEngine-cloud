package org.uengine.zuul.model;

import org.uengine.zuul.BlueGreen;

/**
 * Created by uengine on 2017. 10. 30..
 */
public class DcosState {
    private String marathonAppId;
    private String external;
    private String internal;
    private BlueGreen deployment;

    public String getMarathonAppId() {
        return marathonAppId;
    }

    public void setMarathonAppId(String marathonAppId) {
        this.marathonAppId = marathonAppId;
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

    public BlueGreen getDeployment() {
        return deployment;
    }

    public void setDeployment(BlueGreen deployment) {
        this.deployment = deployment;
    }
}
