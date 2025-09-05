package com.pluxity.permission

import com.pluxity.global.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "permission")
class Permission(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false)
    var resourceName: String,
    @Column(nullable = false)
    var resourceId: String,
    @ManyToOne(fetch = FetchType.LAZY)
    var permissionGroup: PermissionGroup? = null,
) : BaseEntity() {
    fun changeResourceName(resourceName: String) {
        this.resourceName = resourceName
    }

    fun changeResourceId(resourceId: String) {
        this.resourceId = resourceId
    }

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
