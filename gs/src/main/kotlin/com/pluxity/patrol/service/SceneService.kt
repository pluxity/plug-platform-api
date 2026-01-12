package com.pluxity.patrol.service

import com.pluxity.facility.FacilityRepository
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.patrol.dto.SceneCreateRequest
import com.pluxity.patrol.dto.SceneListResponse
import com.pluxity.patrol.dto.SceneResponse
import com.pluxity.patrol.dto.SceneUpdateRequest
import com.pluxity.patrol.entity.Scene
import com.pluxity.patrol.entity.SceneDeviceAction
import com.pluxity.patrol.repository.SceneRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class SceneService(
    val facilityRepository: FacilityRepository,
    val sceneRepository: SceneRepository,
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

        request.sceneDeviceActionRequests?.forEach { actionRequest ->
            if (!actionRequest.deviceType.execute(actionRequest.deviceAction)) {
                throw CustomException(ErrorCode.INVALID_DEVICE_ACTION, actionRequest.deviceType, actionRequest.deviceAction)
            }

            // todo: deviceId 검증 필요
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
            sceneRepository.findByIdWithDetails(id)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENE, id)
        validateFacility(scene, facilityId)
        return SceneResponse.from(scene)
    }

    @Transactional(readOnly = true)
    fun getScenesByFacilityId(facilityId: Long): List<SceneListResponse> {
        if (!facilityRepository.existsById(facilityId)) {
            throw CustomException(ErrorCode.NOT_FOUND_FACILITY, facilityId)
        }
        return sceneRepository
            .findByFacilityId(facilityId)
            .map { SceneListResponse.from(it) }
    }

    fun updateScene(
        facilityId: Long,
        id: Long,
        request: SceneUpdateRequest,
    ) {
        val scene =
            sceneRepository.findByIdWithDetails(id)
                ?: throw CustomException(ErrorCode.NOT_FOUND_SCENE, id)
        validateFacility(scene, facilityId)

        request.name?.takeIf { it != scene.name }?.let { scene.name = it }
        request.description?.takeIf { it != scene.description }?.let { scene.description = it }
        request.position?.takeIf { it != scene.position }?.let { scene.position = it }
        request.rotation?.takeIf { it != scene.rotation }?.let { scene.rotation = it }
        request.duration?.takeIf { it != scene.duration }?.let { scene.duration = it }

        // todo: deviceId 검증 필요
        request.sceneDeviceActionRequests?.let { actionRequests ->
            val requestIds = actionRequests.mapNotNull { it.sceneDeviceActionId }.toSet()

            scene.sceneDeviceActions.removeIf { it.id !in requestIds }

            actionRequests.forEach { actionRequest ->
                if (!actionRequest.deviceType.execute(actionRequest.deviceAction)) {
                    throw CustomException(ErrorCode.INVALID_DEVICE_ACTION, actionRequest.deviceType, actionRequest.deviceAction)
                }

                if (actionRequest.sceneDeviceActionId != null) {
                    // 기존 scenDeviceAction 업데이트
                    scene.sceneDeviceActions.find { it.id == actionRequest.sceneDeviceActionId }?.apply {
                        deviceType = actionRequest.deviceType
                        deviceId = actionRequest.deviceId
                        deviceAction = actionRequest.deviceAction
                        actionParam = actionRequest.actionParam
                        executionOrder = actionRequest.executionOrder ?: 0
                    } ?: throw CustomException(ErrorCode.NOT_FOUND_ACTION)
                } else {
                    // 신규 scenDeviceAction 생성
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
    }

    fun deleteScene(
        facilityId: Long,
        id: Long,
    ) {
        val deletedCount = sceneRepository.deleteByIdAndFacilityId(id, facilityId)
        if (deletedCount == 0L) {
            throw CustomException(ErrorCode.NOT_FOUND_SCENE, id)
        }
    }

    private fun validateFacility(
        scene: Scene,
        facilityId: Long,
    ) {
        if (scene.facility.id != facilityId) {
            throw CustomException(ErrorCode.UNMATCHED_FACILITY_SCENE)
        }
    }
}
