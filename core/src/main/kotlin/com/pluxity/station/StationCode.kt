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
    @NotFound(action = NotFoundAction.IGNORE)
    @JoinColumn(
        name = "station_id",
    )
    @ManyToOne(fetch = FetchType.LAZY)
    var station: Station,
    var code: String,
) : IdentityIdEntity()
