package com.pluxity.asset.service

import com.pluxity.asset.dto.AssetCreateRequest
import com.pluxity.asset.dto.AssetResponse
import com.pluxity.asset.dto.AssetUpdateRequest
import com.pluxity.asset.entity.Asset
import com.pluxity.asset.entity.AssetCategory
import com.pluxity.asset.repository.AssetCategoryRepository
import com.pluxity.asset.repository.AssetRepository
import com.pluxity.feature.service.FeatureService
import com.pluxity.file.dto.FileResponse
import com.pluxity.file.entity.FileEntity
import com.pluxity.file.service.FileService
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.global.utils.MappingUtils
import com.pluxity.global.utils.SortUtils
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AssetService(
    private val assetRepository: AssetRepository,
    private val assetCategoryRepository: AssetCategoryRepository,
    private val fileService: FileService,
    private val assetCategoryService: AssetCategoryService,
) {
    @Autowired
    fun setFeatureService(
        @Lazy featureService: FeatureService,
    ) {
        this.featureService = featureService
    }

    private lateinit var featureService: FeatureService

    @Transactional(readOnly = true)
    fun getAsset(id: Long): AssetResponse {
        val asset = findById(id)
        val assetFileResponse = getFileResponse(asset)
        val thumbnailFileResponse = getThumbnailFileResponse(asset)
        return AssetResponse.from(asset, assetFileResponse, thumbnailFileResponse)
    }

    @Transactional(readOnly = true)
    fun getAssets(): List<AssetResponse> {
        val assets = assetRepository.findAll(SortUtils.getOrderByCreatedAtDesc())
        val fileMap: Map<Long, FileResponse> =
            MappingUtils.getFileMapByIds(
                assets,
                { v ->
                    java.util.stream.Stream
                        .of(v.thumbnailFileId, v.fileId)
                },
                fileService,
            )
        return assets.map { asset ->
            AssetResponse.from(asset, fileMap[asset.fileId], fileMap[asset.thumbnailFileId])
        }
    }

    @Transactional(readOnly = true)
    fun getAssetsByCategory(categoryId: Long): List<AssetResponse> {
        val category: AssetCategory = assetCategoryService.findById(categoryId)
        val assets = assetRepository.findByCategory(category)
        val fileMap: Map<Long, FileResponse> =
            MappingUtils.getFileMapByIds(
                assets,
                { v ->
                    java.util.stream.Stream
                        .of(v.thumbnailFileId, v.fileId)
                },
                fileService,
            )
        return assets.map { asset ->
            AssetResponse.from(asset, fileMap[asset.fileId], fileMap[asset.thumbnailFileId])
        }
    }

    @Transactional(readOnly = true)
    fun getAssetByCode(code: String): AssetResponse {
        val asset =
            assetRepository
                .findByCode(code)
                .orElseThrow { CustomException(ErrorCode.NOT_FOUND_ASSET_BY_CODE, code) }
        val assetFileResponse = getFileResponse(asset)
        val thumbnailFileResponse = getThumbnailFileResponse(asset)
        return AssetResponse.from(asset, assetFileResponse, thumbnailFileResponse)
    }

    @Transactional
    fun createAsset(
        @Valid request: AssetCreateRequest,
    ): Long {
        createValidation(request)

        val asset = Asset.create(request)

        if (request.categoryId != null) {
            val category =
                assetCategoryRepository
                    .findById(request.categoryId)
                    .orElseThrow { CustomException(ErrorCode.NOT_FOUND_ASSET_CATEGORY, request.categoryId) }
            asset.updateCategory(category)
        }

        val savedAsset = assetRepository.save(asset)

        if (request.fileId != null) {
            val filePath = savedAsset.getAssetFilePath()
            val fileEntity: FileEntity = fileService.finalizeUpload(request.fileId, filePath)
            savedAsset.updateFileEntity(fileEntity)
        }

        if (request.thumbnailFileId != null) {
            val thumbnailPath = savedAsset.getThumbnailFilePath()
            val thumbnailEntity: FileEntity = fileService.finalizeUpload(request.thumbnailFileId, thumbnailPath)
            savedAsset.updateThumbnailFileEntity(thumbnailEntity)
        }

        return savedAsset.id!!
    }

    private fun createValidation(request: AssetCreateRequest) {
        assetRepository.findByName(request.name).ifPresent {
            throw CustomException(ErrorCode.DUPLICATE_ASSET_NAME, request.name)
        }
        assetRepository.findByCode(request.code).ifPresent {
            throw CustomException(ErrorCode.DUPLICATE_ASSET_CODE, request.code)
        }
    }

    @Transactional
    fun updateAsset(
        id: Long,
        @Valid request: AssetUpdateRequest,
    ) {
        updateValidation(id, request)
        val asset = findById(id)

        asset.update(request)

        if (request.categoryId != null) {
            val category =
                assetCategoryRepository
                    .findById(request.categoryId)
                    .orElseThrow { CustomException(ErrorCode.NOT_FOUND_ASSET_CATEGORY, request.categoryId) }
            asset.updateCategory(category)
        }

        if (request.fileId != null && request.fileId != asset.fileId) {
            val fileEntity: FileEntity = fileService.finalizeUpload(request.fileId, asset.getAssetFilePath())
            asset.updateFileEntity(fileEntity)
        }

        if (request.thumbnailFileId != null && request.thumbnailFileId != asset.thumbnailFileId) {
            val thumbnailEntity: FileEntity =
                fileService.finalizeUpload(request.thumbnailFileId, asset.getThumbnailFilePath())
            asset.updateThumbnailFileEntity(thumbnailEntity)
        }
    }

    private fun updateValidation(
        id: Long,
        request: AssetUpdateRequest,
    ) {
        assetRepository.findByNameAndIdNot(request.name, id).ifPresent {
            throw CustomException(ErrorCode.DUPLICATE_ASSET_NAME, request.name)
        }
        assetRepository.findByCodeAndIdNot(request.code, id).ifPresent {
            throw CustomException(ErrorCode.DUPLICATE_ASSET_CODE, request.code)
        }
    }

    @Transactional
    fun deleteAsset(id: Long) {
        val asset = findById(id)
        val featureIds: List<String> = featureService.findFeatureIdsByAssetId(id)
        val featureCount = featureIds.size

        // 모든 연관관계 제거
        asset.clearAllRelations()

        // Feature 삭제
        if (featureIds.isNotEmpty()) {
            for (featureId in featureIds) {
                featureService.deleteFeature(featureId)
            }
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
        asset.updateCategory(category)
    }

    @Transactional
    fun removeCategory(assetId: Long) {
        val asset = findById(assetId)
        if (asset.category == null) {
            throw CustomException(ErrorCode.NOT_EXIST_ASSET_CATEGORY, assetId)
        }
        asset.updateCategory(null)
    }

    @Transactional
    fun findById(id: Long): Asset = assetRepository.findById(id).orElseThrow { notFoundAsset(id) }

    fun getFileResponse(asset: Asset?): FileResponse? {
        if (asset == null) return null
        if (!asset.hasFile()) return null
        return fileService.getFileResponse(asset.fileId)
    }

    fun getThumbnailFileResponse(asset: Asset?): FileResponse? {
        if (asset == null) return null
        if (!asset.hasThumbnail()) return null
        return fileService.getFileResponse(asset.thumbnailFileId)
    }

    private fun notFoundAsset(id: Long): CustomException = CustomException(ErrorCode.NOT_FOUND_ASSET, id)
}
