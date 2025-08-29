package com.pluxity.asset.service

import com.pluxity.asset.repository.AssetRepository
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AssetValidator(
    private val assetRepository: AssetRepository,
) {
    @Transactional(readOnly = true)
    fun validateAssetId(id: Long) {
        assetRepository.findById(id).orElseThrow { CustomException(ErrorCode.NOT_FOUND_ASSET, id) }
    }
}
