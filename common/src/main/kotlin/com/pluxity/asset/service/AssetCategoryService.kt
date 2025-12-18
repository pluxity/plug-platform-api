package com.pluxity.asset.service

import com.pluxity.asset.dto.AssetCategoryCreateRequest
import com.pluxity.asset.dto.AssetCategoryResponse
import com.pluxity.asset.dto.AssetCategoryUpdateRequest
import com.pluxity.asset.dto.toAssetCategoryResponseWithChildren
import com.pluxity.asset.dto.toResponse
import com.pluxity.asset.entity.AssetCategory
import com.pluxity.asset.repository.AssetCategoryRepository
import com.pluxity.category.dto.CategoryDepthResponse
import com.pluxity.category.service.CategoryService
import com.pluxity.file.extensions.getFileMapById
import com.pluxity.file.service.FileService
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
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

        val fileMap = fileService.getFileMapById(allCategories) { it.iconFileId }

        return allCategories.filter { it.parent == null }.map { it.toAssetCategoryResponseWithChildren(fileMap) }
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

        return category.requiredId
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

        if (category.iconFileId != request.thumbnailFileId) {
            request.thumbnailFileId?.let { thumbnailId ->
                category.updateIconFileId(thumbnailId)
                fileService.finalizeUpload(thumbnailId, "${ASSET_CATEGORIES}${category.id}")
            } ?: category.updateIconFileId(null)
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

    fun getCategoryDepth(): CategoryDepthResponse = CategoryDepthResponse(AssetCategory.MAX_DEPTH)

    companion object {
        private const val ASSET_CATEGORIES: String = "asset-categories/"
    }
}
