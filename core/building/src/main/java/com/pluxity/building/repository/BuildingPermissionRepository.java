package com.pluxity.building.repository;

import com.pluxity.building.entity.BuildingPermission;
import com.pluxity.permission.repository.PermissionRepository;
import com.pluxity.user.entity.Role;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BuildingPermissionRepository extends PermissionRepository<BuildingPermission> {
    
    @Query("""
            SELECT bp 
            FROM BuildingPermission bp 
            WHERE bp.role = :role 
            AND bp.resourceId = :buildingId
            """)
    List<BuildingPermission> findByRoleAndBuildingId(@Param("role") Role role, @Param("buildingId") Long buildingId);
    
    Optional<BuildingPermission> findByRoleAndResourceId(Role role, Long buildingId);
} 