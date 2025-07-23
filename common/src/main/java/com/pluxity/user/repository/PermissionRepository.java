package com.pluxity.user.repository;

import com.pluxity.user.entity.Permission;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByResourceNameAndResourceId(String resourceType, String resourceId);
}
