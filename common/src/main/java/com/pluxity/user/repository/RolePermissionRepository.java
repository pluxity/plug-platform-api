package com.pluxity.user.repository;

import com.pluxity.permission.PermissionGroup;
import com.pluxity.user.entity.Role;
import com.pluxity.user.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    void deleteAllByRole(Role role);

    void deleteAllByPermissionGroup(PermissionGroup permissionGroup);
}
