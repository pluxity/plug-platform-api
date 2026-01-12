package com.pluxity.station

import com.pluxity.global.entity.IdentityIdEntity
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction

@Entity
@Table(name = "station_line")
class StationLine(
    @JoinColumn(name = "station_id")
    @ManyToOne(fetch = FetchType.EAGER)
    @NotFound(action = NotFoundAction.IGNORE)
    var station: Station,
    @JoinColumn(name = "line_id")
    @ManyToOne(fetch = FetchType.LAZY)
    var line: Line,
) : IdentityIdEntity()
