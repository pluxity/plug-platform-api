package com.pluxity.facility.history

import com.pluxity.global.entity.BaseEntity
import jakarta.persistence.*
import lombok.AccessLevel
import lombok.Builder
import lombok.Getter
import lombok.NoArgsConstructor
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener::class)
class FacilityHistory @Builder constructor(private var fileId: Long?, private var facilityId: Long?, private var comment: String?) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null
}
