package com.pluxity.asset.service

import com.pluxity.asset.repository.AssetRepository
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.function.Supplier

@Service
@RequiredArgsConstructor
class AssetValidator {
    private val assetRepository: AssetRepository? = null

    @Transactional(readOnly = true)
    fun validateAssetId(id: Long) {
        assetRepository!!.findById(id).orElseThrow<CustomException?>(Supplier { CustomException(ErrorCode.NOT_FOUND_ASSET, id) })
    }
}
