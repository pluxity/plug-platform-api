package com.pluxity.global.annotation;

import com.pluxity.user.entity.ResourceType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface CheckPermissionCategory {
    ResourceType categoryResourceType();
}
