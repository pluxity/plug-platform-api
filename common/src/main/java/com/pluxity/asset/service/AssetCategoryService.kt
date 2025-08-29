package com.pluxity.asset.service

import com.pluxity.asset.dto.AssetCategoryCreateRequest
import com.pluxity.asset.dto.AssetCategoryDepthResponse
import com.pluxity.asset.dto.AssetCategoryResponse
import com.pluxity.asset.dto.AssetCategoryUpdateRequest
import com.pluxity.asset.entity.AssetCategory
import com.pluxity.asset.repository.AssetCategoryRepository
import com.pluxity.category.service.CategoryService
import com.pluxity.file.dto.FileResponse
import com.pluxity.file.service.FileService
import com.pluxity.global.exception.CustomException
import com.pluxity.global.utils.MappingUtils
import com.pluxity.global.utils.SortUtils
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AssetCategoryService(
    private val assetCategoryRepository: AssetCategoryRepository,
    private val fileService: FileService,
) : CategoryService<AssetCategory>() {
    companion object {
        const val ASSET_CATEGORIES: String = "asset-categories/"
    }

    override fun getRepository(): JpaRepository<AssetCategory, Long> = assetCategoryRepository

    @Transactional(readOnly = true)
    fun getAllCategories(): List<AssetCategoryResponse> {
        val allCategories = assetCategoryRepository.findAll(SortUtils.getOrderByCreatedAtDesc())
        val fileMap: Map<Long, FileResponse> =
            MappingUtils.getFileMapByIds(
                allCategories,
                { v ->
                    java.util.stream.Stream
                        .of(v.iconFileId)
                },
                fileService,
            )

        val list =
            allCategories.map { category ->
                AssetCategoryResponse.from(category, fileMap[category.iconFileId])
            }

        return MappingUtils.makeCategoryTree(
            list,
            AssetCategoryResponse::id,
            AssetCategoryResponse::parentId,
            AssetCategoryResponse::children,
        )
    }

    @Transactional(readOnly = true)
    fun getChildCategories(parentId: Long): List<AssetCategoryResponse> {
        val childCategories = assetCategoryRepository.findByParentId(parentId)
        return childCategories.map { createAssetCategoryResponseWithoutChildren(it) }
    }

    private fun createAssetCategoryResponseWithoutChildren(category: AssetCategory): AssetCategoryResponse =
        AssetCategoryResponse.fromWithoutChildren(
            category,
            category.iconFileId?.let { fileService.getFileResponse(it) },
        )

    @Transactional
    fun createAssetCategory(request: AssetCategoryCreateRequest): Long {
        validateCodeUniqueness(request.code)
        val category =
            AssetCategory
                .builder()
                .name(request.name)
                .code(request.code)
                .iconFileId(request.thumbnailFileId)
                .build()

        val parent = MappingUtils.findByIdIfExists(request.parentId) { id: Long -> super.findById(id) }

        if (request.thumbnailFileId != null) {
            category.updateIconFileId(request.thumbnailFileId)
            fileService.finalizeUpload(request.thumbnailFileId, "$ASSET_CATEGORIES${category.id}/")
        }

        return super.create(category, parent)
    }

    @Transactional
    fun updateAssetCategory(
        id: Long,
        request: AssetCategoryUpdateRequest,
    ) {
        val category = findById(id)
        if (category.code != request.code) {
            validateCodeUniqueness(request.code)
        }
        super.update(id, request.name, request.parentId)
        category.updateCode(request.code)
        category.updateIconFileId(request.thumbnailFileId)

        if (request.thumbnailFileId != null) {
            fileService.finalizeUpload(request.thumbnailFileId, "$ASSET_CATEGORIES${category.id}/")
        }
    }

    @Transactional
    fun deleteAssetCategory(id: Long) {
        val category = findById(id)

        if (category.assets.isNotEmpty()) {
            throw CustomException(com.pluxity.global.constant.ErrorCode.ASSET_CATEGORY_HAS_ASSET)
        }

        if (category.children.isNotEmpty()) {
            throw CustomException(com.pluxity.global.constant.ErrorCode.CATEGORY_HAS_CHILDREN)
        }

        assetCategoryRepository.delete(category)
    }

    private fun validateCodeUniqueness(code: String) {
        if (assetCategoryRepository.existsByCode(code)) {
            throw CustomException(com.pluxity.global.constant.ErrorCode.DUPLICATE_ASSET_CATEGORY_CODE, code)
        }
    }

    fun getCategoryDepth(): AssetCategoryDepthResponse = AssetCategoryDepthResponse(AssetCategory.builder().build().maxDepth)
}
