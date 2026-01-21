package com.pluxity.patrol.service

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.patrol.constant.ScenarioExecutionStatus
import com.pluxity.patrol.constant.TriggerSource
import com.pluxity.patrol.entity.Scenario
import com.pluxity.patrol.entity.ScenarioExecution
import com.pluxity.patrol.event.ScenarioExecutedEvent
import com.pluxity.patrol.repository.ScenarioExecutionRepository
import com.pluxity.patrol.repository.ScenarioRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional
class ScenarioExecutionService(
    private val repository: ScenarioExecutionRepository,
    private val scenarioRepository: ScenarioRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    fun execute(scenario: Scenario) {
        val now = LocalDateTime.now()

        val execution =
            repository.save(
                ScenarioExecution(
                    scenario = scenario,
                    triggerType = TriggerSource.AUTO,
                    executionStatus = ScenarioExecutionStatus.TRIGGERED,
                    triggeredAt = now,
                    startedAt = now,
                ),
            )

        eventPublisher.publishEvent(
            ScenarioExecutedEvent(execution.requiredId, scenario.requiredId),
        )
    }

    fun start(scenarioId: Long): Long {
        val now = LocalDateTime.now()

        val scenario =
            scenarioRepository.findByIdOrNull(scenarioId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENARIO, scenarioId)

        return repository
            .save(
                ScenarioExecution(
                    scenario = scenario,
                    triggerType = TriggerSource.MANUAL,
                    executionStatus = ScenarioExecutionStatus.RUNNING,
                    startedAt = now,
                ),
            ).requiredId
    }

    fun complete(executionId: Long) {
        val execution =
            repository.findByIdOrNull(executionId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENARIO_EXECUTION)
        execution.complete(LocalDateTime.now())
    }

    fun fail(
        executionId: Long,
        errorMessage: String?,
    ) {
        val execution =
            repository.findByIdOrNull(executionId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENARIO_EXECUTION)
        execution.fail(LocalDateTime.now(), errorMessage)
    }

    fun cancel(executionId: Long) {
        val execution =
            repository.findByIdOrNull(executionId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENARIO_EXECUTION)

        execution.cancel(LocalDateTime.now())
    }
}
