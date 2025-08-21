package com.pluxity.cctv

import com.pluxity.cctv.dto.CctvCreateRequest
import com.pluxity.cctv.dto.CctvResponse
import com.pluxity.cctv.dto.CctvUpdateRequest
import com.pluxity.cctv.dto.toCctvResponse
import com.pluxity.cctv.entity.Cctv
import com.pluxity.cctv.repository.CctvRepository
import com.pluxity.cctv.repository.DeviceCctvRepository
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
) {
    @Transactional
    fun create(request: CctvCreateRequest): String = cctvRepository.save(Cctv(id = request.id, name = request.name, url = request.url)).id

    @Transactional(readOnly = true)
    fun findAll(): List<CctvResponse> {
        val list = cctvRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
        return list.map { it.toCctvResponse() }
    }

    @Transactional(readOnly = true)
    fun getById(id: String): CctvResponse {
        val cctv = findById(id)
        return cctv.toCctvResponse()
    }

    @Transactional
    fun update(
        id: String,
        request: CctvUpdateRequest,
    ) {
        val cctv = findById(id)
        cctv.updateCctv(request)
    }

    @Transactional
    fun delete(id: String) {
        val cctv = findById(id)
        deviceCctvRepository.deleteByCctvIdIn(listOf(cctv.id))
        cctvRepository.deleteById(cctv.id)
    }

    @Transactional(readOnly = true)
    fun findById(id: String): Cctv =
        cctvRepository.findByIdOrNull(id)
            ?: throw CustomException(ErrorCode.NOT_FOUND_CCTV, id)
}
