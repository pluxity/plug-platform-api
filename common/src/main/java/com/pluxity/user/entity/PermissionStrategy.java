package com.pluxity.user.entity;

public interface PermissionStrategy {
    boolean check(User user, Object resource);
}
