package com.pluxity.patrol.dto

import com.pluxity.feature.entity.Spatial

data class SceneCreateRequest(
    val name: String,
    val description: String?,
    val duration: Double?,
    val position: Spatial?,
    val rotation: Spatial?,
    val sceneDeviceActionRequests: ArrayList<SceneDeviceActionRequest>?,
)
