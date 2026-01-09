package com.pluxity.cctv

import com.pluxity.cctv.dto.CctvCreateRequest
import com.pluxity.cctv.dto.CctvResponse
import com.pluxity.cctv.dto.CctvUpdateRequest
import com.pluxity.cctv.dto.toCctvResponse
import com.pluxity.cctv.entity.Cctv
import com.pluxity.cctv.repository.CctvRepository
import com.pluxity.cctv.repository.DeviceCctvRepository
import com.pluxity.feature.entity.Feature
import com.pluxity.feature.service.FeatureAssignType
import com.pluxity.feature.service.FeatureAssignment
import com.pluxity.global.annotation.CheckPermission
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.global.utils.UUIDUtils
import com.pluxity.permission.PermissionLevel
import com.pluxity.permission.ResourceType
import com.pluxity.properties.MediaMtxProperties
import com.pluxity.user.entity.PermissionAction
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Service
class CctvService(
    private val cctvRepository: CctvRepository,
    private val deviceCctvRepository: DeviceCctvRepository,
    private val mediaMtxService: MediaMtxService,
    private val mediaMtxProperties: MediaMtxProperties,
) : FeatureAssignment {
    override val type: FeatureAssignType = FeatureAssignType.CCTV

    @PostConstruct
    fun init() {
        try {
            synchronizeCctv()
        } catch (e: Exception) {
            log.error { "CctvService 초기화 중 미디어서버 동기화 실패 (서버 상태를 확인하세요): ${e.message}" }
        }
    }

    @Transactional
    @CheckPermission(action = PermissionAction.CREATE, resourceType = ResourceType.CCTV, level = PermissionLevel.WRITE)
    fun create(request: CctvCreateRequest): String {
        var mtxName: String? = null
        request.url?.let {
            mtxName = UUIDUtils.generateShortUUID()
            mediaMtxService.addPath(mtxName, request.url)
        }
        return cctvRepository.save(Cctv(id = request.id, name = request.name, url = request.url, mtxName = mtxName)).id
    }

    @Transactional(readOnly = true)
    fun findAll(facilityId: Long? = null): List<CctvResponse> {
        val list = cctvRepository.findAllByFacilityIdIfPresent(facilityId)
        return list.map { it.toCctvResponse(mediaMtxProperties.viewUrl) }
    }

    @Transactional(readOnly = true)
    fun getById(id: String): CctvResponse = findById(id).toCctvResponse(mediaMtxProperties.viewUrl)

    @Transactional
    @CheckPermission(action = PermissionAction.UPDATE, resourceType = ResourceType.CCTV, level = PermissionLevel.WRITE)
    fun update(
        id: String,
        request: CctvUpdateRequest,
    ) {
        val cctv = findById(id)
        if (cctv.url != request.url) {
            cctv.mtxName?.let {
                mediaMtxService.deletePath(it)
            }
            var mtxName: String? = null
            request.url?.let {
                mtxName = UUIDUtils.generateShortUUID()
                mediaMtxService.addPath(mtxName, request.url)
            }
            cctv.updateMtxName(mtxName)
        }
        cctv.updateCctv(request)
    }

    @Transactional
    @CheckPermission(action = PermissionAction.DELETE, resourceType = ResourceType.CCTV, level = PermissionLevel.ADMIN)
    fun delete(id: String) {
        val cctv = findById(id)
        cctv.mtxName?.let {
            mediaMtxService.deletePath(it)
        }
        deviceCctvRepository.deleteByCctvIdIn(listOf(cctv.id))
        cctvRepository.deleteById(cctv.id)
    }

    fun findById(id: String): Cctv =
        cctvRepository.findByIdOrNullCustom(id)
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

    fun synchronizeCctv() {
        val mtxList = mediaMtxService.getAllPath().map { it.name }
        val cctvList = cctvRepository.findAll()
        val dbList = cctvList.mapNotNull { it.mtxName }

        mtxList.minus(dbList.toSet()).forEach {
            mediaMtxService.deletePath(it)
        }

        dbList.minus(mtxList.toSet()).forEach {
            mediaMtxService.addPath(it, cctvList.first { cctv -> cctv.mtxName == it }.url ?: return@forEach)
        }
    }
}
