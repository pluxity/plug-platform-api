package com.pluxity.user.entity;

import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Role extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Long id;

    @Column(name = "role_name", nullable = false, unique = true)
    private String roleName;

    @OneToMany(
            mappedBy = "role",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true)
    private final List<RolePermission> rolePermissions = new ArrayList<>();

    @Builder
    public Role(String roleName) {
        this.roleName = Objects.requireNonNull(roleName, "Role name must not be blank");
    }

    public void changeRoleName(String roleName) {
        this.roleName = Objects.requireNonNull(roleName, "Role name must not be blank");
    }

    // RolePermission 연관관계 편의 메서드
    public void addPermission(Permission permission) {
        Objects.requireNonNull(permission, "Permission must not be null");

        // 이미 해당 권한이 있는지 확인
        if (hasPermission(permission)) {
            throw new IllegalStateException(
                    "Permission already exists in this role: " + permission.getDescription());
        }

        RolePermission rolePermission = new RolePermission(this, permission);
        rolePermission.changeRole(this);
        rolePermission.changePermission(permission);
        this.rolePermissions.add(rolePermission);
    }

    public void addPermissions(List<Permission> permissions) {
        Objects.requireNonNull(permissions, "Permissions list must not be null");

        // 중복 권한 체크
        List<Permission> duplicatePermissions =
                permissions.stream().filter(this::hasPermission).toList();

        if (!duplicatePermissions.isEmpty()) {
            String duplicateDescriptions =
                    duplicatePermissions.stream()
                            .map(Permission::getDescription)
                            .reduce((a, b) -> a + ", " + b)
                            .orElse("");

            throw new IllegalStateException(
                    "Some permissions already exist in this role: " + duplicateDescriptions);
        }

        permissions.forEach(this::addPermission);
    }

    public void removePermission(Permission permission) {
        Objects.requireNonNull(permission, "Permission must not be null");

        RolePermission rolePermissionToRemove =
                this.rolePermissions.stream()
                        .filter(rp -> rp.getPermission().equals(permission))
                        .findFirst()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Permission not found in this role: " + permission.getDescription()));

        this.rolePermissions.remove(rolePermissionToRemove);
    }

    public void clearPermissions() {
        this.rolePermissions.clear();
    }

    public boolean hasPermission(Permission permission) {
        return this.rolePermissions.stream()
                .map(RolePermission::getPermission)
                .anyMatch(p -> p.equals(permission));
    }

    public List<RolePermission> getRolePermissions() {
        return Collections.unmodifiableList(rolePermissions);
    }

    public List<Permission> getPermissions() {
        return this.rolePermissions.stream().map(RolePermission::getPermission).toList();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role role)) return false;
        return Objects.equals(getId(), role.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
