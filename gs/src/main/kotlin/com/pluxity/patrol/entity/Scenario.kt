package com.pluxity.patrol.entity

import com.pluxity.facility.Facility
import com.pluxity.global.entity.IdentityIdEntity
import com.pluxity.patrol.dto.ScenarioUpdateRequest
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OrderBy
import org.hibernate.annotations.NotFound
import org.hibernate.annotations.NotFoundAction

@Entity
class Scenario(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id", nullable = false)
    @NotFound(action = NotFoundAction.IGNORE)
    var facility: Facility,
    @OneToMany(mappedBy = "scenario", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("executionOrder ASC")
    var scenarioScenes: MutableList<ScenarioScene> = mutableListOf(),
    @OneToMany(mappedBy = "scenario", cascade = [CascadeType.ALL], orphanRemoval = true)
    var triggers: MutableList<Trigger> = mutableListOf(),
    @Column(nullable = false, length = 100)
    var name: String,
    @Column(length = 500)
    var description: String?,
    var isActive: Boolean = true,
) : IdentityIdEntity() {
    fun addScenarioScenes(scenarioScene: ScenarioScene) {
        scenarioScenes.add(scenarioScene)
        scenarioScene.scenario = this
    }

    fun removeScenarioScenes(scenarioScene: ScenarioScene) {
        scenarioScenes.remove(scenarioScene)
    }

    fun addTrigger(trigger: Trigger) {
        triggers.add(trigger)
        trigger.scenario = this
    }

    fun removeTrigger(trigger: Trigger) {
        triggers.remove(trigger)
    }

    fun updateScenario(request: ScenarioUpdateRequest) {
        this.name = request.name
        this.description = request.description
        this.isActive = request.isActive ?: true
    }
}
