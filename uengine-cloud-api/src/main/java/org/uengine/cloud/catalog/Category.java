package org.uengine.cloud.catalog;

import java.util.List;

/**
 * Created by uengine on 2018. 1. 15..
 */
public class Category {
    private String id;
    private String type;
    private String description;
    private List<CategoryItem> items;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<CategoryItem> getItems() {
        return items;
    }

    public void setItems(List<CategoryItem> items) {
        this.items = items;
    }
}
