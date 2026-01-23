package com.pluxity.patrol.entity

import com.pluxity.feature.entity.Spatial
import com.pluxity.global.entity.IdentityIdEntity
import jakarta.persistence.AttributeOverride
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany

@Entity
class SceneExecution(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_execution_id")
    var scenarioExecution: ScenarioExecution,
    @OneToMany(mappedBy = "sceneExecution", cascade = [CascadeType.ALL], orphanRemoval = true)
    var sceneDeviceActionExecutions: MutableList<SceneDeviceActionExecution> = mutableListOf(),
    val sceneName: String,
    val executionOrder: Int,
    val duration: Double,
    @AttributeOverride(
        name = "z",
        column = Column(name = "position_z"),
    ) @AttributeOverride(
        name = "y",
        column = Column(name = "position_y"),
    ) @AttributeOverride(
        name = "x",
        column = Column(name = "position_x"),
    ) @Embedded var position: Spatial? = Spatial(0.0, 0.0, 0.0),
    @AttributeOverride(
        name = "z",
        column = Column(name = "rotation_z"),
    ) @AttributeOverride(
        name = "y",
        column = Column(name = "rotation_y"),
    ) @AttributeOverride(
        name = "x",
        column = Column(name = "rotation_x"),
    ) @Embedded var rotation: Spatial? = Spatial(0.0, 0.0, 0.0),
) : IdentityIdEntity() {
    fun addSceneDeviceActionExecution(sceneDeviceActionExecution: SceneDeviceActionExecution) {
        this.sceneDeviceActionExecutions.add(sceneDeviceActionExecution)
        sceneDeviceActionExecution.sceneExecution = this
    }
}
