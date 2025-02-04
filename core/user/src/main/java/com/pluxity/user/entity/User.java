package com.pluxity.user.entity;

import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "name", nullable = false, length = 10)
    private String name;

    @Column(name = "code", nullable = false)
    private String code;

    @OneToMany(
            mappedBy = "user",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true)
    private final List<UserRole> userRoles = new ArrayList<>();

    @Builder
    public User(String username, String password, String name, String code) {
        this.username = Objects.requireNonNull(username, "Username must not be null");
        this.password = Objects.requireNonNull(password, "Password must not be null");
        this.name = Objects.requireNonNull(name, "Name must not be null");
        this.code = Objects.requireNonNull(code, "Code must not be null");
    }

    public void changeUsername(String username) {
        this.username = Objects.requireNonNull(username, "Username must not be null");
    }

    public void changePassword(String password) {
        this.password = Objects.requireNonNull(password, "Password must not be null");
    }

    public void addRoles(List<Role> roles) {
        Objects.requireNonNull(roles, "Roles list must not be null");

        List<Role> duplicateRoles = roles.stream().filter(this::hasRole).toList();

        if (!duplicateRoles.isEmpty()) {
            String duplicateNames =
                    duplicateRoles.stream().map(Role::getRoleName).reduce((a, b) -> a + ", " + b).orElse("");

            throw new IllegalStateException("Some roles already exist for this user: " + duplicateNames);
        }

        roles.forEach(this::addRole);
    }

    public void addRole(Role role) {
        Objects.requireNonNull(role, "Role must not be null");

        if (hasRole(role)) {
            throw new IllegalStateException("Role already exists for this user: " + role.getRoleName());
        }

        UserRole userRole = new UserRole(this, role);
        this.userRoles.add(userRole);
    }

    public void removeRole(Role role) {
        Objects.requireNonNull(role, "Role must not be null");

        UserRole userRoleToRemove =
                this.userRoles.stream()
                        .filter(ur -> ur.getRole().equals(role))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Role not found for this user: " + role.getRoleName()));

        this.userRoles.remove(userRoleToRemove);
    }

    public void clearRoles() {
        this.userRoles.clear();
    }

    public List<Role> getRoles() {
        return userRoles.stream().map(UserRole::getRole).toList();
    }

    public List<Permission> getPermissions() {
        return this.getRoles().stream()
                .flatMap(role -> role.getRolePermissions().stream())
                .map(RolePermission::getPermission)
                .distinct()
                .toList();
    }

    public boolean hasRole(Role role) {
        return userRoles.stream().map(UserRole::getRole).anyMatch(r -> r.equals(role));
    }

    public boolean hasPermission(Permission permission) {
        return getPermissions().stream().anyMatch(p -> Objects.equals(p.getId(), permission.getId()));
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeCode(String code) {
        this.code = code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(getId(), user.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
