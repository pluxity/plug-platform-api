package com.pluxity.permission;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByResourceNameAndResourceId(String resourceName, String resourceId);

    boolean existsByResourceNameAndResourceId(String resourceName, String resourceId);

    List<Permission> findByResourceNameAndResourceIdIn(String resourceName, List<String> resourceIds);
}
