package com.pluxity.user.repository;

import com.pluxity.user.entity.ResourcePermission;
import com.pluxity.user.entity.Role;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ResourcePermissionRepository extends JpaRepository<ResourcePermission, Long> {
    boolean existsByRoleAndResourceNameAndResourceId(Role role, String resourceName, Long resourceId);

    void deleteByRoleAndResourceNameAndResourceId(Role role, String resourceName, Long resourceId);

    @Query(
            "SELECT p.resourceId FROM ResourcePermission p WHERE p.role = :role AND p.resourceName = :resourceName")
    List<Long> findResourceIdsByRoleAndResourceName(
            @Param("role") Role role, @Param("resourceName") String resourceName);

    @Modifying
    @Query(
            "DELETE FROM ResourcePermission p WHERE p.role = :role AND p.resourceName = :resourceName AND p.resourceId IN :resourceIds")
    void deleteByRoleAndResourceNameAndResourceIdIn(
            @Param("role") Role role,
            @Param("resourceName") String resourceName,
            @Param("resourceIds") List<Long> resourceIds);
}
