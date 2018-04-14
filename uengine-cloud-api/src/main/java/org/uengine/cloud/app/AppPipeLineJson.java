package org.uengine.cloud.app;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.uengine.iam.util.JsonUtils;

import javax.persistence.*;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "app_pipeline_json")
public class AppPipeLineJson {

    @Id
    private String appName;

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

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }
}
