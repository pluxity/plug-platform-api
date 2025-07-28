package com.pluxity.user.repository;

import com.pluxity.user.entity.Permission;
import com.pluxity.user.entity.Role;
import com.pluxity.user.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    void deleteAllByRole(Role role);

    void deleteAllByPermission(Permission permission);
}
