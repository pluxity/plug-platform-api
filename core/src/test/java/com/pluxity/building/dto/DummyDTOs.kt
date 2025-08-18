package com.pluxity.building.dto

import facility.dummyCreateFacilityRequest
import facility.dummyUpdateFacilityRequest
import facility.floor.dummyCreateFloorRequest

fun dummyCreateBuildingRequest(): BuildingCreateRequest =
    BuildingCreateRequest(
        dummyCreateFacilityRequest(),
        listOf(dummyCreateFloorRequest()),
    )

fun dummyUpdateBuildingRequest(): BuildingUpdateRequest =
    BuildingUpdateRequest(
        dummyUpdateFacilityRequest(),
        listOf(dummyCreateFloorRequest()),
    )
