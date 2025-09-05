package com.pluxity.facility.path

import com.pluxity.facility.Facility
import com.pluxity.global.entity.BaseEntity
import jakarta.persistence.*
import lombok.AccessLevel
import lombok.Builder
import lombok.Getter
import lombok.NoArgsConstructor
import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener::class)
class FacilityPath @Builder constructor(
    @field:NotFound(action = NotFoundAction.IGNORE) @field:JoinColumn(name = "facility_id") @field:ManyToOne(
        fetch = FetchType.LAZY
    ) private var facility: Facility?, private var name: String?, @field:Enumerated(EnumType.STRING) private var pathType: PathType?, @field:Column(
        columnDefinition = "text"
    ) private var path: String?
) : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null

    fun updateName(name: String?) {
        this.name = name
    }

    fun updatePathType(pathType: PathType?) {
        this.pathType = pathType
    }

    fun updatePath(path: String?) {
        this.path = path
    }
}
