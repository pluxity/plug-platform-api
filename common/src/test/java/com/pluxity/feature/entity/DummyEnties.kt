package com.pluxity.feature.entity

import com.pluxity.facility.Facility
import io.mockk.mockk

fun dummyFeature(
    id: String? = "feature_id",
    position: Spatial? = dummySpatial(),
    rotation: Spatial? = dummySpatial(),
    scale: Spatial? = dummySpatial(),
    assetId: Long? = 1L,
    facility: Facility? = mockk(),
    floorId: String? = "1",
) = Feature(
    id,
    position,
    rotation,
    scale,
    assetId,
    facility,
    floorId,
)

fun dummySpatial(
    x: Double = 0.0,
    y: Double = 0.0,
    z: Double = 0.0,
) = Spatial(x, y, z)
