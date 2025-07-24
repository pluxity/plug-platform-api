package com.pluxity.user.repository;

import com.pluxity.user.entity.Permission;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByResourceNameAndResourceId(String resourceName, String resourceId);

    boolean existsByResourceNameAndResourceId(String resourceName, String resourceId);
}
