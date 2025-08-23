package com.pluxity.station.dto

import facility.dummyCreateFacilityRequest
import facility.dummyUpdateFacilityRequest
import facility.floor.dummyFloorRequest

fun dummyCreateStationRequest(): StationCreateRequest =
    StationCreateRequest(
        dummyCreateFacilityRequest(),
        listOf(dummyFloorRequest()),
        listOf(1),
        listOf("code"),
    )

fun dummyUpdateStationRequest(): StationUpdateRequest =
    StationUpdateRequest(
        dummyUpdateFacilityRequest(),
        listOf(dummyFloorRequest()),
        StationUpdateInfo(listOf(1L), listOf("code")),
    )

fun dummyLineCreateRequest(): LineCreateRequest = LineCreateRequest("name", "color")

fun dummyLineUpdateRequest(): LineUpdateRequest = LineUpdateRequest("updateName", "updateColor")
