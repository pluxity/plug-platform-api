package com.pluxity.user.entity;

import com.pluxity.permission.ResourceType;

public interface Permissible {
    String getResourceId();

    ResourceType getResourceType();
}
