package com.pluxity.facility.history

import com.pluxity.global.entity.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@EntityListeners(AuditingEntityListener::class)
class FacilityHistory(
    val fileId: Long,
    val facilityId: Long,
    val comment: String,
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    fun requiredId(): Long = checkNotNull(this.id) { "FacilityHistory ID is required" }
}
