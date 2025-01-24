package com.pluxity.user.entity;

import com.pluxity.core.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "permissions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Permission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "permission_id")
    private Long id;

    @Column(name = "description")
    private String description;

    @OneToMany(
            mappedBy = "permission",
            cascade = {CascadeType.PERSIST, CascadeType.MERGE},
            orphanRemoval = true)
    private final List<RolePermission> rolePermissions = new ArrayList<>();

    @Builder
    public Permission(String description) {
        this.description = Objects.requireNonNull(description, "Description must not be null");
    }

    public void updateDescription(String description) {
        this.description = Objects.requireNonNull(description, "Description must not be null");
    }

    // Role 관리 메서드
    public void addRole(Role role) {
        Objects.requireNonNull(role, "Role must not be null");
        RolePermission rolePermission = new RolePermission(role, this);
        this.rolePermissions.add(rolePermission);
    }

    public void removeRole(Role role) {
        Objects.requireNonNull(role, "Role must not be null");
        this.rolePermissions.removeIf(
                rolePermission -> Objects.equals(rolePermission.getRole().getId(), role.getId()));
    }

    // 조회 메서드
    public List<RolePermission> getRolePermissions() {
        return Collections.unmodifiableList(rolePermissions);
    }

    public List<Role> getRoles() {
        return this.rolePermissions.stream().map(RolePermission::getRole).toList();
    }

    public boolean hasRole(Role role) {
        return this.rolePermissions.stream()
                .anyMatch(rolePermission -> Objects.equals(rolePermission.getRole().getId(), role.getId()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permission that)) return false;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
