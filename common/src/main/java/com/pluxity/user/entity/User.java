package com.pluxity.user.entity;

import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "name", nullable = false, length = 20)
    private String name;

    @Column(name = "code", length = 20)
    private String code;

    @Column(name = "phoneNumber")
    private String phoneNumber;

    @Column(name = "department")
    private String department;

    @OneToMany(
            mappedBy = "user",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true)
    private final Set<UserRole> userRoles = new LinkedHashSet<>();

    @Builder
    public User(
            String username,
            String password,
            String name,
            String code,
            String phoneNumber,
            String department) {
        this.username = Objects.requireNonNull(username, "Username must not be null");
        this.password = Objects.requireNonNull(password, "Password must not be null");
        this.name = Objects.requireNonNull(name, "Name must not be null");
        this.code = Objects.requireNonNull(code, "Code must not be null");
        this.phoneNumber = phoneNumber;
        this.department = department;
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
                    duplicateRoles.stream().map(Role::getName).reduce((a, b) -> a + ", " + b).orElse("");

            throw new IllegalStateException("Some roles already exist for this user: " + duplicateNames);
        }

        roles.forEach(this::addRole);
    }

    public void addRole(Role role) {
        Objects.requireNonNull(role, "Role must not be null");

        if (hasRole(role)) {
            throw new IllegalStateException("Role already exists for this user: " + role.getName());
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
                                () -> new IllegalStateException("Role not found for this user: " + role.getName()));

        this.userRoles.remove(userRoleToRemove);
    }

    public void updateRoles(List<Role> newRoles) {
        Objects.requireNonNull(newRoles, "Roles list must not be null");

        Set<Role> currentRoles = new HashSet<>(getRoles());
        Set<Role> updatedRoles = new HashSet<>(newRoles);

        currentRoles.stream().filter(role -> !updatedRoles.contains(role)).forEach(this::removeRole);

        updatedRoles.stream().filter(role -> !currentRoles.contains(role)).forEach(this::addRole);
    }

    public void clearRoles() {
        this.userRoles.clear();
    }

    public List<Role> getRoles() {
        return userRoles.stream().map(UserRole::getRole).toList();
    }

    public boolean hasRole(Role role) {
        return userRoles.stream().map(UserRole::getRole).anyMatch(r -> r.equals(role));
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeCode(String code) {
        this.code = code;
    }

    public void changePhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void changeDepartment(String department) {
        this.department = department;
    }
}
