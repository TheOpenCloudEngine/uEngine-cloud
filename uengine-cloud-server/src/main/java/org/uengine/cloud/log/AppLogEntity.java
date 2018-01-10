package org.uengine.cloud.log;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.uengine.iam.util.JsonUtils;

import javax.persistence.*;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Entity
public class AppLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String owner;

    private String updater;

    private String appName;

    @Enumerated(EnumType.STRING)
    private AppLogAction action;

    @Enumerated(EnumType.STRING)
    private AppLogStatus status;

    @JsonIgnore
    @Column(columnDefinition = "TEXT")
    private String logString;

    @Column(name = "regDate", nullable = false, updatable = false, insertable = true)
    private long regDate;

    public Map getLog() {
        try {
            return JsonUtils.unmarshal(this.logString);
        } catch (IOException ex) {
            return new HashMap();
        }
    }

    public void setLog(Map logMap) {
        try {
            this.logString = JsonUtils.marshal(logMap);
        } catch (IOException ex) {
            this.logString = "{}";
        }
    }

    @PrePersist
    void preInsert() {
        this.regDate = new Date().getTime();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getUpdater() {
        return updater;
    }

    public void setUpdater(String updater) {
        this.updater = updater;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getLogString() {
        return logString;
    }

    public void setLogString(String logString) {
        this.logString = logString;
    }

    public long getRegDate() {
        return regDate;
    }

    public void setRegDate(long regDate) {
        this.regDate = regDate;
    }

    public AppLogStatus getStatus() {
        return status;
    }

    public void setStatus(AppLogStatus status) {
        this.status = status;
    }

    public AppLogAction getAction() {
        return action;
    }

    public void setAction(AppLogAction action) {
        this.action = action;
    }
}
