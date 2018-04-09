package org.uengine.cloud.catalog;

import java.util.Map;

/**
 * Created by uengine on 2018. 1. 15..
 */
public class FileMapping {
    private String path;
    private String file;

    private Map<String, Object> data;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }



}
