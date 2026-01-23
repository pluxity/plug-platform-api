package com.pluxity.patrol.service

import com.pluxity.facility.FacilityRepository
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.patrol.dto.ScenarioCreateRequest
import com.pluxity.patrol.dto.ScenarioListResponse
import com.pluxity.patrol.dto.ScenarioResponse
import com.pluxity.patrol.dto.ScenarioSceneUpdateRequest
import com.pluxity.patrol.dto.ScenarioUpdateRequest
import com.pluxity.patrol.dto.TriggerRequest
import com.pluxity.patrol.entity.Scenario
import com.pluxity.patrol.entity.ScenarioScene
import com.pluxity.patrol.repository.ScenarioRepository
import com.pluxity.patrol.repository.SceneRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ScenarioService(
    val scenarioRepository: ScenarioRepository,
    val sceneRepository: SceneRepository,
    val facilityRepository: FacilityRepository,
    val triggerService: TriggerService,
) {
    fun createScenario(
        facilityId: Long,
        request: ScenarioCreateRequest,
    ): Long {
        val facility =
            facilityRepository.findByIdOrNull(facilityId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_FACILITY)

        val scenario =
            Scenario(
                facility = facility,
                name = request.name,
                description = request.description,
                isActive = request.isActive,
            )

        request.scenarioSceneRequests?.let { sceneRequests ->
            val executionOrders = sceneRequests.map { it.order }
            validateExecutionOrders(executionOrders)

            val sceneIds = sceneRequests.map { it.sceneId }
            val scenes = sceneRepository.findAllById(sceneIds).associateBy { it.id }

            sceneRequests.forEach { sceneRequest ->
                val findScene =
                    scenes[sceneRequest.sceneId]
                        ?: throw CustomException(ErrorCode.NOT_FOUND_SCENE, sceneRequest.sceneId)

                if (findScene.facility.id != facilityId) {
                    throw CustomException(ErrorCode.UNMATCHED_FACILITY_SCENE)
                }
                validateFacility(scenario, facilityId)

                val scenarioScene =
                    ScenarioScene(
                        scenario = scenario,
                        scene = findScene,
                        executionOrder = sceneRequest.order,
                        duration = sceneRequest.duration ?: findScene.duration,
                        transitionTime = sceneRequest.transitionTime,
                    )

                scenario.addScenarioScenes(scenarioScene)
            }
        }

        val savedScenario = scenarioRepository.save(scenario)

        request.triggers?.forEach { triggerRequest ->
            triggerService.createTrigger(triggerRequest, savedScenario)
        }

        return savedScenario.requiredId
    }

    @Transactional(readOnly = true)
    fun getScenario(
        facilityId: Long,
        id: Long,
    ): ScenarioResponse {
        val scenario =
            scenarioRepository.findByIdWithDetails(id)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENARIO, id)

        validateFacility(scenario, facilityId)

        return ScenarioResponse.from(scenario)
    }

    @Transactional(readOnly = true)
    fun getScenarioByFacilityId(facilityId: Long): List<ScenarioListResponse> {
        if (!facilityRepository.existsById(facilityId)) {
            throw CustomException(ErrorCode.NOT_FOUND_FACILITY, facilityId)
        }
        return scenarioRepository
            .findByFacilityId(facilityId)
            .map { ScenarioListResponse.from(it) }
    }

    fun updateScenario(
        facilityId: Long,
        id: Long,
        request: ScenarioUpdateRequest,
    ) {
        val scenario =
            scenarioRepository.findByIdWithDetails(id)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENARIO, id)

        validateFacility(scenario, facilityId)
        scenario.updateScenario(request)

        updateScenarioScenes(scenario, request.scenarioScenes ?: emptyList(), facilityId)
        updateTriggers(scenario, request.triggers ?: emptyList())
    }

    private fun updateScenarioScenes(
        scenario: Scenario,
        sceneRequests: List<ScenarioSceneUpdateRequest>,
        facilityId: Long,
    ) {
        validateExecutionOrders(sceneRequests.map { it.order })

        val requestIds = sceneRequests.mapNotNull { it.scenarioSceneId }.toSet()

        // 요청에 포함되지 않은 scenarioScene 제거
        scenario.scenarioScenes.removeIf { it.id !in requestIds }

        val scenes =
            if (sceneRequests.isNotEmpty()) {
                sceneRepository.findAllById(sceneRequests.map { it.sceneId }).associateBy { it.requiredId }
            } else {
                emptyMap()
            }

        sceneRequests.forEach { sceneRequest ->
            val scene =
                scenes[sceneRequest.sceneId]
                    ?: throw CustomException(ErrorCode.NOT_FOUND_SCENE, sceneRequest.sceneId)

            if (scene.facility.id != facilityId) {
                throw CustomException(ErrorCode.UNMATCHED_FACILITY_SCENE)
            }

            if (sceneRequest.scenarioSceneId != null) {
                val scenarioScene =
                    scenario.scenarioScenes.find { it.id == sceneRequest.scenarioSceneId }
                        ?: throw CustomException(ErrorCode.NOT_FOUND_SCENARIO_SCENE, sceneRequest.scenarioSceneId)

                if (scenarioScene.scene.id != sceneRequest.sceneId) {
                    scenarioScene.scene = scene
                }
                scenarioScene.updateScenarioScene(sceneRequest)
            } else {
                scenario.addScenarioScenes(
                    ScenarioScene(
                        scenario = scenario,
                        scene = scene,
                        executionOrder = sceneRequest.order,
                        duration = sceneRequest.duration ?: scene.duration,
                        transitionTime = sceneRequest.transitionTime,
                    ),
                )
            }
        }
    }

    private fun updateTriggers(
        scenario: Scenario,
        triggerRequests: List<TriggerRequest>,
    ) {
        val requestTriggerIds = triggerRequests.mapNotNull { it.triggerId }.toSet()
        scenario.triggers.removeIf { it.id !in requestTriggerIds }

        triggerRequests.forEach { triggerRequest ->
            if (triggerRequest.triggerId != null) {
                val existingTrigger =
                    scenario.triggers.find { it.id == triggerRequest.triggerId }
                        ?: throw CustomException(ErrorCode.NOT_FOUND_TRIGGER, triggerRequest.triggerId)
                triggerService.updateTrigger(existingTrigger, triggerRequest)
            } else {
                val newTrigger = triggerService.createTrigger(triggerRequest, scenario)
                scenario.addTrigger(newTrigger)
            }
        }
    }

    fun deleteScenario(
        facilityId: Long,
        id: Long,
    ) {
        val deletedCount = scenarioRepository.deleteByIdAndFacilityId(id, facilityId)
        if (deletedCount == 0L) {
            throw CustomException(ErrorCode.NOT_FOUND_SCENARIO, id)
        }
    }

    private fun validateExecutionOrders(executionOrders: List<Int>) {
        if (executionOrders.any { it < 1 }) {
            throw CustomException(ErrorCode.INVALID_EXECUTION_ORDER)
        }
        if (executionOrders.size != executionOrders.distinct().size) {
            throw CustomException(ErrorCode.DUPLICATE_EXECUTION_ORDER)
        }
    }

    private fun validateFacility(
        scenario: Scenario,
        facilityId: Long,
    ) {
        if (scenario.facility.id != facilityId) {
            throw CustomException(ErrorCode.UNMATCHED_FACILITY_SCENARIO)
        }
    }
}
