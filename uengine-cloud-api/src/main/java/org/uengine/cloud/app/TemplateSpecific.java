package org.uengine.cloud.app;

import org.uengine.cloud.catalog.FileMapping;

import java.io.Serializable;
import java.util.List;

/**
 * Created by uengine on 2018. 2. 6..
 */
public class TemplateSpecific {

    List<FileMapping> fileMappings;
    Serializable data;

    public List<FileMapping> getFileMappings() {
        return fileMappings;
    }

    public void setFileMappings(List<FileMapping> fileMappings) {
        this.fileMappings = fileMappings;
    }

    public Serializable getData() {
        return data;
    }

    public void setData(Serializable data) {
        this.data = data;
    }
}
