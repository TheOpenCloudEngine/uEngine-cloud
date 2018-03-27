package org.uengine.cloud.debug;

import javax.persistence.*;

@Entity
@Table(name = "app_debug_port")
public class AppDebugPort {

    @Id
    private String marathonAppId;

    private String taskId;

    private int hostPort;

    private int containerPort;

    private int servicePort;

    public String getMarathonAppId() {
        return marathonAppId;
    }

    public void setMarathonAppId(String marathonAppId) {
        this.marathonAppId = marathonAppId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public int getHostPort() {
        return hostPort;
    }

    public void setHostPort(int hostPort) {
        this.hostPort = hostPort;
    }

    public int getContainerPort() {
        return containerPort;
    }

    public void setContainerPort(int containerPort) {
        this.containerPort = containerPort;
    }

    public int getServicePort() {
        return servicePort;
    }

    public void setServicePort(int servicePort) {
        this.servicePort = servicePort;
    }
}
