package com.pluxity.patrol.entity

import base.entity.withId
import com.pluxity.facility.Facility
import com.pluxity.feature.entity.Spatial
import com.pluxity.patrol.constant.DeviceAction
import com.pluxity.patrol.constant.DeviceType
import facility.dummyFacility

fun dummyScene(
    id: Long = 1L,
    facility: Facility = dummyFacility(),
    name: String = "테스트 씬",
    description: String? = "테스트 설명",
    duration: Double? = 10.0,
    position: Spatial? = Spatial(0.0, 0.0, 0.0),
    rotation: Spatial? = Spatial(0.0, 0.0, 0.0),
): Scene =
    Scene(
        facility = facility,
        name = name,
        description = description,
        duration = duration,
        position = position,
        rotation = rotation,
    ).withId(id)

fun dummySceneDeviceAction(
    id: Long = 1L,
    scene: Scene,
    deviceType: DeviceType = DeviceType.CCTV,
    deviceId: String = "device-1",
    deviceAction: DeviceAction = DeviceAction.VIEW,
    actionParam: String? = null,
    executionOrder: Int? = 0,
): SceneDeviceAction =
    SceneDeviceAction(
        scene = scene,
        deviceType = deviceType,
        deviceId = deviceId,
        deviceAction = deviceAction,
        actionParam = actionParam,
        executionOrder = executionOrder,
    ).withId(id)
