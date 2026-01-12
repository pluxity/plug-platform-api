package com.pluxity.patrol.dto

import com.pluxity.feature.entity.Spatial

data class SceneUpdateRequest(
    val name: String?,
    val description: String?,
    val duration: Double?,
    val rotation: Spatial?,
    val position: Spatial?,
    val sceneDeviceActionRequests: ArrayList<SceneDeviceActionRequest>?,
)
