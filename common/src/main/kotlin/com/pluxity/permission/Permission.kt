package com.pluxity.permission

import com.pluxity.global.entity.IdentityIdEntity
import com.pluxity.user.entity.RolePermission
import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "permission")
class Permission(
    var name: String,
    var description: String?,
) : IdentityIdEntity() {
    @OneToMany(mappedBy = "permission", cascade = [CascadeType.ALL])
    val rolePermissions: MutableSet<RolePermission> = HashSet()

    @OneToMany(mappedBy = "permission", cascade = [CascadeType.ALL])
    val resourcePermissions: MutableSet<ResourcePermission> = HashSet()

    @OneToMany(mappedBy = "permission", cascade = [CascadeType.ALL])
    val domainPermissions: MutableSet<DomainPermission> = HashSet()

    fun changeName(name: String) {
        this.name = name
    }

    fun changeDescription(description: String) {
        this.description = description
    }

    fun addResourcePermission(permission: ResourcePermission) {
        if (!resourcePermissions.contains(permission)) {
            resourcePermissions.add(permission)
            if (permission.permission != this) {
                permission.changePermission(this)
            }
        }
    }

    fun removeResourcePermission(permission: ResourcePermission) {
        if (resourcePermissions.contains(permission)) {
            resourcePermissions.remove(permission)
            if (permission.permission == this) {
                permission.clearPermission()
            }
        }
    }

    fun addDomainPermission(permission: DomainPermission) {
        if (!domainPermissions.contains(permission)) {
            domainPermissions.add(permission)
            if (permission.permission != this) {
                permission.changePermission(this)
            }
        }
    }

    fun removeDomainPermission(permission: DomainPermission) {
        if (domainPermissions.contains(permission)) {
            domainPermissions.remove(permission)
            if (permission.permission == this) {
                permission.clearPermission()
            }
        }
    }
}
