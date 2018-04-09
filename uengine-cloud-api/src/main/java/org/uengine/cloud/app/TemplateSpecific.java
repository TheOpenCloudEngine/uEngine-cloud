package org.uengine.cloud.app;

import org.uengine.cloud.catalog.FileMapping;

import java.util.List;
import java.util.Map;

/**
 * Created by uengine on 2018. 2. 6..
 */
public class TemplateSpecific {

    List<FileMapping> fileMappings;

    Map<String, Object> data;

    public List<FileMapping> getFileMappings() {
        return fileMappings;
    }

    public void setFileMappings(List<FileMapping> fileMappings) {
        this.fileMappings = fileMappings;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }


}
