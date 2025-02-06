package com.pluxity.user.repository;

import com.pluxity.user.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = {"template", "userRoles", "userRoles.role", "userRoles.role.rolePermissions", "userRoles.role.rolePermissions.permission"})
    @NonNull
    List<User> findAll();

    @EntityGraph(attributePaths = {"template", "userRoles", "userRoles.role", "userRoles.role.rolePermissions", "userRoles.role.rolePermissions.permission"})
    @NonNull
    Optional<User> findById(@NonNull Long id);

    @EntityGraph(attributePaths = {"template", "userRoles", "userRoles.role", "userRoles.role.rolePermissions", "userRoles.role.rolePermissions.permission"})
    Optional<User> findByUsername(@NonNull String username);
}
