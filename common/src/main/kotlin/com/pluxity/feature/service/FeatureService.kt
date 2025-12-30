package com.pluxity.feature.service

import com.pluxity.asset.service.AssetValidator
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
    private val featureAssignmentRegistry: FeatureAssignmentRegistry,
) {
    @Transactional
    fun createFeature(request: FeatureCreateRequest): FeatureResponse {
        log.debug { "피처 생성 요청: id=${request.id}, facilityId=${request.facilityId}, assetId=${request.assetId}" }

        // ID 중복 체크
        val featureId = request.id
        if (featureRepository.findByIdOrNull(featureId) != null) {
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
        featureAssignmentRegistry.get(FeatureAssignType.CCTV)?.revokeByFeature(feature)
        featureAssignmentRegistry.get(FeatureAssignType.THERMO_HYGROMETER)?.revokeByFeature(feature)
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
            FeatureAssignType.THERMO_HYGROMETER -> assignDeviceToFeature(featureId, assignDto, force)
            FeatureAssignType.CCTV -> assignCctvToFeature(featureId, assignDto, force)
        }
    }

    @Transactional
    fun removeSomethingFromFeature(
        featureId: String,
        assignDto: FeatureAssignDto,
    ) {
        when (assignDto.type) {
            FeatureAssignType.THERMO_HYGROMETER -> removeDeviceFromFeature(featureId, assignDto)
            FeatureAssignType.CCTV -> removeCctvFromFeature(featureId, assignDto)
        }
    }

    private fun assignCctvToFeature(
        featureId: String,
        assignDto: FeatureAssignDto,
        force: Boolean,
    ) {
        val feature = findFeatureById(featureId)
        if (!force && featureAssignmentRegistry.get(FeatureAssignType.CCTV)?.isAssigned(assignDto.id) == true) {
            throw CustomException(
                ErrorCode.ALREADY_ASSIGNED_TARGET,
                assignDto.id,
                assignDto.type.description,
            )
        }
        validateAssign(featureId, force, feature)
        clearExistingAssignments(feature)
        featureAssignmentRegistry.get(FeatureAssignType.CCTV)?.assignFeature(assignDto.id, feature)
    }

    private fun removeCctvFromFeature(
        featureId: String,
        assignDto: FeatureAssignDto,
    ) {
        featureAssignmentRegistry.get(FeatureAssignType.CCTV)?.validateRevoke(assignDto.id, featureId)
        featureAssignmentRegistry.get(FeatureAssignType.CCTV)?.clearFeatureFromTarget(assignDto.id)
    }

    private fun assignDeviceToFeature(
        featureId: String,
        assignDto: FeatureAssignDto,
        force: Boolean,
    ) {
        log.debug { "피처에 디바이스 할당: featureId=$featureId, assignDto=$assignDto" }

        val feature = findFeatureById(featureId)

        // 디바이스 조회 - id로 조회
        if (!force && featureAssignmentRegistry.get(FeatureAssignType.THERMO_HYGROMETER)?.isAssigned(assignDto.id) == true) {
            throw CustomException(ErrorCode.DUPLICATE_DEVICE_OTHER_FEATURE, assignDto.id)
        }
        validateAssign(featureId, force, feature)
        clearExistingAssignments(feature)
        featureAssignmentRegistry.get(FeatureAssignType.THERMO_HYGROMETER)?.assignFeature(assignDto.id, feature)

        log.debug { "온습도계와 피처 관계 설정 완료: deviceId=${assignDto.id}, featureId=$featureId" }
    }

    private fun clearExistingAssignments(feature: Feature) {
        featureAssignmentRegistry.get(FeatureAssignType.THERMO_HYGROMETER)?.revokeByFeature(feature)
        featureAssignmentRegistry.get(FeatureAssignType.CCTV)?.revokeByFeature(feature)
    }

    private fun validateAssign(
        featureId: String?,
        force: Boolean,
        feature: Feature,
    ) {
        val isAssignCctv = featureAssignmentRegistry.get(FeatureAssignType.CCTV)?.existsByFeature(feature) ?: false
        if (!force && isAssignCctv) {
            throw CustomException(ErrorCode.DUPLICATE_FEATURE_OTHER_CCTV, featureId)
        }
        val isAssignFeature =
            featureAssignmentRegistry.get(FeatureAssignType.THERMO_HYGROMETER)?.existsByFeature(feature) ?: false
        if (!force && isAssignFeature) {
            throw CustomException(ErrorCode.ALREADY_FEATURE_ASSIGNED, featureId)
        }
    }

    private fun removeDeviceFromFeature(
        featureId: String,
        assignDto: FeatureAssignDto,
    ) {
        val feature = findFeatureById(featureId)
        featureAssignmentRegistry.get(FeatureAssignType.THERMO_HYGROMETER)?.validateRevoke(assignDto.id, feature.id)
        featureAssignmentRegistry.get(FeatureAssignType.THERMO_HYGROMETER)?.clearFeatureFromTarget(assignDto.id)
        log.debug { "피처에서 디바이스 제거: featureId=$featureId, deviceId=${assignDto.id}" }
    }
}
