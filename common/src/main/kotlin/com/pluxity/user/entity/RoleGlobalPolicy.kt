package com.pluxity.user.entity

import com.pluxity.global.entity.IdentityIdEntity
import com.pluxity.permission.ResourceType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "role_global_policies")
class RoleGlobalPolicy(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    var role: Role,
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false)
    var resourceType: ResourceType,
    @Enumerated(EnumType.STRING)
    @Column(name = "permission_type", nullable = false)
    var permissionType: RoleGlobalPermissionType,
) : IdentityIdEntity()
