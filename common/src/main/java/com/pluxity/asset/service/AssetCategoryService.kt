package com.pluxity.asset.service

import com.pluxity.asset.dto.AssetCategoryCreateRequest
import com.pluxity.asset.dto.AssetCategoryDepthResponse
import com.pluxity.asset.dto.AssetCategoryResponse
import com.pluxity.asset.dto.AssetCategoryUpdateRequest
import com.pluxity.asset.entity.AssetCategory
import com.pluxity.asset.repository.AssetCategoryRepository
import com.pluxity.category.service.CategoryService
import com.pluxity.file.service.FileService
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.global.utils.MappingUtils
import com.pluxity.global.utils.SortUtils
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.function.Function
import java.util.stream.Stream

@Service
@RequiredArgsConstructor
@Slf4j
class AssetCategoryService : CategoryService<AssetCategory>() {
    private val assetCategoryRepository: AssetCategoryRepository? = null
    private val fileService: FileService? = null

    override fun getRepository(): JpaRepository<AssetCategory?, Long?> {
        return assetCategoryRepository!!
    }

    @get:Transactional(readOnly = true)
    val allCategories: MutableList<AssetCategoryResponse?>
        get() {
            val allCategories =
                assetCategoryRepository!!.findAll(SortUtils.getOrderByCreatedAtDesc())
            val fileMap =
                MappingUtils.getFileMapByIds<AssetCategory?>(
                    allCategories,
                    Function { v: AssetCategory? -> Stream.of<Long?>(v!!.getIconFileId()) },
                    fileService
                )
            val list =
                allCategories.stream()
                    .map<AssetCategoryResponse?> { v: AssetCategory? -> AssetCategoryResponse.Companion.from(v, fileMap.get(v!!.getIconFileId())) }
                    .toList()

            return MappingUtils.makeCategoryTree<AssetCategoryResponse?>(
                list,
                AssetCategoryResponse::id,
                AssetCategoryResponse::parentId,
                AssetCategoryResponse::children
            )
        }

    @Transactional(readOnly = true)
    fun getChildCategories(parentId: Long?): MutableList<AssetCategoryResponse?> {
        val childCategories = assetCategoryRepository!!.findByParentId(parentId)
        return childCategories.stream().map<AssetCategoryResponse?> { category: AssetCategory? -> this.createAssetCategoryResponseWithoutChildren(category!!) }.toList()
    }

    private fun createAssetCategoryResponseWithoutChildren(category: AssetCategory): AssetCategoryResponse {
        return AssetCategoryResponse.Companion.fromWithoutChildren(
            category,
            if (category.getIconFileId() != null)
                fileService!!.getFileResponse(category.getIconFileId())
            else
                null
        )
    }

    @Transactional
    fun createAssetCategory(request: AssetCategoryCreateRequest): Long? {
        validateCodeUniqueness(request.code)
        val category =
            AssetCategory.builder()
                .name(request.name)
                .code(request.code)
                .iconFileId(request.thumbnailFileId)
                .build()
        val parent = MappingUtils.findByIdIfExists<Long?, AssetCategory?>(request.parentId, Function { id: Long? -> super.findById(id) })

        if (request.thumbnailFileId != null) {
            category.updateIconFileId(request.thumbnailFileId)
            fileService!!.finalizeUpload(
                request.thumbnailFileId, ASSET_CATEGORIES + category.getId() + "/"
            )
        }

        return super.create(category, parent)
    }

    @Transactional
    fun updateAssetCategory(id: Long?, request: AssetCategoryUpdateRequest) {
        val category = findById(id)
        if (category.getCode() != request.code) {
            validateCodeUniqueness(request.code)
        }
        super.update(id, request.name, request.parentId)
        category.updateCode(request.code)
        category.updateIconFileId(request.thumbnailFileId)

        if (request.thumbnailFileId != null) {
            fileService!!.finalizeUpload(
                request.thumbnailFileId, ASSET_CATEGORIES + category.getId() + "/"
            )
        }
    }

    @Transactional
    fun deleteAssetCategory(id: Long?) {
        val category = findById(id)

        if (!category.getAssets().isEmpty()) {
            throw CustomException(ErrorCode.ASSET_CATEGORY_HAS_ASSET)
        }

        if (!category.getChildren().isEmpty()) {
            throw CustomException(ErrorCode.CATEGORY_HAS_CHILDREN)
        }

        assetCategoryRepository!!.delete(category)
    }

    private fun validateCodeUniqueness(code: String?) {
        if (assetCategoryRepository!!.existsByCode(code)) {
            throw CustomException(ErrorCode.DUPLICATE_ASSET_CATEGORY_CODE, code)
        }
    }

    val categoryDepth: AssetCategoryDepthResponse
        get() = AssetCategoryDepthResponse(AssetCategory.builder().build().getMaxDepth())

    companion object {
        const val ASSET_CATEGORIES: String = "asset-categories/"
    }
}
