package com.pluxity.patrol.entity

import com.pluxity.global.entity.IdentityIdEntity
import com.pluxity.patrol.dto.ScenarioSceneUpdateRequest
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class ScenarioScene(
    @ManyToOne
    @JoinColumn(name = "scenario_id", nullable = false)
    var scenario: Scenario,
    @ManyToOne
    @JoinColumn(name = "scene_id", nullable = false)
    var scene: Scene,
    @Column(nullable = false)
    var executionOrder: Int,
    var duration: Double?,
) : IdentityIdEntity() {
    fun updateScenarioScene(request: ScenarioSceneUpdateRequest) {
        this.executionOrder = request.order
        this.duration = request.duration ?: scene.duration
    }
}
