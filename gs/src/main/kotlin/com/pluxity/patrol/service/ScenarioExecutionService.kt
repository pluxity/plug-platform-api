package com.pluxity.patrol.service

import com.pluxity.facility.FacilityRepository
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.global.utils.findAllNotNull
import com.pluxity.messaging.dto.ScenarioTriggerInfo
import com.pluxity.messaging.dto.TriggerTargetInfo
import com.pluxity.patrol.constant.ScenarioExecutionStatus
import com.pluxity.patrol.constant.TriggerSource
import com.pluxity.patrol.dto.ScenarioExecutionDetailResponse
import com.pluxity.patrol.dto.ScenarioExecutionResponse
import com.pluxity.patrol.dto.toDetailResponse
import com.pluxity.patrol.dto.toResponse
import com.pluxity.patrol.entity.Scenario
import com.pluxity.patrol.entity.ScenarioExecution
import com.pluxity.patrol.entity.ScenarioScene
import com.pluxity.patrol.entity.SceneDeviceActionExecution
import com.pluxity.patrol.entity.SceneExecution
import com.pluxity.patrol.entity.Trigger
import com.pluxity.patrol.repository.ScenarioExecutionRepository
import com.pluxity.patrol.repository.ScenarioRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class ScenarioExecutionService(
    private val scenarioExecutionRepository: ScenarioExecutionRepository,
    private val scenarioRepository: ScenarioRepository,
    private val facilityRepository: FacilityRepository,
) {
    @Transactional
    fun execute(
        scenario: Scenario,
        trigger: Trigger,
    ): ScenarioTriggerInfo {
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

        createSceneExecutions(execution, scenario.scenarioScenes)

        val targets =
            trigger.triggerTargets.map { target ->
                TriggerTargetInfo(
                    targetType = target.targetType,
                    targetId = target.targetId,
                )
            }

        return ScenarioTriggerInfo(
            scenarioExecutionId = execution.requiredId,
            scenarioId = scenario.requiredId,
            targets = targets,
        )
    }

    @Transactional
    fun startManually(scenarioId: Long): Long {
        val scenario =
            scenarioRepository.findByIdWithDetails(scenarioId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENARIO, scenarioId)

        val execution =
            scenarioExecutionRepository.save(
                ScenarioExecution(
                    scenarioName = scenario.name,
                    facilityName = scenario.facility.name,
                    triggerType = TriggerSource.MANUAL,
                    executionStatus = ScenarioExecutionStatus.RUNNING,
                    startedAt = LocalDateTime.now(),
                ),
            )

        createSceneExecutions(execution, scenario.scenarioScenes)

        return execution.requiredId
    }

    private fun createSceneExecutions(
        scenarioExecution: ScenarioExecution,
        scenarioScenes: List<ScenarioScene>,
    ) {
        scenarioScenes.forEach { scenarioScene ->
            val sceneExecution =
                SceneExecution(
                    scenarioExecution = scenarioExecution,
                    sceneName = scenarioScene.scene.name,
                    executionOrder = scenarioScene.executionOrder,
                    duration = scenarioScene.duration,
                    position = scenarioScene.scene.position,
                    rotation = scenarioScene.scene.rotation,
                )

            scenarioExecution.addSceneExecution(sceneExecution)

            scenarioScene.scene.sceneDeviceActions.forEach { deviceAction ->
                sceneExecution.addSceneDeviceActionExecution(
                    SceneDeviceActionExecution(
                        sceneExecution = sceneExecution,
                        deviceType = deviceAction.deviceType,
                        deviceId = deviceAction.deviceId,
                        deviceAction = deviceAction.deviceAction,
                        actionParam = deviceAction.actionParam,
                        executionOrder = deviceAction.executionOrder,
                    ),
                )
            }
        }
    }

    @Transactional
    fun complete(executionId: Long) {
        val execution =
            scenarioExecutionRepository.findByIdOrNull(executionId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENARIO_EXECUTION)
        execution.complete(LocalDateTime.now())
    }

    @Transactional
    fun fail(
        executionId: Long,
        errorMessage: String?,
    ) {
        val execution =
            scenarioExecutionRepository.findByIdOrNull(executionId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENARIO_EXECUTION)
        execution.fail(LocalDateTime.now(), errorMessage)
    }

    @Transactional
    fun cancel(executionId: Long) {
        val execution =
            scenarioExecutionRepository.findByIdOrNull(executionId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENARIO_EXECUTION)

        execution.cancel(LocalDateTime.now())
    }

    @Transactional
    fun running(executionId: Long) {
        val execution =
            scenarioExecutionRepository.findByIdOrNull(executionId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENARIO_EXECUTION)

        execution.running(LocalDateTime.now())
    }

    fun findById(id: Long): ScenarioExecutionDetailResponse {
        val execution =
            scenarioExecutionRepository.findByIdWithDetails(id)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENARIO_EXECUTION, id)
        return execution.toDetailResponse()
    }

    fun findByFilters(
        facilityId: Long?,
        scenarioId: Long?,
        status: ScenarioExecutionStatus?,
        startDate: LocalDate?,
        endDate: LocalDate?,
    ): List<ScenarioExecutionResponse> {
        val startDateTime = startDate?.atStartOfDay()
        val endDateTime = endDate?.atTime(23, 59, 59)

        val facility =
            facilityId?.let {
                facilityRepository.findByIdOrNull(it) ?: throw CustomException(ErrorCode.NOT_FOUND_FACILITY, it)
            }

        val scenario =
            scenarioId?.let {
                scenarioRepository.findByIdOrNull(it) ?: throw CustomException(ErrorCode.NOT_FOUND_SCENARIO, it)
            }

        return scenarioExecutionRepository
            .findAllNotNull {
                select(
                    entity(ScenarioExecution::class),
                ).from(
                    entity(ScenarioExecution::class),
                ).where(
                    and(
                        status?.let { path(ScenarioExecution::executionStatus).eq(it) },
                        facility?.let { path(ScenarioExecution::facilityName).eq(it.name) },
                        scenario?.let { path(ScenarioExecution::scenarioName).eq(it.name) },
                        startDateTime?.let { path(ScenarioExecution::startedAt).ge(it) },
                        endDateTime?.let { path(ScenarioExecution::startedAt).le(it) },
                    ),
                ).orderBy(path(ScenarioExecution::startedAt).desc())
            }.map { it.toResponse() }
    }
}
