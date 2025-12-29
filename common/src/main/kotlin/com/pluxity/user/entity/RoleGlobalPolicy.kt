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
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "role_global_policies",
    uniqueConstraints = [UniqueConstraint(columnNames = ["role_id", "resource_type"])],
)
class RoleGlobalPolicy(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    var role: Role,
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", length = 50, nullable = false)
    var resourceType: ResourceType,
) : IdentityIdEntity()
