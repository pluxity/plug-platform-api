package com.pluxity.asset.service

import com.pluxity.asset.dto.AssetCreateRequest
import com.pluxity.asset.dto.AssetResponse
import com.pluxity.asset.dto.AssetUpdateRequest
import com.pluxity.asset.dto.toResponse
import com.pluxity.asset.entity.Asset
import com.pluxity.asset.repository.AssetCategoryRepository
import com.pluxity.asset.repository.AssetRepository
import com.pluxity.feature.service.FeatureService
import com.pluxity.file.dto.FileResponse
import com.pluxity.file.service.FileService
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.global.utils.SortUtils
import com.pluxity.global.utils.getFileMapByIds
import jakarta.validation.Valid
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AssetService(
    private val assetRepository: AssetRepository,
    private val assetCategoryRepository: AssetCategoryRepository,
    private val fileService: FileService,
    private val assetCategoryService: AssetCategoryService,
    private val featureService: FeatureService,
) {
    @Transactional(readOnly = true)
    fun getAsset(id: Long): AssetResponse {
        val asset = findById(id)
        return asset.toResponse(
            file = getFileResponse(asset),
            thumbnailFile = getThumbnailFileResponse(asset),
        )
    }

    @Transactional(readOnly = true)
    fun getAssets(): List<AssetResponse> {
        val assets = assetRepository.findAll(SortUtils.orderByCreatedAtDesc)
        val fileMap =
            fileService.getFileMapByIds(assets) {
                listOfNotNull(it.fileId, it.thumbnailFileId)
            }

        return assets.map { asset ->
            asset.toResponse(
                file = fileMap[asset.fileId],
                thumbnailFile = fileMap[asset.thumbnailFileId],
            )
        }
    }

    @Transactional(readOnly = true)
    fun getAssetsByCategory(categoryId: Long): List<AssetResponse> {
        val category = assetCategoryService.findById(categoryId)
        val assets = assetRepository.findByCategory(category)
        val fileMap =
            fileService.getFileMapByIds(assets) {
                listOfNotNull(it.fileId, it.thumbnailFileId)
            }

        return assets.map { asset ->
            asset.toResponse(
                file = fileMap[asset.fileId],
                thumbnailFile = fileMap[asset.thumbnailFileId],
            )
        }
    }

    @Transactional(readOnly = true)
    fun getAssetByCode(code: String): AssetResponse {
        val asset =
            assetRepository
                .findByCode(code)
                ?: throw CustomException(ErrorCode.NOT_FOUND_ASSET_BY_CODE, code)

        return asset.toResponse(
            file = getFileResponse(asset),
            thumbnailFile = getThumbnailFileResponse(asset),
        )
    }

    @Transactional
    fun createAsset(
        @Valid request: AssetCreateRequest,
    ): Long {
        if (assetRepository.findByName(request.name) != null) {
            throw CustomException(ErrorCode.DUPLICATE_ASSET_NAME, request.name)
        }

        if (assetRepository.findByCode(request.code) != null) {
            throw CustomException(ErrorCode.DUPLICATE_ASSET_CODE, request.code)
        }

        val asset = Asset.create(request)

        request.categoryId?.let { categoryId ->
            val category =
                assetCategoryRepository
                    .findByIdOrNull(categoryId)
                    ?: throw CustomException(ErrorCode.NOT_FOUND_ASSET_CATEGORY, categoryId)
            asset.assignCategory(category)
        }

        val savedAsset = assetRepository.save(asset)

        request.fileId?.let { fileId ->
            val fileEntity = fileService.finalizeUpload(fileId, savedAsset.getAssetFilePath())
            savedAsset.updateFileEntity(fileEntity)
        }

        request.thumbnailFileId?.let { thumbnailFileId ->
            val thumbnailEntity = fileService.finalizeUpload(thumbnailFileId, savedAsset.getThumbnailFilePath())
            savedAsset.updateThumbnailFileEntity(thumbnailEntity)
        }

        return requireNotNull(savedAsset.id)
    }

    @Transactional
    fun updateAsset(
        id: Long,
        @Valid request: AssetUpdateRequest,
    ) {
        if (assetRepository.findByNameAndIdNot(request.name, id) != null) {
            throw CustomException(ErrorCode.DUPLICATE_ASSET_NAME, request.name)
        }

        if (assetRepository.findByCodeAndIdNot(request.code, id) != null) {
            throw CustomException(ErrorCode.DUPLICATE_ASSET_CODE, request.code)
        }

        val asset = findById(id)
        asset.update(request)

        request.categoryId?.let { categoryId ->
            val category =
                assetCategoryRepository
                    .findByIdOrNull(categoryId)
                    ?: throw CustomException(ErrorCode.NOT_FOUND_ASSET_CATEGORY, categoryId)
            asset.assignCategory(category)
        }

        request.fileId?.takeIf { it != asset.fileId }?.let { fileId ->
            val fileEntity = fileService.finalizeUpload(fileId, asset.getAssetFilePath())
            asset.updateFileEntity(fileEntity)
        }

        request.thumbnailFileId?.takeIf { it != asset.thumbnailFileId }?.let { thumbnailFileId ->
            val thumbnailEntity = fileService.finalizeUpload(thumbnailFileId, asset.getThumbnailFilePath())
            asset.updateThumbnailFileEntity(thumbnailEntity)
        }
    }

    @Transactional
    fun deleteAsset(id: Long) {
        val asset = findById(id)
        val featureIds = featureService.findFeatureIdsByAssetId(id)

        asset.clearAllRelations()

        featureIds.forEach { featureId ->
            featureService.deleteFeature(featureId)
        }

        assetRepository.delete(asset)
    }

    @Transactional
    fun assignCategory(
        assetId: Long,
        categoryId: Long,
    ) {
        val asset = findById(assetId)
        val category = assetCategoryService.findById(categoryId)
        asset.assignCategory(category)
    }

    @Transactional
    fun removeCategory(assetId: Long) {
        val asset = findById(assetId)
        asset.removeCategory()
    }

    @Transactional(readOnly = true)
    fun findById(id: Long): Asset = assetRepository.findByIdOrNull(id) ?: throw notFoundAsset(id)

    fun getFileResponse(asset: Asset?): FileResponse? = asset?.takeIf { it.hasFile() }?.let { fileService.getFileResponse(it.fileId) }

    fun getThumbnailFileResponse(asset: Asset?): FileResponse? =
        asset?.takeIf { it.hasThumbnail() }?.let { fileService.getFileResponse(it.thumbnailFileId) }

    private fun notFoundAsset(id: Long): CustomException = CustomException(ErrorCode.NOT_FOUND_ASSET, id)
}
