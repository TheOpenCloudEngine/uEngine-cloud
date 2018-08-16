package org.uengine.cloud.catalog;

import java.io.Serializable;

/**
 * Created by uengine on 2018. 1. 15..
 */
public class FileMapping {
    private String path;
    private String file;

    private Object data;

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

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
