package com.pluxity.user.repository;

import com.pluxity.user.entity.Role;
import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

    @EntityGraph(
            attributePaths = {
                "userRoles.user",
                "userRoles.role",
                "rolePermissions.permissionGroup.permissions"
            })
    @Override
    @Nonnull
    Optional<Role> findById(@Nonnull Long id);

    @EntityGraph(
            attributePaths = {
                "userRoles.user",
                "userRoles.role",
                "rolePermissions.permissionGroup.permissions"
            })
    @Override
    @Nonnull
    List<Role> findAll();
}
