package org.uengine.cloud.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.uengine.iam.util.JsonUtils;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "app_deploy_json")
public class AppDeployJson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String appName;

    private String stage;

    @JsonIgnore
    @Column(columnDefinition = "TEXT")
    private String jsonString;

    public Map getJson() {
        try {
            return JsonUtils.unmarshal(this.jsonString);
        } catch (Exception ex) {
            return new HashMap();
        }
    }

    public void setJson(Map map) {
        try {
            this.jsonString = JsonUtils.marshal(map);
        } catch (Exception ex) {
            this.jsonString = "{}";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
