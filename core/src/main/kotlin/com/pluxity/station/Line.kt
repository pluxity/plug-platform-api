package com.pluxity.station

import com.pluxity.global.entity.IdentityIdEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import jakarta.persistence.Table

@Entity
@Table(name = "line")
class Line(
    @Column(
        unique = true,
        nullable = false,
        length = 50,
    )
    var name: String,
    var color: String?,
) : IdentityIdEntity() {
    @OneToMany(mappedBy = "line")
    val stationLines: MutableList<StationLine> = ArrayList()

    fun getStations(): List<Station> = stationLines.map { it.station }

    fun update(
        name: String?,
        color: String?,
    ) {
        name?.let { this.name = it }
        color?.let { this.color = it }
    }

    fun removeStationLine(stationLine: StationLine) {
        stationLines.remove(stationLine)
    }

    fun addStationLine(stationLine: StationLine) {
        stationLines.add(stationLine)
    }
}
