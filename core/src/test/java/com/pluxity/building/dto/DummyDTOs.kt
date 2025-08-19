package com.pluxity.building.dto

import facility.dummyCreateFacilityRequest
import facility.dummyUpdateFacilityRequest
import facility.floor.dummyFloorRequest

fun dummyCreateBuildingRequest(): BuildingCreateRequest =
    BuildingCreateRequest(
        dummyCreateFacilityRequest(),
        listOf(dummyFloorRequest()),
    )

fun dummyUpdateBuildingRequest(): BuildingUpdateRequest =
    BuildingUpdateRequest(
        dummyUpdateFacilityRequest(),
        listOf(dummyFloorRequest()),
    )
