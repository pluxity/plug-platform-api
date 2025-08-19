package com.pluxity.station.dto

import com.pluxity.facility.dto.FacilityResponse
import com.pluxity.facility.floor.dto.FloorResponse
import com.pluxity.feature.dto.FeatureResponse
import com.pluxity.label3d.Label3DResponse
import java.util.Collections.emptyList

class StationResponseWithFeature(
    val facility: FacilityResponse,
    floors: List<FloorResponse> = emptyList(),
    lineIds: List<Long> = emptyList(),
    features: List<FeatureResponse> = emptyList(),
    label3Ds: List<Label3DResponse> = emptyList(),
    stationCodes: List<String> = emptyList(),
)
