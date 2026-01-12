package com.pluxity.facility.floor

import com.pluxity.facility.Facility
import com.pluxity.global.entity.IdentityIdEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction

@Entity
@Table(name = "floor")
class Floor(
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "facility_id")
    @NotFound(action = NotFoundAction.IGNORE)
    var facility: Facility? = null,
    @Column(name = "floor_id", nullable = false)
    val floorId: String,
    @Column(name = "name", nullable = false)
    val name: String,
) : IdentityIdEntity()
