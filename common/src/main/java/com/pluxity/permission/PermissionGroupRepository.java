package com.pluxity.permission;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionGroupRepository extends JpaRepository<PermissionGroup, Long> {
    boolean existsByName(String permissionGroupName);

    boolean existsByNameAndIdNot(String newName, Long id);
}
