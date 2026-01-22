package com.pluxity.patrol.service

import com.pluxity.facility.FacilityRepository
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.messaging.dto.ScenarioTriggerMessage
import com.pluxity.messaging.dto.TriggerTargetInfo
import com.pluxity.patrol.constant.ScenarioExecutionStatus
import com.pluxity.patrol.constant.TriggerSource
import com.pluxity.patrol.dto.ScenarioExecutionDetailResponse
import com.pluxity.patrol.dto.ScenarioExecutionResponse
import com.pluxity.patrol.dto.ScenarioExecutionSearchRequest
import com.pluxity.patrol.entity.Scenario
import com.pluxity.patrol.entity.ScenarioExecution
import com.pluxity.patrol.entity.SceneExecution
import com.pluxity.patrol.entity.Trigger
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
    private val scenarioExecutionRepository: ScenarioExecutionRepository,
    private val scenarioRepository: ScenarioRepository,
    private val eventPublisher: ApplicationEventPublisher,
    private val facilityRepository: FacilityRepository,
) {
    fun execute(
        scenario: Scenario,
        trigger: Trigger,
    ) {
        val now = LocalDateTime.now()

        val execution =
            scenarioExecutionRepository.save(
                ScenarioExecution(
                    scenarioName = scenario.name,
                    facilityName = scenario.facility.name,
                    triggerType = TriggerSource.AUTO,
                    executionStatus = ScenarioExecutionStatus.TRIGGERED,
                    triggeredAt = now,
                    startedAt = now,
                ),
            )

        scenario.scenarioScenes.forEach { scenarioScene ->
            execution.addSceneExecution(
                SceneExecution(
                    scenarioExecution = execution,
                    sceneName = scenarioScene.scene.name,
                    executionOrder = scenarioScene.executionOrder,
                    duration = scenarioScene.duration,
                    position = scenarioScene.scene.position,
                    rotation = scenarioScene.scene.rotation,
                ),
            )
        }

        val targets =
            trigger.triggerTargets.map { target ->
                TriggerTargetInfo(
                    targetType = target.targetType,
                    targetId = target.targetId,
                )
            }

        eventPublisher.publishEvent(
            ScenarioTriggerMessage(execution.requiredId, scenario.requiredId, targets),
        )
    }

    fun startManually(scenarioId: Long): Long {
        val scenario =
            scenarioRepository.findByIdWithDetails(scenarioId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENARIO, scenarioId)

        val savedId =
            scenarioExecutionRepository
                .save(
                    ScenarioExecution(
                        scenarioName = scenario.name,
                        facilityName = scenario.facility.name,
                        triggerType = TriggerSource.MANUAL,
                        executionStatus = ScenarioExecutionStatus.RUNNING,
                        startedAt = LocalDateTime.now(),
                    ),
                ).requiredId

        val scenarioExecution =
            scenarioExecutionRepository.findByIdOrNull(savedId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENARIO_EXECUTION, savedId)

        scenario.scenarioScenes.forEach { scenarioScene ->
            scenarioExecution.addSceneExecution(
                SceneExecution(
                    scenarioExecution = scenarioExecution,
                    sceneName = scenarioScene.scene.name,
                    executionOrder = scenarioScene.executionOrder,
                    duration = scenarioScene.duration,
                    position = scenarioScene.scene.position,
                    rotation = scenarioScene.scene.rotation,
                ),
            )
        }
        return savedId
    }

    fun complete(executionId: Long) {
        val execution =
            scenarioExecutionRepository.findByIdOrNull(executionId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENARIO_EXECUTION)
        execution.complete(LocalDateTime.now())
    }

    fun fail(
        executionId: Long,
        errorMessage: String?,
    ) {
        val execution =
            scenarioExecutionRepository.findByIdOrNull(executionId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENARIO_EXECUTION)
        execution.fail(LocalDateTime.now(), errorMessage)
    }

    fun cancel(executionId: Long) {
        val execution =
            scenarioExecutionRepository.findByIdOrNull(executionId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENARIO_EXECUTION)

        execution.cancel(LocalDateTime.now())
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): ScenarioExecutionDetailResponse {
        val execution =
            scenarioExecutionRepository.findByIdWithDetails(id)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENARIO_EXECUTION, id)
        return ScenarioExecutionDetailResponse.from(execution)
    }

    @Transactional(readOnly = true)
    fun findByScenarioId(
        scenarioId: Long,
        request: ScenarioExecutionSearchRequest,
    ): List<ScenarioExecutionResponse> {
        val startDateTime = request.startDate?.atStartOfDay()
        val endDateTime = request.endDate?.atTime(23, 59, 59)

        val scenario =
            scenarioRepository.findByIdOrNull(scenarioId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENARIO, scenarioId)

        return scenarioExecutionRepository
            .findByScenarioIdAndFilters(
                scenarioName = scenario.name,
                status = request.status,
                startDate = startDateTime,
                endDate = endDateTime,
            ).map { ScenarioExecutionResponse.from(it) }
    }

    @Transactional(readOnly = true)
    fun findByFacilityId(
        facilityId: Long,
        request: ScenarioExecutionSearchRequest,
    ): List<ScenarioExecutionResponse> {
        val startDateTime = request.startDate?.atStartOfDay()
        val endDateTime = request.endDate?.atTime(23, 59, 59)

        val facility = (
            facilityRepository.findByIdOrNull(facilityId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_FACILITY, facilityId)
        )

        return scenarioExecutionRepository
            .findByFacilityIdAndFilters(
                facilityName = facility.name,
                status = request.status,
                startDate = startDateTime,
                endDate = endDateTime,
            ).map { ScenarioExecutionResponse.from(it) }
    }
}
