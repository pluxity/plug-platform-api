package com.pluxity.permission;

import com.pluxity.global.entity.BaseEntity;
import com.pluxity.user.entity.RolePermission;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "permission_group")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PermissionGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    @OneToMany(mappedBy = "permissionGroup", cascade = CascadeType.ALL)
    private final Set<RolePermission> rolePermissions = new HashSet<>();

    @OneToMany(mappedBy = "permissionGroup", cascade = CascadeType.ALL)
    private final Set<Permission> permissions = new HashSet<>();

    @Builder
    public PermissionGroup(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    public void addPermission(Permission permission) {
        if (permission != null && !this.permissions.contains(permission)) {
            this.permissions.add(permission);
            if (permission.getPermissionGroup() != this) {
                permission.changePermissionGroup(this);
            }
        }
    }

    public void removePermission(Permission permission) {
        if (permission != null && this.permissions.contains(permission)) {
            this.permissions.remove(permission);
            if (permission.getPermissionGroup() == this) {
                permission.clearPermissionGroup();
            }
        }
    }

    public void clearPermissions() {
        new HashSet<>(this.permissions).forEach(this::removePermission);
    }
}
