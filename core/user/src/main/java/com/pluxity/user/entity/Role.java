package com.pluxity.user.entity;

import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;

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
    private final Set<RolePermission> rolePermissions = new LinkedHashSet<>();

    @Builder
    public Role(String roleName) {
        this.roleName = Objects.requireNonNull(roleName, "Role name must not be null");
    }

    public void changeRoleName(String roleName) {
        this.roleName = Objects.requireNonNull(roleName, "Role name must not be blank");
    }

    // RolePermission 연관관계 편의 메서드
    public void addPermission(Permission permission) {
        Objects.requireNonNull(permission, "Permission must not be null");

        if (hasPermission(permission)) {
            throw new IllegalStateException("Permission already exists for this role: " + permission.getDescription());
        }

        RolePermission rolePermission = new RolePermission(this, permission);
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
                                                "Permission not found for this role: " + permission.getDescription()));

        this.rolePermissions.remove(rolePermissionToRemove);
    }

    public void clearPermissions() {
        this.rolePermissions.clear();
    }

    public boolean hasPermission(Permission permission) {
        return rolePermissions.stream()
                .map(RolePermission::getPermission)
                .anyMatch(p -> Objects.equals(p, permission));
    }

    public List<RolePermission> getRolePermissions() {
        return Collections.unmodifiableList(new ArrayList<>(rolePermissions));
    }

    public List<Permission> getPermissions() {
        return rolePermissions.stream().map(RolePermission::getPermission).toList();
    }

}
