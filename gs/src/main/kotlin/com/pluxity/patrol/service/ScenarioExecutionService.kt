package com.pluxity.patrol.service

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.patrol.constant.ScenarioExecutionStatus
import com.pluxity.patrol.constant.TriggerSource
import com.pluxity.patrol.entity.Scenario
import com.pluxity.patrol.entity.ScenarioExecution
import com.pluxity.patrol.event.ScenarioExecutedEvent
import com.pluxity.patrol.repository.ScenarioExecutionRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ScenarioExecutionService(
    private val repository: ScenarioExecutionRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    @Transactional
    fun execute(scenario: Scenario) {
        val execution =
            repository.save(
                ScenarioExecution(
                    scenario = scenario,
                    triggerType = TriggerSource.AUTO,
                    executionStatus = ScenarioExecutionStatus.TRIGGERED,
                    triggeredAt = LocalDateTime.now(),
                ),
            )

        eventPublisher.publishEvent(
            ScenarioExecutedEvent(execution.requiredId, scenario.requiredId),
        )
    }

    @Transactional
    fun start(executionId: Long) {
        val triggeredExecution =
            repository.findByIdOrNull(executionId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_DATA)

        repository.save(
            ScenarioExecution(
                scenario = triggeredExecution.scenario,
                triggerType = triggeredExecution.triggerType,
                executionStatus = ScenarioExecutionStatus.RUNNING,
                triggeredAt = triggeredExecution.triggeredAt,
                startedAt = LocalDateTime.now(),
            ),
        )
    }

    @Transactional
    fun complete(
        executionId: Long,
        success: Boolean,
        errorMessage: String?,
    ) {
        val triggeredExecution =
            repository.findByIdOrNull(executionId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_DATA)

        repository.save(
            ScenarioExecution(
                scenario = triggeredExecution.scenario,
                triggerType = triggeredExecution.triggerType,
                executionStatus = if (success) ScenarioExecutionStatus.COMPLETED else ScenarioExecutionStatus.FAILED,
                triggeredAt = triggeredExecution.triggeredAt,
                finishedAt = LocalDateTime.now(),
                errorMessage = errorMessage,
                startedAt = triggeredExecution.startedAt,
            ),
        )
    }

    @Transactional
    fun cancel(executionId: Long) {
        val triggeredExecution =
            repository.findByIdOrNull(executionId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_DATA)

        repository.save(
            ScenarioExecution(
                scenario = triggeredExecution.scenario,
                triggerType = triggeredExecution.triggerType,
                executionStatus = ScenarioExecutionStatus.CANCELLED,
                triggeredAt = triggeredExecution.triggeredAt,
                finishedAt = LocalDateTime.now(),
                startedAt = triggeredExecution.startedAt,
            ),
        )
    }
}
