package com.pluxity.patrol.entity

import com.pluxity.global.entity.IdentityIdEntity
import com.pluxity.patrol.constant.ScenarioExecutionStatus
import com.pluxity.patrol.constant.TriggerSource
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime

@Entity
class ScenarioExecution(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    val scenario: Scenario,
    var triggerType: TriggerSource,
    var executionStatus: ScenarioExecutionStatus,
    val triggeredAt: LocalDateTime,
    var startedAt: LocalDateTime? = null,
    var finishedAt: LocalDateTime? = null,
    var errorMessage: String? = null,
) : IdentityIdEntity()
