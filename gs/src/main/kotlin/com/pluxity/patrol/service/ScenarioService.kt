package com.pluxity.patrol.service

import com.pluxity.facility.FacilityRepository
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.patrol.dto.ScenarioCreateRequest
import com.pluxity.patrol.dto.ScenarioListResponse
import com.pluxity.patrol.dto.ScenarioResponse
import com.pluxity.patrol.dto.ScenarioUpdateRequest
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
                    )

                scenario.addScenarioScenes(scenarioScene)
            }
        }

        return scenarioRepository.save(scenario).requiredId
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

        // 기본 필드 업데이트
        scenario.updateScenario(request)

        val sceneRequests = request.scenarioScenes ?: emptyList()

        // 순서 검증
        val requestOrderIds = sceneRequests.map { it.order }
        validateExecutionOrders(requestOrderIds)

        // 요청에 scenarioScene이 없다면 모두 제거
        val requestIds = sceneRequests.mapNotNull { it.scenarioSceneId }.toSet()
        scenario.scenarioScenes.removeIf { it.id !in requestIds }

        val sceneIds = sceneRequests.map { it.sceneId }
        val scenes =
            if (sceneIds.isNotEmpty()) {
                sceneRepository.findAllById(sceneIds).associateBy { it.id }
            } else {
                emptyMap()
            }

        sceneRequests.forEach { sceneRequest ->
            if (sceneRequest.scenarioSceneId != null) {
                val scenarioScene =
                    scenario.scenarioScenes.find { it.id == sceneRequest.scenarioSceneId }
                        ?: throw CustomException(ErrorCode.NOT_FOUND_SCENARIO_SCENE, sceneRequest.scenarioSceneId)

                // scene이 변경된 경우
                if (scenarioScene.scene.id != sceneRequest.sceneId) {
                    val newScene =
                        scenes[sceneRequest.sceneId]
                            ?: throw CustomException(ErrorCode.NOT_FOUND_SCENE, sceneRequest.sceneId)
                    if (newScene.facility.id != facilityId) {
                        throw CustomException(ErrorCode.UNMATCHED_FACILITY_SCENE)
                    }
                    scenarioScene.scene = newScene
                }
                scenarioScene.updateScenarioScene(sceneRequest)
            } else {
                val findScene =
                    scenes[sceneRequest.sceneId]
                        ?: throw CustomException(ErrorCode.NOT_FOUND_SCENE, sceneRequest.sceneId)

                if (findScene.facility.id != facilityId) {
                    throw CustomException(ErrorCode.UNMATCHED_FACILITY_SCENE)
                }

                scenario.addScenarioScenes(
                    ScenarioScene(
                        scenario = scenario,
                        scene = findScene,
                        executionOrder = sceneRequest.order,
                        duration = sceneRequest.duration ?: findScene.duration,
                    ),
                )
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
