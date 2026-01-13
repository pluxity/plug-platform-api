package com.pluxity.patrol.entity

import com.pluxity.global.entity.IdentityIdEntity
import com.pluxity.patrol.constant.DeviceAction
import com.pluxity.patrol.constant.DeviceType
import com.pluxity.patrol.dto.SceneDeviceActionUpdateRequest
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

@Entity
@Table(name = "scene_device_actions")
class SceneDeviceAction(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scene_id", nullable = false)
    var scene: Scene,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var deviceType: DeviceType,
    @Column(nullable = false)
    var deviceId: String,
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var deviceAction: DeviceAction,
    @Column(columnDefinition = "TEXT")
    var actionParam: String? = null,
    var executionOrder: Int? = 0,
) : IdentityIdEntity() {
    fun updateSceneDeviceAction(request: SceneDeviceActionUpdateRequest) {
        this.deviceType = request.deviceType
        this.deviceId = request.deviceId
        this.deviceAction = request.deviceAction
        this.actionParam = request.actionParam
        this.executionOrder = request.executionOrder ?: 0
    }
}
