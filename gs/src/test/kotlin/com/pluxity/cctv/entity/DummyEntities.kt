package com.pluxity.cctv.entity

import com.pluxity.feature.entity.Feature

fun dummyCctv(
    id: String = "cctvId",
    name: String = "cctvName",
    url: String = "url",
    feature: Feature? = null,
): Cctv = Cctv(id, name, url, feature)
