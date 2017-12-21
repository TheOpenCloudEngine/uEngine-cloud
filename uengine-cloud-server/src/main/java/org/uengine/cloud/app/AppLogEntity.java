package org.uengine.cloud.app;

import org.springframework.stereotype.Component;
import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by uengine on 2017. 8. 1..
 */
@Entity
public class AppLogEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long appLogId;



    String logData;

    String info;

    String serviceType;

    @Temporal(TemporalType.TIMESTAMP)
    Date regDate;

    @Temporal(TemporalType.TIMESTAMP)
    Date updateDate;

    public Long getAppLogId() { return appLogId; }

    public void setAppLogId(Long appLogId) { this.appLogId = appLogId; }

    public String getLogData() { return logData; }

    public void setLogData(String logData) { this.logData = logData; }

    public String getInfo() { return info; }

    public void setInfo(String info) { this.info = info; }

    public String getServiceType() { return serviceType; }

    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public Date getRegDate() { return regDate; }

    public void setRegDate(Date regDate) { this.regDate = regDate; }

    public Date getUpdateDate() { return updateDate; }

    public void setUpdateDate(Date updateDate) { this.updateDate = updateDate; }
}