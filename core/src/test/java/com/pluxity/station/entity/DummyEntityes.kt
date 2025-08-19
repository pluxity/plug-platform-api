package com.pluxity.station.entity

import base.entity.withAudit
import com.pluxity.station.Line
import com.pluxity.station.Station
import com.pluxity.station.StationCode
import com.pluxity.station.StationLine
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime

fun dummyStation(
    id: Long? = 1L,
    name: String = "name",
    description: String = "description",
): Station {
    val retStation =
        Station(
            name,
            description,
        ).withAudit(LocalDateTime.now())
    ReflectionTestUtils.setField(retStation, "id", id)
    return retStation
}

fun dummyLine(
    id: Long? = 1L,
    name: String = "name",
    color: String = "color",
): Line = Line(id, name, color).withAudit(LocalDateTime.now())

fun dummyStationLine(
    id: Long? = 1L,
    station: Station = dummyStation(),
): StationLine = StationLine(id, station, dummyLine())

fun dummyStationCode(station: Station = dummyStation()): StationCode = StationCode(station = station, code = "code")
