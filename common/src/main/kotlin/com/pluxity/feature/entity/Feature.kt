package com.pluxity.feature.entity

import com.pluxity.facility.Facility
import com.pluxity.feature.dto.FeatureUpdateRequest
import com.pluxity.global.entity.BaseEntity
import jakarta.persistence.AttributeOverride
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@Table(name = "feature")
@EntityListeners(AuditingEntityListener::class)
class Feature(
    @Id var id: String,
    @AttributeOverride(
        name = "z",
        column = Column(name = "position_z"),
    ) @AttributeOverride(
        name = "y",
        column = Column(name = "position_y"),
    ) @AttributeOverride(
        name = "x",
        column = Column(name = "position_x"),
    ) @Embedded var position: Spatial? = null,
    @AttributeOverride(
        name = "z",
        column = Column(name = "rotation_z"),
    ) @AttributeOverride(
        name = "y",
        column = Column(name = "rotation_y"),
    ) @AttributeOverride(
        name = "x",
        column = Column(name = "rotation_x"),
    ) @Embedded var rotation: Spatial? = null,
    @AttributeOverride(
        name = "z",
        column = Column(name = "scale_z"),
    ) @AttributeOverride(
        name = "y",
        column = Column(name = "scale_y"),
    ) @AttributeOverride(
        name = "x",
        column = Column(name = "scale_x"),
    ) @Embedded var scale: Spatial? = null,
    var assetId: Long? = null,
    @NotFound(action = NotFoundAction.IGNORE) @JoinColumn(name = "facility_id") @ManyToOne(
        fetch = FetchType.LAZY,
    ) var facility: Facility,
    @Column(name = "floor_id") var floorId: String? = null,
) : BaseEntity() {
    fun update(request: FeatureUpdateRequest) {
        if (request.position != null) {
            this.position = request.position
        }
        if (request.rotation != null) {
            this.rotation = request.rotation
        }
        if (request.scale != null) {
            this.scale = request.scale
        }
    }
}
