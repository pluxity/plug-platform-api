package com.pluxity.patrol.event

data class ScenarioExecutedEvent(
    val scenarioExecutionId: Long,
    val scenarioId: Long,
)
