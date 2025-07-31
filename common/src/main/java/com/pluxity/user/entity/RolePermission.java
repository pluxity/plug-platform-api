package com.pluxity.user.entity;

import com.pluxity.permission.PermissionGroup;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "role_permission")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RolePermission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "permission_set_id")
    private PermissionGroup permissionGroup;

    @Builder
    public RolePermission(Role role, PermissionGroup permissionGroup) {
        this.role = role;
        this.permissionGroup = permissionGroup;
    }

    public void changeRole(Role role) {
        this.role = role;
    }
}
