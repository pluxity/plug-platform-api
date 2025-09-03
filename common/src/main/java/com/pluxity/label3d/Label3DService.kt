package com.pluxity.label3d

import com.pluxity.facility.FacilityService
import com.pluxity.feature.dto.FeatureUpdateRequest
import com.pluxity.feature.entity.Feature
import com.pluxity.feature.service.FeatureService
import com.pluxity.global.utils.SortUtils
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class Label3DService(
    private val label3DRepository: Label3DRepository,
    private val featureService: FeatureService,
    private val facilityService: FacilityService,
) {
    @Transactional
    fun createLabel3D(request: Label3DCreateRequest): Label3DResponse {
        val feature =
            featureService.saveFeature(
                Feature(
                    id = request.id,
                    facility = facilityService.findById(request.facilityId),
                    floorId = request.floorId,
                    position = request.position,
                    rotation = request.rotation,
                    scale = request.scale,
                ),
            )

        val label3D = Label3D(feature = feature, displayText = request.displayText)

        val savedLabel3D = label3DRepository.save(label3D)
        return savedLabel3D.toLabel3DResponse()
    }

    @Transactional(readOnly = true)
    fun getLabel3DById(id: String): Label3DResponse = findLabel3DById(id).toLabel3DResponse()

    @Transactional(readOnly = true)
    fun getAllLabel3Ds(): List<Label3DResponse> =
        label3DRepository
            .findAll(SortUtils.orderByCreatedAtDesc)
            .map { it.toLabel3DResponse() }

    @Transactional(readOnly = true)
    fun getLabel3DsByFacilityId(facilityId: Long): List<Label3DResponse> =
        label3DRepository
            .findAllByFacilityId(facilityId)
            .map { it.toLabel3DResponse() }

    @Transactional
    fun updateLabel3D(
        id: String,
        request: Label3DUpdateRequest,
    ) {
        val label3D = findLabel3DById(id)

        val featureUpdateRequest =
            FeatureUpdateRequest(request.position, request.rotation, request.scale)
        label3D.feature.update(featureUpdateRequest)
    }

    @Transactional
    fun deleteLabel3D(id: String) {
        val label3D = findLabel3DById(id)
        featureService.deleteFeature(label3D.feature.id!!)
        label3DRepository.delete(label3D)
    }

    fun findLabel3DById(id: String): Label3D =
        label3DRepository
            .findByIdOrNull(id)
            ?: throw EntityNotFoundException("Label3D not found with id: $id")
}
