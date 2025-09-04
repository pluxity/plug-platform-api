package com.pluxity.asset.service

import com.pluxity.asset.dto.AssetCategoryCreateRequest
import com.pluxity.asset.dto.AssetCategoryDepthResponse
import com.pluxity.asset.dto.AssetCategoryResponse
import com.pluxity.asset.dto.AssetCategoryUpdateRequest
import com.pluxity.asset.dto.toResponse
import com.pluxity.asset.entity.AssetCategory
import com.pluxity.asset.repository.AssetCategoryRepository
import com.pluxity.category.service.CategoryService
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
    override val repository: JpaRepository<AssetCategory, Long>,
) : CategoryService<AssetCategory>() {
    @Transactional(readOnly = true)
    fun getAllCategories(): List<AssetCategoryResponse> {
        val allCategories = assetCategoryRepository.findAll(SortUtils.orderByCreatedAtDesc)
        if (allCategories.isEmpty()) return emptyList()

        val iconFileIds = allCategories.mapNotNull { it.iconFileId }
        val fileMap = fileService.getFiles(iconFileIds).associateBy { it.id }
        val flatList =
            allCategories.map { category ->
                category.toResponse(
                    includeChildren = false,
                    thumbnailFile = fileMap[category.iconFileId],
                )
            }

        return MappingUtils.makeCategoryTree(
            flatList,
            AssetCategoryResponse::id,
            AssetCategoryResponse::parentId,
            AssetCategoryResponse::children,
        )
    }

    @Transactional(readOnly = true)
    fun getChildCategories(parentId: Long): List<AssetCategoryResponse> {
        val childCategories = assetCategoryRepository.findByParentId(parentId)
        return childCategories.map { category ->
            category.toResponse(
                includeChildren = false,
                thumbnailFile = fileService.getFileResponse(category.iconFileId),
            )
        }
    }

    @Transactional
    fun createAssetCategory(request: AssetCategoryCreateRequest): Long {
        validateCodeUniqueness(request.code)

        val parent = request.parentId?.let { id: Long -> super.findById(id) }

        val category =
            AssetCategory(
                code = request.code,
                iconFileId = request.thumbnailFileId,
                categoryName = request.name,
            ).apply {
                assignToParent(parent)
            }

        assetCategoryRepository.save(category)

        request.thumbnailFileId?.let {
            fileService.finalizeUpload(it, "${ASSET_CATEGORIES}${category.id}")
        }

        return category.id!!
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

        request.thumbnailFileId?.let {
            fileService.finalizeUpload(it, "${ASSET_CATEGORIES}${category.id}")
        }
    }

    @Transactional
    fun deleteAssetCategory(id: Long) {
        val category = findById(id)

        require(category.assets.isEmpty()) {
            throw CustomException(com.pluxity.global.constant.ErrorCode.ASSET_CATEGORY_HAS_ASSET)
        }

        require(category.children.isEmpty()) {
            throw CustomException(com.pluxity.global.constant.ErrorCode.CATEGORY_HAS_CHILDREN)
        }

        assetCategoryRepository.delete(category)
    }

    private fun validateCodeUniqueness(code: String) {
        if (assetCategoryRepository.existsByCode(code)) {
            throw CustomException(com.pluxity.global.constant.ErrorCode.DUPLICATE_ASSET_CATEGORY_CODE, code)
        }
    }

    fun getCategoryDepth(): AssetCategoryDepthResponse = AssetCategoryDepthResponse(AssetCategory.MAX_DEPTH)

    companion object {
        private const val ASSET_CATEGORIES: String = "asset-categories/"
    }
}
