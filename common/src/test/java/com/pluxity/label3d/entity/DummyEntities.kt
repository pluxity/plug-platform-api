package com.pluxity.label3d.entity

import com.pluxity.feature.entity.Feature
import com.pluxity.label3d.Label3D
import entity.dummyFeature

fun dummyLabel3d(
    id: String? = "id",
    feature: Feature = dummyFeature(),
    displayText: String? = "displayText",
) = Label3D(id, feature, displayText)
