package com.pluxity.user.repository;

import com.pluxity.user.entity.Role;
import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);

    @Override
    @EntityGraph(attributePaths = {"rolePermissions", "rolePermissions.permission"})
    @Nonnull
    Optional<Role> findById(@Nonnull Long id);

    @Override
    @EntityGraph(attributePaths = {"rolePermissions", "rolePermissions.permission"})
    @Nonnull
    List<Role> findAll();
}
