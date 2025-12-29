package com.pluxity.permission

import com.pluxity.global.entity.IdentityIdEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "permission")
class Permission(
    @Column(nullable = false)
    var resourceName: String,
    @Column(nullable = false)
    var resourceId: String,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var level: PermissionLevel = PermissionLevel.READ,
    @ManyToOne(fetch = FetchType.LAZY)
    var permissionGroup: PermissionGroup? = null,
) : IdentityIdEntity() {
    fun allows(
        resourceName: String,
        resourceId: String,
        requiredLevel: PermissionLevel,
    ): Boolean =
        this.resourceName.equals(resourceName, ignoreCase = true) &&
            this.resourceId == resourceId &&
            this.level.allows(requiredLevel)

    fun changePermissionGroup(permissionGroup: PermissionGroup?) {
        this.permissionGroup?.permissions?.remove(this)
        this.permissionGroup = permissionGroup
        permissionGroup?.permissions?.let { permissions ->
            if (!permissions.contains(this)) {
                permissions.add(this)
            }
        }
    }

    fun clearPermissionGroup() {
        this.permissionGroup = null
    }
}
