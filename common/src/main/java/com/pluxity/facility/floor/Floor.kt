package com.pluxity.facility.floor

import com.pluxity.facility.Facility
import jakarta.persistence.*
import lombok.AccessLevel
import lombok.Builder
import lombok.Getter
import lombok.NoArgsConstructor
import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction

@Entity
@Table(name = "floor")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class Floor @Builder constructor(
    @field:NotFound(action = NotFoundAction.IGNORE) @field:JoinColumn(name = "facility_id") @field:ManyToOne(
        fetch = FetchType.LAZY
    ) private var facility: Facility?, @field:Column(name = "floor_id", nullable = false) private var floorId: String?, @field:Column(
        name = "name",
        nullable = false
    ) private var name: String?
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private val id: Long? = null

    fun assignParent(facility: Facility?) {
        this.facility = facility
    }
}
