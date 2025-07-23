package com.pluxity.user.repository;

import com.pluxity.user.entity.Permission;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByResourceNameAndResourceId(
            @NotNull String resourceType, Long resourceId);
}
