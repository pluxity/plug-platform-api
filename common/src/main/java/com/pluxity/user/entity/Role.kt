package com.pluxity.user.entity

import com.pluxity.global.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "roles")
class Role(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long? = null,
    @Column(name = "name", nullable = false, unique = true)
    var name: String,
    @Column(name = "description", length = 100)
    var description: String?,
) : BaseEntity() {
    var auth: String? = "USER"

    @OneToMany(mappedBy = "role")
    var userRoles: List<UserRole> = mutableListOf()

    @OneToMany(mappedBy = "role")
    var rolePermissions: MutableSet<RolePermission> = mutableSetOf()

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
    ): Boolean =
        rolePermissions!!
            .asSequence()
            .map { it.permissionGroup }
            .flatMap { it.permissions.asSequence() }
            .any { it.matches(resourceName, resourceId) }

    fun addRolePermission(rolePermission: RolePermission) {
        rolePermissions.add(rolePermission)
    }

    fun removeRolePermission(rolePermission: RolePermission) {
        rolePermissions.remove(rolePermission)
    }
}
