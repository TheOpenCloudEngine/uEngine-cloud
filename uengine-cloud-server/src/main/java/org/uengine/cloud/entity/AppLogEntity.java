package org.uengine.cloud.entity;


import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.GeneratorType;

import javax.persistence.*;
import java.util.Date;

@Entity
public class AppLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long appLogId;

    String ownerName;

    String updateUserName;

    String appName;

    @Column(length=100000)
    String appInfo;

    @Column(name = "updateDate", nullable = false, updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    Date updateDate;

    public Long getAppLogId() {
        return appLogId;
    }

    public void setAppLogId(Long appLogId) {
        this.appLogId = appLogId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getUpdateUserName() {
        return updateUserName;
    }

    public void setUpdateUserName(String updateUser) {
        this.updateUserName = updateUser;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public String getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(String appInfo) {
        this.appInfo = appInfo;
    }
}
