package com.example.electronicsgadgets.models;

import java.io.Serializable;

public class Category implements Serializable {
    private String id;
    private String name;
    private int iconResId;
    private String imageUrl;
    private boolean isSelected;

    public Category() {
    }

    public Category(String id, String name, int iconResId) {
        this.id = id;
        this.name = name;
        this.iconResId = iconResId;
        this.isSelected = false;
    }

    public Category(String id, String name, int iconResId, String imageUrl) {
        this.id = id;
        this.name = name;
        this.iconResId = iconResId;
        this.imageUrl = imageUrl;
        this.isSelected = false;
    }

    public String getId() {
        return id;
    }

    public String getCategoryId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
