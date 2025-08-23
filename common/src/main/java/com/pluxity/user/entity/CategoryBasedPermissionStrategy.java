package com.pluxity.user.entity;

import com.pluxity.global.annotation.ResolvePermission;
import java.util.Optional;

@ResolvePermission(PermissionType.CATEGORY)
public class CategoryBasedPermissionStrategy implements PermissionStrategy {
    @Override
    public boolean check(User user, Object resource) {
        return Optional.ofNullable(resource)
                .filter(r -> r instanceof Permissible)
                .map(r -> (Permissible) r)
                .map(p -> user.canAccess(p.getResourceType().getResourceName(), p.getResourceId()))
                .orElse(false);
    }
}
