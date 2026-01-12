package com.pluxity.facility.path

import com.pluxity.facility.Facility
import com.pluxity.global.entity.IdentityIdEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EntityListeners
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction
import org.springframework.data.jpa.domain.support.AuditingEntityListener

@Entity
@EntityListeners(AuditingEntityListener::class)
class FacilityPath(
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "facility_id")
    @NotFound(action = NotFoundAction.IGNORE)
    var facility: Facility? = null,
    @Column(nullable = false)
    var name: String,
    @Enumerated(EnumType.STRING)
    var pathType: PathType,
    @Column(columnDefinition = "text", nullable = false)
    var path: String,
) : IdentityIdEntity() {
    fun updateName(name: String) {
        this.name = name
    }

    fun updatePathType(pathType: PathType) {
        this.pathType = pathType
    }

    fun updatePath(path: String) {
        this.path = path
    }
}
