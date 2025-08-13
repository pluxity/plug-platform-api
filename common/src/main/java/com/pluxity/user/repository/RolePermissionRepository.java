package com.pluxity.user.repository;

import com.pluxity.permission.PermissionGroup;
import com.pluxity.user.entity.Role;
import com.pluxity.user.entity.RolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    void deleteAllByPermissionGroup(PermissionGroup permissionGroup);

    @Modifying
    @Query("DELETE FROM RolePermission rp WHERE rp.role = :role")
    void deleteAllByRole(@Param("role") Role role);
}
