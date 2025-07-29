package com.pluxity.user.entity;

public interface CategorizedPermissible extends Permissible {
    String getCategoryResourceId();

    ResourceType getCategoryResourceType();
}
