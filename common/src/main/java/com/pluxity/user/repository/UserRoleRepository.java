package com.pluxity.user.repository;

import com.pluxity.user.entity.Role;
import com.pluxity.user.entity.User;
import com.pluxity.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
    void deleteAllByUser(User user);

    void deleteAllByRole(Role role);
}
