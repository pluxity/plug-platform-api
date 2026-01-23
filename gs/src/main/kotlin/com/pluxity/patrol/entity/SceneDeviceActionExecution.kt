package com.pluxity.patrol.entity

import com.pluxity.global.entity.IdentityIdEntity
import com.pluxity.patrol.constant.DeviceAction
import com.pluxity.patrol.constant.DeviceType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class SceneDeviceActionExecution(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_execution_id", nullable = false)
    var sceneExecution: SceneExecution,
    @Enumerated(EnumType.STRING)
    val deviceType: DeviceType,
    val deviceId: String,
    @Enumerated(EnumType.STRING)
    val deviceAction: DeviceAction,
    @Column(columnDefinition = "TEXT")
    val actionParam: String? = null,
    val executionOrder: Int?,
) : IdentityIdEntity()
