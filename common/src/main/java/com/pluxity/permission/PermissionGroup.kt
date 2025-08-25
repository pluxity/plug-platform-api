package com.pluxity.permission

import com.pluxity.global.entity.BaseEntity
import com.pluxity.user.entity.RolePermission
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "permission_group")
class PermissionGroup(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    var name: String,
    var description: String?,
) : BaseEntity() {
    @OneToMany(mappedBy = "permissionGroup", cascade = [CascadeType.ALL])
    val rolePermissions: MutableSet<RolePermission> = HashSet()

    @OneToMany(mappedBy = "permissionGroup", cascade = [CascadeType.ALL])
    val permissions: MutableSet<Permission> = HashSet()

    fun changeName(name: String) {
        this.name = name
    }

    fun changeDescription(description: String) {
        this.description = description
    }

    fun addPermission(permission: Permission) {
        if (!permissions.contains(permission)) {
            permissions.add(permission)
            if (permission.permissionGroup != this) {
                permission.changePermissionGroup(this)
            }
        }
    }

    fun removePermission(permission: Permission) {
        if (permissions.contains(permission)) {
            permissions.remove(permission)
            if (permission.permissionGroup == this) {
                permission.clearPermissionGroup()
            }
        }
    }
}
