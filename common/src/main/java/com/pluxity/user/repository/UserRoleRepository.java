package com.pluxity.user.repository;

import com.pluxity.user.entity.Role;
import com.pluxity.user.entity.User;
import com.pluxity.user.entity.UserRole;
import io.lettuce.core.dynamic.annotation.Param;
import jakarta.annotation.Nonnull;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    void deleteAllByUser(User user);

    @Modifying
    @Query("DELETE FROM UserRole ur WHERE ur.role = :role")
    void deleteAllByRole(@Param("role") Role role);

    @EntityGraph(attributePaths = {"user", "role.rolePermissions"})
    @Nonnull
    List<UserRole> findAll();
}
