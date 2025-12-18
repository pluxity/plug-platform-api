package com.pluxity.facility.history

import com.pluxity.global.entity.IdentityIdEntity
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@EntityListeners(AuditingEntityListener::class)
class FacilityHistory(
    val fileId: Long,
    val facilityId: Long,
    val comment: String,
) : IdentityIdEntity()
