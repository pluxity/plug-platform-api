package com.pluxity.user.entity

import com.pluxity.global.entity.IdentityIdEntity
import com.pluxity.permission.PermissionLevel
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "roles")
class Role(
    @Column(name = "name", nullable = false, unique = true)
    var name: String,
    @Column(name = "description", length = 100)
    var description: String?,
    var auth: String? = RoleType.USER.name,
) : IdentityIdEntity() {
    @OneToMany(mappedBy = "role")
    var userRoles: MutableList<UserRole> = mutableListOf()

    @OneToMany(mappedBy = "role")
    var rolePermissions: MutableSet<RolePermission> = mutableSetOf()

    @OneToMany(mappedBy = "role")
    var roleGlobalPolicies: MutableSet<RoleGlobalPolicy> = mutableSetOf()

    fun getAuthority(): String = "ROLE_$auth"

    fun changeRoleName(name: String) {
        this.name = name
    }

    fun changeDescription(description: String?) {
        this.description = description
    }

    fun hasPermissionFor(
        resourceName: String,
        resourceId: String,
        requiredLevel: PermissionLevel,
    ): Boolean =
        rolePermissions
            .asSequence()
            .map { it.permissionGroup }
            .flatMap { it.permissions.asSequence() }
            .any { it.allows(resourceName, resourceId, requiredLevel) }

    fun addRolePermission(rolePermission: RolePermission) {
        rolePermissions.add(rolePermission)
    }

    fun removeRolePermission(rolePermission: RolePermission) {
        rolePermissions.remove(rolePermission)
    }
}
