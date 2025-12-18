package com.pluxity.permission

import com.pluxity.global.entity.IdentityIdEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
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
    @ManyToOne(fetch = FetchType.LAZY)
    var permissionGroup: PermissionGroup? = null,
) : IdentityIdEntity() {
    fun matches(
        resourceName: String?,
        resourceId: String?,
    ): Boolean = this.resourceName.equals(resourceName, ignoreCase = true) && this.resourceId == resourceId

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
