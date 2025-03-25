package com.pluxity.category.entity;

import java.util.List;

public interface Category<T extends Category<T>> {
    Long getId();
    String getName();
    void setName(String name);
    T getParent();
    void changeParent(T parent);
    List<T> getChildren();
    void addChild(T child);
    void removeChild(T child);
    Integer getLevel();
    void setLevel(Integer level);
    String getPath();
}