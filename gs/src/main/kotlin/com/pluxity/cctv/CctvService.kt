package com.pluxity.cctv

import com.pluxity.cctv.dto.CctvCreateRequest
import com.pluxity.cctv.dto.CctvResponse
import com.pluxity.cctv.dto.CctvUpdateRequest
import com.pluxity.cctv.dto.toCctvResponse
import com.pluxity.cctv.entity.Cctv
import com.pluxity.cctv.repository.CctvRepository
import com.pluxity.cctv.repository.DeviceCctvRepository
import com.pluxity.feature.entity.Feature
import com.pluxity.feature.service.FeatureAssignment
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CctvService(
    private val cctvRepository: CctvRepository,
    private val deviceCctvRepository: DeviceCctvRepository,
) : FeatureAssignment {
    @Transactional
    fun create(request: CctvCreateRequest): String = cctvRepository.save(Cctv(id = request.id, name = request.name, url = request.url)).id

    @Transactional(readOnly = true)
    fun findAll(): List<CctvResponse> = cctvRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).map { it.toCctvResponse() }

    @Transactional(readOnly = true)
    fun getById(id: String): CctvResponse = findById(id).toCctvResponse()

    @Transactional
    fun update(
        id: String,
        request: CctvUpdateRequest,
    ) = findById(id).updateCctv(request)

    @Transactional
    fun delete(id: String) {
        val cctv = findById(id)
        deviceCctvRepository.deleteByCctvIdIn(listOf(cctv.id))
        cctvRepository.deleteById(cctv.id)
    }

    fun findById(id: String): Cctv =
        cctvRepository.findByIdOrNull(id)
            ?: throw CustomException(ErrorCode.NOT_FOUND_CCTV, id)

    override fun isAssigned(id: String): Boolean = findById(id).feature != null

    override fun existsByFeature(feature: Feature): Boolean = cctvRepository.existsByFeature(feature)

    override fun assignFeature(
        id: String,
        feature: Feature,
    ) = findById(id).changeFeature(feature)

    override fun validateRevoke(
        id: String,
        featureId: String,
    ) {
        val cctv = findById(id)
        val f = cctv.feature ?: throw CustomException(ErrorCode.CCTV_NOT_ASSIGNED, id)
        if (f.id != featureId) {
            throw CustomException(ErrorCode.CCTV_MISMATCH)
        }
    }

    override fun clearFeatureFromTarget(id: String) {
        cctvRepository.findByIdOrNull(id).let { it?.changeFeature(null) }
    }

    override fun revokeByFeature(feature: Feature) = cctvRepository.updateFeatureByFeature(feature)
}
