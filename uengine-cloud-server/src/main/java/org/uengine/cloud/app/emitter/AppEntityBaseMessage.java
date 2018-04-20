package org.uengine.cloud.app.emitter;

import org.uengine.cloud.app.AppEntity;

public class AppEntityBaseMessage {
    private AppEntityBaseMessageTopic topic;
    private AppEntity appEntity;
    private String stage;
    private Object body;

    public AppEntityBaseMessageTopic getTopic() {
        return topic;
    }

    public void setTopic(AppEntityBaseMessageTopic topic) {
        this.topic = topic;
    }

    public AppEntity getAppEntity() {
        return appEntity;
    }

    public void setAppEntity(AppEntity appEntity) {
        this.appEntity = appEntity;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public String getAppName() {
        return this.appEntity == null ? null : this.appEntity.getName();
    }
}
