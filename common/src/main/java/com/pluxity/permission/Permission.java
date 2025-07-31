package com.pluxity.permission;

import com.pluxity.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "permission")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Permission extends BaseEntity {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private PermissionGroup permissionGroup;

    @Column(nullable = false)
    private String resourceName;

    @Column(nullable = false)
    private String resourceId;

    @Builder
    public Permission(String resourceName, String resourceId, PermissionGroup permissionGroup) {
        this.resourceName = resourceName;
        this.resourceId = resourceId;
        this.permissionGroup = permissionGroup;
    }

    public void changeResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public void changeResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public boolean matches(String resourceName, String resourceId) {
        return this.resourceName.equalsIgnoreCase(resourceName) && this.resourceId.equals(resourceId);
    }

    protected void changePermissionGroup(PermissionGroup permissionGroup) {
        if (this.permissionGroup != null) {
            this.permissionGroup.getPermissions().remove(this);
        }
        this.permissionGroup = permissionGroup;
        if (permissionGroup != null && !permissionGroup.getPermissions().contains(this)) {
            permissionGroup.getPermissions().add(this);
        }
    }

    protected void clearPermissionGroup() {
        this.permissionGroup = null;
    }
}
