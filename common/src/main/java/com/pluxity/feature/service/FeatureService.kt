package com.pluxity.feature.service

import com.pluxity.asset.service.AssetValidator
import com.pluxity.device.entity.Device
import com.pluxity.device.repository.DeviceRepository
import com.pluxity.facility.FacilityService
import com.pluxity.feature.dto.FeatureAssignDto
import com.pluxity.feature.dto.FeatureCreateRequest
import com.pluxity.feature.dto.FeatureResponse
import com.pluxity.feature.dto.FeatureUpdateRequest
import com.pluxity.feature.dto.toFeatureResponse
import com.pluxity.feature.entity.Feature
import com.pluxity.feature.entity.Spatial
import com.pluxity.feature.repository.FeatureRepository
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
class FeatureService(
    private val featureRepository: FeatureRepository,
    private val facilityService: FacilityService,
    private val assetValidator: AssetValidator,
    private val deviceRepository: DeviceRepository,
    private val featureAssignment: FeatureAssignment?,
) {
    @Transactional
    fun createFeature(request: FeatureCreateRequest): FeatureResponse {
        log.debug { "피처 생성 요청: id=${request.id}, facilityId=${request.facilityId}, assetId=${request.assetId}" }

        // ID 중복 체크
        val featureId = request.id
        featureRepository.findByIdOrNull(featureId)?.let {
            throw CustomException(ErrorCode.DUPLICATE_FEATURE_ID, featureId)
        }

        // 먼저 관련 엔티티 조회
        val facility = facilityService.findById(request.facilityId)
        assetValidator.validateAssetId(request.assetId)

        // 저장
        val savedFeature: Feature =
            featureRepository.save(
                Feature(
                    id = featureId,
                    position = request.position ?: Spatial(0.0, 0.0, 0.0),
                    rotation = request.rotation ?: Spatial(0.0, 0.0, 0.0),
                    scale = request.scale ?: Spatial(1.0, 1.0, 1.0),
                    assetId = request.assetId,
                    floorId = request.floorId,
                    facility = facility,
                ),
            )
        log.debug { "피처 저장 완료: id=${savedFeature.id}" }

        return savedFeature.toFeatureResponse()
    }

    @Transactional(readOnly = true)
    fun getFeatures(facilityId: Long): List<FeatureResponse> {
        val facility = facilityService.findById(facilityId)
        return featureRepository
            .findByFacilityOrderByCreatedAtDesc(facility)
            .map { it.toFeatureResponse() }
    }

    @Transactional
    fun updateFeature(
        id: String,
        request: FeatureUpdateRequest,
    ): FeatureResponse {
        val feature = findFeatureById(id)
        feature.update(request)
        return feature.toFeatureResponse()
    }

    @Transactional
    fun deleteFeature(id: String) {
        val feature = findFeatureById(id)
        featureAssignment?.revokeByFeature(feature)
        deviceRepository.revokeByFeature(feature)
        featureRepository.delete(feature)
    }

    fun findFeatureById(id: String): Feature =
        featureRepository.findByIdOrNull(id) ?: throw CustomException(ErrorCode.NOT_FOUND_FEATURE, id)

    @Transactional
    fun saveFeature(feature: Feature): Feature = featureRepository.save(feature)

    @Transactional(readOnly = true)
    fun findFeatureIdsByAssetId(assetId: Long): List<String> = featureRepository.findByAssetId(assetId).mapNotNull { it.id }

    @Transactional
    fun assignSomethingToFeature(
        featureId: String,
        assignDto: FeatureAssignDto,
        force: Boolean,
    ) {
        when (assignDto.type) {
            FeatureAssignType.DEVICE -> assignDeviceToFeature(featureId, assignDto, force)
            FeatureAssignType.CCTV -> assignCctvToFeature(featureId, assignDto, force)
        }
    }

    @Transactional
    fun removeSomethingFromFeature(
        featureId: String,
        assignDto: FeatureAssignDto,
    ) {
        when (assignDto.type) {
            FeatureAssignType.DEVICE -> removeDeviceFromFeature(featureId, assignDto)
            FeatureAssignType.CCTV -> removeCctvFromFeature(featureId, assignDto)
        }
    }

    private fun assignCctvToFeature(
        featureId: String,
        assignDto: FeatureAssignDto,
        force: Boolean,
    ) {
        val feature = findFeatureById(featureId)
        if (!force && featureAssignment?.isAssigned(assignDto.id) == true) {
            throw CustomException(
                ErrorCode.ALREADY_ASSIGNED_TARGET,
                assignDto.id,
                assignDto.type.description,
            )
        }
        validateAssign(featureId, force, feature)
        clearExistingAssignments(feature)
        featureAssignment?.assignFeature(assignDto.id, feature)
    }

    private fun removeCctvFromFeature(
        featureId: String,
        assignDto: FeatureAssignDto,
    ) {
        featureAssignment?.validateRevoke(assignDto.id, featureId)
        featureAssignment?.clearFeatureFromTarget(assignDto.id)
    }

    private fun assignDeviceToFeature(
        featureId: String,
        assignDto: FeatureAssignDto,
        force: Boolean,
    ) {
        log.debug { "피처에 디바이스 할당: featureId=$featureId, assignDto=$assignDto" }

        val feature = findFeatureById(featureId)

        // 디바이스 조회 - id로 조회
        val device = findDeviceById(assignDto.id)
        if (!force && device.feature != null) {
            throw CustomException(ErrorCode.DUPLICATE_DEVICE_OTHER_FEATURE, assignDto.id)
        }
        validateAssign(featureId, force, feature)
        clearExistingAssignments(feature)
        device.changeFeature(feature)

        log.debug { "디바이스와 피처 관계 설정 완료: deviceId=${device.id}, featureId=$featureId" }
    }

    private fun clearExistingAssignments(feature: Feature) {
        deviceRepository.revokeByFeature(feature)
        featureAssignment?.revokeByFeature(feature)
    }

    private fun validateAssign(
        featureId: String?,
        force: Boolean,
        feature: Feature,
    ) {
        val isAssignCctv = featureAssignment?.existsByFeature(feature) ?: false
        if (!force && isAssignCctv) {
            throw CustomException(ErrorCode.DUPLICATE_FEATURE_OTHER_CCTV, featureId)
        }
        val isAssignFeature = deviceRepository.existsByFeature(feature)
        if (!force && isAssignFeature) {
            throw CustomException(ErrorCode.ALREADY_FEATURE_ASSIGNED, featureId)
        }
    }

    private fun findDeviceById(deviceId: String): Device =
        deviceRepository
            .findByIdOrNull(deviceId)
            ?: throw CustomException(ErrorCode.NOT_FOUND_DEVICE, deviceId)

    private fun removeDeviceFromFeature(
        featureId: String,
        assignDto: FeatureAssignDto,
    ) {
        val feature = findFeatureById(featureId)
        val device = findDeviceById(assignDto.id)

        val deviceFeature = device.feature ?: throw CustomException(ErrorCode.DEVICE_NOT_ASSIGNED, device.id)

        if (deviceFeature.id != feature.id) {
            throw CustomException(ErrorCode.DEVICE_MISMATCH)
        }

        device.changeFeature(null)
        log.debug { "피처에서 디바이스 제거: featureId=$featureId, deviceId=${device.id}" }
    }
}
