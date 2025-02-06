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

    public void changeDescription(String description) {
        this.description = Objects.requireNonNull(description, "Description must not be null");
    }

    public List<RolePermission> getRolePermissions() {
        return Collections.unmodifiableList(rolePermissions);
    }

    public List<Role> getRoles() {
        return this.rolePermissions.stream().map(RolePermission::getRole).toList();
    }

    public boolean hasRole(Role role) {
        return this.rolePermissions.stream()
                .anyMatch(rolePermission -> Objects.equals(rolePermission.getRole(), role));
    }
}
