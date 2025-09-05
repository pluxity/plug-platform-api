package com.pluxity.facility.category

import com.pluxity.category.service.CategoryService
import com.pluxity.facility.category.dto.FacilityCategoryAllResponse
import com.pluxity.facility.category.dto.FacilityCategoryCreateRequest
import com.pluxity.facility.category.dto.FacilityCategoryResponse
import com.pluxity.facility.category.dto.FacilityCategoryUpdateRequest
import com.pluxity.facility.category.dto.toAllResponse
import com.pluxity.facility.category.dto.toResponse
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.constant.ErrorCode.INVALID_REFERENCE
import com.pluxity.global.exception.CustomException
import com.pluxity.global.utils.MappingUtils
import com.pluxity.global.utils.SortUtils
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FacilityCategoryService(
    private val facilityCategoryRepository: FacilityCategoryRepository,
) : CategoryService<FacilityCategory>() {
    override val repository: JpaRepository<FacilityCategory, Long> = facilityCategoryRepository

    @Transactional
    fun create(request: FacilityCategoryCreateRequest): Long {
        validateDuplicateName(request.name, request.parentId)

        val entity = FacilityCategory(request.name)
        val parent = request.parentId?.let { findById(it) }

        return super.create(entity, parent)
    }

    @Transactional(readOnly = true)
    fun findAll(): FacilityCategoryAllResponse {
        val list =
            facilityCategoryRepository
                .findAll(SortUtils.orderByCreatedAtDesc)
                .map { it.toResponse() }

        val categoryTree =
            MappingUtils.makeCategoryTree(
                list,
                FacilityCategoryResponse::id,
                FacilityCategoryResponse::parentId,
                FacilityCategoryResponse::children,
            )

        return categoryTree.toAllResponse(FacilityCategory("").maxDepth)
    }

    @Transactional
    fun update(
        id: Long,
        request: FacilityCategoryUpdateRequest,
    ) {
        val category = findById(id)

        category
            .takeIf { it.name != request.name || it.parent?.id != request.parentId }
            ?.let {
                validateDuplicateName(request.name, request.parentId, existingId = id)
            }

        super.update(id, request.name, request.parentId)
    }

    @Transactional
    fun delete(id: Long) {
        val facility = findById(id)

        require(facility.children.isEmpty()) {
            throw CustomException(ErrorCode.CATEGORY_HAS_CHILDREN)
        }
        require(facility.facilities.isEmpty()) {
            throw CustomException(ErrorCode.FACILITY_CATEGORY_HAS_FACILITY)
        }
        facilityCategoryRepository.delete(facility)
    }

    private fun validateDuplicateName(
        name: String,
        parentId: Long?,
        existingId: Long? = null,
    ) {
        val foundCategory = facilityCategoryRepository.findByNameAndParentId(name, parentId)
        if (foundCategory != null && foundCategory.id != existingId) {
            throw CustomException(INVALID_REFERENCE, name)
        }
    }
}
