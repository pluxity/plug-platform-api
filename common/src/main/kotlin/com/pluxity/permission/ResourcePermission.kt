package com.pluxity.permission

import com.pluxity.global.entity.IdentityIdEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "resource_permission",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_resource_permission_resource",
            columnNames = ["permission_id", "resource_name", "resource_id"],
        ),
    ],
)
class ResourcePermission(
    @Column(nullable = false)
    var resourceName: String,
    @Column(nullable = false)
    var resourceId: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var level: PermissionLevel = PermissionLevel.READ,
    @ManyToOne(fetch = FetchType.LAZY)
    var permission: Permission? = null,
) : IdentityIdEntity() {
    fun allows(
        resourceName: String,
        resourceId: String,
        requiredLevel: PermissionLevel,
    ): Boolean =
        this.resourceName.equals(resourceName, ignoreCase = true) &&
            this.resourceId == resourceId &&
            this.level.allows(requiredLevel)

    fun changePermission(permission: Permission?) {
        this.permission?.resourcePermissions?.remove(this)
        this.permission = permission
        permission?.resourcePermissions?.let { permissions ->
            if (!permissions.contains(this)) {
                permissions.add(this)
            }
        }
    }

    fun clearPermission() {
        this.permission = null
    }
}
