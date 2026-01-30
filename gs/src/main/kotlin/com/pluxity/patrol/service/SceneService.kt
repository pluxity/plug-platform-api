package com.pluxity.patrol.service

import com.pluxity.facility.FacilityRepository
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.patrol.dto.SceneCreateRequest
import com.pluxity.patrol.dto.SceneListResponse
import com.pluxity.patrol.dto.SceneResponse
import com.pluxity.patrol.dto.SceneUpdateRequest
import com.pluxity.patrol.dto.toListResponse
import com.pluxity.patrol.dto.toResponse
import com.pluxity.patrol.entity.Scene
import com.pluxity.patrol.entity.SceneDeviceAction
import com.pluxity.patrol.repository.ScenarioSceneRepository
import com.pluxity.patrol.repository.SceneRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SceneService(
    private val facilityRepository: FacilityRepository,
    private val sceneRepository: SceneRepository,
    private val scenarioSceneRepository: ScenarioSceneRepository,
    private val deviceManager: DeviceManager,
) {
    fun createScene(
        facilityId: Long,
        request: SceneCreateRequest,
    ): Long {
        val facility =
            facilityRepository.findByIdOrNull(facilityId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_FACILITY, facilityId)

        val scene =
            Scene(
                name = request.name,
                description = request.description,
                facility = facility,
                duration = request.duration,
                position = request.position,
                rotation = request.rotation,
            )

        val actionRequests = request.sceneDeviceActionRequests

        if (actionRequests.isEmpty()) {
            return sceneRepository.save(scene).requiredId
        }

        val deviceIdsByType =
            actionRequests
                .groupBy({ it.deviceType }, { it.deviceId })

        deviceManager.validateDevicesExist(deviceIdsByType)

        actionRequests.forEach { actionRequest ->

            if (!actionRequest.deviceType.supports(actionRequest.deviceAction)) {
                throw CustomException(ErrorCode.INVALID_DEVICE_ACTION, actionRequest.deviceType, actionRequest.deviceAction)
            }

            scene.addSceneDeviceAction(
                SceneDeviceAction(
                    scene = scene,
                    deviceType = actionRequest.deviceType,
                    deviceId = actionRequest.deviceId,
                    deviceAction = actionRequest.deviceAction,
                    actionParam = actionRequest.actionParam,
                    executionOrder = actionRequest.executionOrder,
                ),
            )
        }

        return sceneRepository.save(scene).requiredId
    }

    @Transactional(readOnly = true)
    fun getScene(
        facilityId: Long,
        id: Long,
    ): SceneResponse {
        val scene =
            sceneRepository.findByIdAndFacilityIdWithDetails(id, facilityId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENE, id)
        return scene.toResponse()
    }

    @Transactional(readOnly = true)
    fun getScenesByFacilityId(facilityId: Long): List<SceneListResponse> {
        facilityRepository.findByIdOrNull(facilityId)
            ?: throw CustomException(ErrorCode.NOT_FOUND_FACILITY, facilityId)

        return sceneRepository
            .findByFacilityId(facilityId)
            .map { it.toListResponse() }
    }

    fun updateScene(
        facilityId: Long,
        id: Long,
        request: SceneUpdateRequest,
    ) {
        val scene =
            sceneRepository.findByIdAndFacilityIdWithDetails(id, facilityId)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENE, id)

        scene.updateScene(request)

        val actionRequests = request.sceneDeviceActionRequests

        val requestIds = actionRequests.mapNotNull { it.sceneDeviceActionId }.toSet()
        scene.sceneDeviceActions.removeIf { it.id !in requestIds }

        if (actionRequests.isEmpty()) return

        val deviceIdsByType =
            actionRequests
                .groupBy({ it.deviceType }, { it.deviceId })

        deviceManager.validateDevicesExist(deviceIdsByType)

        actionRequests.forEach { actionRequest ->
            if (!actionRequest.deviceType.supports(actionRequest.deviceAction)) {
                throw CustomException(ErrorCode.INVALID_DEVICE_ACTION, actionRequest.deviceType, actionRequest.deviceAction)
            }

            if (actionRequest.sceneDeviceActionId != null) {
                // 수정: ID가 있으면 기존 객체 찾아 덮어쓰기
                scene.sceneDeviceActions
                    .find { it.id == actionRequest.sceneDeviceActionId }
                    ?.updateSceneDeviceAction(actionRequest)
                    ?: throw CustomException(ErrorCode.NOT_FOUND_ACTION)
            } else {
                // 생성: ID가 없으면 새로 추가
                scene.addSceneDeviceAction(
                    SceneDeviceAction(
                        scene = scene,
                        deviceType = actionRequest.deviceType,
                        deviceId = actionRequest.deviceId,
                        deviceAction = actionRequest.deviceAction,
                        actionParam = actionRequest.actionParam,
                        executionOrder = actionRequest.executionOrder,
                    ),
                )
            }
        }
    }

    fun deleteScene(
        facilityId: Long,
        id: Long,
    ) {
        scenarioSceneRepository.deleteAllBySceneId(id)
        val deletedCount = sceneRepository.deleteByIdAndFacilityId(id, facilityId)
        if (deletedCount == 0L) {
            throw CustomException(ErrorCode.NOT_FOUND_SCENE, id)
        }
    }
}
