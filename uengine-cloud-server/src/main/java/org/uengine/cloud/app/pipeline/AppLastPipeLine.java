package org.uengine.cloud.app.pipeline;

import org.gitlab4j.api.models.Pipeline;

public class AppLastPipeLine {
    private String appName;

    private Pipeline pipeline;

    public AppLastPipeLine() {
    }

    public AppLastPipeLine(String appName, Pipeline pipeline) {
        this.appName = appName;
        this.pipeline = pipeline;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Pipeline getPipeline() {
        return pipeline;
    }

    public void setPipeline(Pipeline pipeline) {
        this.pipeline = pipeline;
    }
}
