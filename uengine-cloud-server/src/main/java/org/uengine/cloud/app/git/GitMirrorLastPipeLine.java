package org.uengine.cloud.app.git;

import org.gitlab4j.api.models.Pipeline;

public class GitMirrorLastPipeLine {
    private String appName;

    private Pipeline pipeline;

    private String commit;

    private Long githubRepoId;

    public GitMirrorLastPipeLine() {
    }

    public GitMirrorLastPipeLine(String appName, Pipeline pipeline, String commit, Long githubRepoId) {
        this.appName = appName;
        this.pipeline = pipeline;
        this.commit = commit;
        this.githubRepoId = githubRepoId;
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

    public String getCommit() {
        return commit;
    }

    public void setCommit(String commit) {
        this.commit = commit;
    }

    public Long getGithubRepoId() {
        return githubRepoId;
    }

    public void setGithubRepoId(Long githubRepoId) {
        this.githubRepoId = githubRepoId;
    }
}
