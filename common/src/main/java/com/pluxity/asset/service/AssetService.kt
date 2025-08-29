package com.pluxity.asset.service

import com.pluxity.asset.dto.AssetCreateRequest
import com.pluxity.asset.dto.AssetResponse
import com.pluxity.asset.dto.AssetUpdateRequest
import com.pluxity.asset.entity.Asset
import com.pluxity.asset.repository.AssetCategoryRepository
import com.pluxity.asset.repository.AssetRepository
import com.pluxity.feature.service.FeatureService
import com.pluxity.file.dto.FileResponse
import com.pluxity.file.service.FileService
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.global.utils.MappingUtils
import com.pluxity.global.utils.SortUtils
import jakarta.validation.Valid
import lombok.extern.slf4j.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.annotation.Validated
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Stream

@Service
@Slf4j
@Validated
class AssetService(
    private val assetRepository: AssetRepository,
    private val assetCategoryRepository: AssetCategoryRepository,
    private val fileService: FileService,
    private val assetCategoryService: AssetCategoryService
) {
    private var featureService: FeatureService? = null

    @Autowired
    fun setFeatureService(@Lazy featureService: FeatureService) {
        this.featureService = featureService
    }

    @Transactional(readOnly = true)
    fun getAsset(id: Long): AssetResponse {
        val asset = findById(id)
        val assetFileResponse = getFileResponse(asset)
        val thumbnailFileResponse = getThumbnailFileResponse(asset)
        return AssetResponse.Companion.from(asset, assetFileResponse, thumbnailFileResponse)
    }

    @get:Transactional(readOnly = true)
    val assets: MutableList<AssetResponse?>
        get() {
            val assets = assetRepository.findAll(SortUtils.getOrderByCreatedAtDesc())
            val fileMap =
                MappingUtils.getFileMapByIds<Asset?>(
                    assets, Function { v: Asset? -> Stream.of<Long?>(v!!.getThumbnailFileId(), v.getFileId()) }, fileService
                )
            return assets.stream()
                .map<AssetResponse?> { asset: Asset? ->
                    AssetResponse.Companion.from(
                        asset, fileMap.get(asset!!.getFileId()), fileMap.get(asset.getThumbnailFileId())
                    )
                }
                .toList()
        }

    @Transactional(readOnly = true)
    fun getAssetsByCategory(categoryId: Long?): MutableList<AssetResponse?> {
        val category = assetCategoryService.findById(categoryId)
        val assets = assetRepository.findByCategory(category)
        val fileMap =
            MappingUtils.getFileMapByIds<Asset?>(
                assets, Function { v: Asset? -> Stream.of<Long?>(v!!.getThumbnailFileId(), v.getFileId()) }, fileService
            )
        return assets.stream()
            .map<AssetResponse?> { asset: Asset? ->
                AssetResponse.Companion.from(
                    asset, fileMap.get(asset!!.getFileId()), fileMap.get(asset.getThumbnailFileId())
                )
            }
            .toList()
    }

    @Transactional(readOnly = true)
    fun getAssetByCode(code: String?): AssetResponse {
        val asset =
            assetRepository
                .findByCode(code)
                .orElseThrow<CustomException?>(Supplier { CustomException(ErrorCode.NOT_FOUND_ASSET_BY_CODE, code) })
        val assetFileResponse = getFileResponse(asset)
        val thumbnailFileResponse = getThumbnailFileResponse(asset)
        return AssetResponse.Companion.from(asset, assetFileResponse, thumbnailFileResponse)
    }

    @Transactional
    fun createAsset(request: @Valid AssetCreateRequest): Long? {
        createValidation(request)

        val asset: Asset = Asset.Companion.create(request)

        if (request.categoryId != null) {
            val category =
                assetCategoryRepository
                    .findById(request.categoryId)
                    .orElseThrow<CustomException?>(
                        Supplier {
                            CustomException(
                                ErrorCode.NOT_FOUND_ASSET_CATEGORY, request.categoryId
                            )
                        })
            asset.updateCategory(category)
        }

        val savedAsset = assetRepository.save<Asset>(asset)

        if (request.fileId != null) {
            val filePath = savedAsset.getAssetFilePath()
            val fileEntity = fileService.finalizeUpload(request.fileId, filePath)
            savedAsset.updateFileEntity(fileEntity)
        }

        if (request.thumbnailFileId != null) {
            val thumbnailPath = savedAsset.getThumbnailFilePath()
            val thumbnailEntity =
                fileService.finalizeUpload(request.thumbnailFileId, thumbnailPath)
            savedAsset.updateThumbnailFileEntity(thumbnailEntity)
        }

        return savedAsset.getId()
    }

    private fun createValidation(request: AssetCreateRequest) {
        assetRepository
            .findByName(request.name)
            .ifPresent(
                Consumer { asset: Asset? ->
                    throw CustomException(ErrorCode.DUPLICATE_ASSET_NAME, request.name)
                })
        assetRepository
            .findByCode(request.code)
            .ifPresent(
                Consumer { asset: Asset? ->
                    throw CustomException(ErrorCode.DUPLICATE_ASSET_CODE, request.code)
                })
    }

    @Transactional
    fun updateAsset(id: Long, request: @Valid AssetUpdateRequest) {
        updateValidation(id, request)
        val asset = findById(id)

        asset.update(request)

        if (request.categoryId != null) {
            val category =
                assetCategoryRepository
                    .findById(request.categoryId)
                    .orElseThrow<CustomException?>(
                        Supplier { CustomException(ErrorCode.NOT_FOUND_ASSET_CATEGORY, request.categoryId) })
            asset.updateCategory(category)
        }

        if (request.fileId != null && request.fileId != asset.getFileId()) {
            val fileEntity =
                fileService.finalizeUpload(request.fileId, asset.getAssetFilePath())
            asset.updateFileEntity(fileEntity)
        }

        if (request.thumbnailFileId != null
            && request.thumbnailFileId != asset.getThumbnailFileId()
        ) {
            val thumbnailEntity =
                fileService.finalizeUpload(request.thumbnailFileId, asset.getThumbnailFilePath())
            asset.updateThumbnailFileEntity(thumbnailEntity)
        }
    }

    private fun updateValidation(id: Long?, request: AssetUpdateRequest) {
        assetRepository
            .findByNameAndIdNot(request.name, id)
            .ifPresent(
                Consumer { asset: Asset? ->
                    throw CustomException(ErrorCode.DUPLICATE_ASSET_NAME, request.name)
                })
        assetRepository
            .findByCodeAndIdNot(request.code, id)
            .ifPresent(
                Consumer { asset: Asset? ->
                    throw CustomException(ErrorCode.DUPLICATE_ASSET_CODE, request.code)
                })
    }

    @Transactional
    fun deleteAsset(id: Long) {
        val asset = findById(id)

        val featureIds = featureService!!.findFeatureIdsByAssetId(id)
        val featureCount = featureIds.size

        AssetService.log.info("에셋 [{}] 삭제 전 연관관계 정리 시작 (연결된 피처: {}개)", id, featureCount)

        // 모든 연관관계 제거
        asset.clearAllRelations()
        AssetService.log.info("에셋 [{}]의 모든 연관관계 제거 완료", id)

        // Feature 삭제
        if (!featureIds.isEmpty()) {
            AssetService.log.info("에셋 [{}]에 연결되었던 피처 [{}]개 삭제 시작", id, featureCount)
            for (featureId in featureIds) {
                featureService!!.deleteFeature(featureId)
            }
            AssetService.log.info("에셋 [{}]에 연결되었던 피처 모두 삭제 완료", id)
        }

        AssetService.log.info("에셋 [{}] 삭제 진행", id)
        assetRepository.delete(asset)
    }

    @Transactional
    fun assignCategory(assetId: Long, categoryId: Long?) {
        val asset = findById(assetId)
        val category = assetCategoryService.findById(categoryId)

        asset.updateCategory(category)
        AssetService.log.info("에셋 [{}]에 카테고리 [{}]가 할당되었습니다.", assetId, categoryId)
    }

    @Transactional
    fun removeCategory(assetId: Long) {
        val asset = findById(assetId)

        if (asset.getCategory() == null) {
            throw CustomException(ErrorCode.NOT_EXIST_ASSET_CATEGORY, assetId)
        }

        asset.updateCategory(null)
        AssetService.log.info("에셋 [{}]에서 카테고리가 제거되었습니다.", assetId)
    }

    @Transactional
    fun findById(id: Long): Asset {
        return assetRepository.findById(id).orElseThrow<CustomException?>(notFoundAsset(id))
    }

    fun getFileResponse(asset: Asset?): FileResponse? {
        if (asset == null) {
            return null
        }

        if (!asset.hasFile()) {
            return null
        }
        return fileService.getFileResponse(asset.getFileId())
    }

    fun getThumbnailFileResponse(asset: Asset?): FileResponse? {
        if (asset == null) {
            return null
        }
        if (!asset.hasThumbnail()) {
            return null
        }
        return fileService.getFileResponse(asset.getThumbnailFileId())
    }

    companion object {
        private fun notFoundAsset(id: Long?): Supplier<CustomException?> {
            return Supplier { CustomException(ErrorCode.NOT_FOUND_ASSET, id) }
        }
    }
}
