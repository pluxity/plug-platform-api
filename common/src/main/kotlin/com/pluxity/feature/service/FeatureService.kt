package com.pluxity.feature.service

import com.pluxity.asset.service.AssetValidator
import com.pluxity.facility.FacilityService
import com.pluxity.feature.dto.FeatureAssignDto
import com.pluxity.feature.dto.FeatureCreateRequest
import com.pluxity.feature.dto.FeatureResponse
import com.pluxity.feature.dto.FeatureUpdateRequest
import com.pluxity.feature.dto.toFeatureResponse
import com.pluxity.feature.entity.Feature
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
                    position = request.position,
                    rotation = request.rotation,
                    scale = request.scale,
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
        clearExistingAssignments(feature)
        featureRepository.delete(feature)
    }

    fun findFeatureById(id: String): Feature =
        featureRepository.findByIdOrNull(id) ?: throw CustomException(ErrorCode.NOT_FOUND_FEATURE, id)

    @Transactional
    fun saveFeature(feature: Feature): Feature = featureRepository.save(feature)

    @Transactional(readOnly = true)
    fun findFeatureIdsByAssetId(assetId: Long): List<String> = featureRepository.findByAssetId(assetId).map { it.id }

    @Transactional
    fun assignSomethingToFeature(
        featureId: String,
        assignDto: FeatureAssignDto,
        force: Boolean,
    ) {
        log.debug { "피처에 ${assignDto.type.description} 할당: featureId=$featureId, assignDto=$assignDto" }

        val feature = findFeatureById(featureId)
        // 디바이스 조회 - id로 조회
        if (!force && featureAssignmentRegistry.get(assignDto.type)?.isAssigned(assignDto.id) == true) {
            throw CustomException(
                ErrorCode.ALREADY_ASSIGNED_TARGET,
                assignDto.id,
                assignDto.type.description,
            )
        }
        validateAssign(featureId, force, feature)
        clearExistingAssignments(feature)
        featureAssignmentRegistry.get(assignDto.type)?.assignFeature(assignDto.id, feature)

        log.debug { "${assignDto.type.description}, 피처 관계 설정 완료: deviceId=${assignDto.id}, featureId=$featureId" }
    }

    @Transactional
    fun removeSomethingFromFeature(
        featureId: String,
        assignDto: FeatureAssignDto,
    ) {
        val feature = findFeatureById(featureId)
        featureAssignmentRegistry.get(assignDto.type)?.validateRevoke(assignDto.id, feature.id)
        featureAssignmentRegistry.get(assignDto.type)?.clearFeatureFromTarget(assignDto.id)
        log.debug { "피처에서 ${assignDto.type.description} 제거: featureId=$featureId, deviceId=${assignDto.id}" }
    }

    private fun clearExistingAssignments(feature: Feature) {
        featureAssignmentRegistry.forEach { it.revokeByFeature(feature) }
    }

    private fun validateAssign(
        featureId: String,
        force: Boolean,
        feature: Feature,
    ) {
        if (!force && featureAssignmentRegistry.anyExistsByFeature(feature)) {
            throw CustomException(ErrorCode.ALREADY_FEATURE_ASSIGNED, featureId)
        }
    }
}
