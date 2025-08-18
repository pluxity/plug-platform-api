package com.pluxity.park.dto

import facility.dummyCreateFacilityRequest
import facility.dummyUpdateFacilityRequest

fun dummyCreateParkRequest(boundary: String? = null): ParkCreateRequest =
    ParkCreateRequest(
        dummyCreateFacilityRequest(),
        boundary,
    )

fun dummyUpdateParkRequest(boundary: String = "dummyBoundary"): ParkUpdateRequest =
    ParkUpdateRequest(
        dummyUpdateFacilityRequest(),
        boundary,
    )
