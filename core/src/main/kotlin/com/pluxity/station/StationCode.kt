package com.pluxity.station

import com.pluxity.global.entity.IdentityIdEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction

@Entity
class StationCode(
    @JoinColumn(name = "station_id")
    @ManyToOne(fetch = FetchType.EAGER)
    @NotFound(action = NotFoundAction.IGNORE)
    var station: Station,
    var code: String,
) : IdentityIdEntity()
