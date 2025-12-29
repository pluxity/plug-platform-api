package com.pluxity.user.entity

import com.pluxity.permission.ResourceType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "user_resource_permissions")
class UserResourcePermission(
    @Column(name = "user_id", nullable = false)
    val userId: Long,
    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false)
    val resourceType: ResourceType,
    @Column(name = "resource_id", nullable = false)
    val resourceId: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}
