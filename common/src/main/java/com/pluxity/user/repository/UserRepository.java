package com.pluxity.user.repository;

import com.pluxity.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(
            attributePaths = {
                "userRoles",
                "userRoles.role",
            })
    @NonNull
    List<User> findAll();

    @EntityGraph(
            attributePaths = {
                "userRoles",
                "userRoles.role",
            })
    @NonNull
    Optional<User> findById(@NonNull Long id);

    @EntityGraph(
            attributePaths = {
                "userRoles",
                "userRoles.role",
            })
    Optional<User> findByUsername(@NonNull String username);

    boolean existsByUsername(@NonNull String username);
}
