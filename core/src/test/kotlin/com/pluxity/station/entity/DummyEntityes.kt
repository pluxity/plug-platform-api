package com.pluxity.station.entity

import base.entity.withAudit
import base.entity.withId
import com.pluxity.station.Line
import com.pluxity.station.Station
import com.pluxity.station.StationCode
import com.pluxity.station.StationLine

fun dummyStation(
    id: Long? = 1L,
    name: String = "name",
    description: String = "description",
): Station {
    val retStation =
        Station(
            name,
            description,
        ).withAudit().withId(id)
    return retStation
}

fun dummyLine(
    id: Long? = 1L,
    name: String = "name",
    color: String = "color",
): Line = Line(name, color).withAudit().withId(id)

fun dummyStationLine(
    id: Long? = 1L,
    station: Station = dummyStation(),
): StationLine = StationLine(station, dummyLine()).withId(id)

fun dummyStationCode(station: Station = dummyStation()): StationCode = StationCode(station = station, code = "code")
