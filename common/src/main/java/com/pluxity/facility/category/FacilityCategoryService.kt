package com.pluxity.facility.category

import com.pluxity.category.service.CategoryService
import com.pluxity.facility.category.dto.FacilityCategoryAllResponse
import com.pluxity.facility.category.dto.FacilityCategoryCreateRequest
import com.pluxity.facility.category.dto.FacilityCategoryResponse
import com.pluxity.facility.category.dto.FacilityCategoryUpdateRequest
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.global.utils.MappingUtils
import com.pluxity.global.utils.SortUtils
import lombok.RequiredArgsConstructor
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.function.Consumer
import java.util.function.Function
import java.util.stream.Collectors

@Service
@RequiredArgsConstructor
class FacilityCategoryService : CategoryService<FacilityCategory>() {
    private val repository: FacilityCategoryRepository? = null

    override fun getRepository(): JpaRepository<FacilityCategory?, Long?> {
        return repository!!
    }

    @Transactional
    fun create(request: FacilityCategoryCreateRequest): Long {
        if (request.parentId != null) {
            repository!!
                .findByNameAndParentId(request.name, request.parentId)
                .ifPresent(
                    Consumer { existingCategory: FacilityCategory? ->
                        throw CustomException(ErrorCode.INVALID_REFERENCE, request.name)
                    })
        }
        val entity = FacilityCategory.builder().name(request.name).build()
        val parent = MappingUtils.findByIdIfExists<Long?, FacilityCategory>(request.parentId, Function { id: Long? -> super.findById(id!!) })

        return super.create(entity, parent)
    }

    @Transactional(readOnly = true)
    fun findAll(): FacilityCategoryAllResponse {
        val list =
            repository!!.findAll(SortUtils.getOrderByCreatedAtDesc()).stream()
                .map<FacilityCategoryResponse?> { category: FacilityCategory? -> FacilityCategoryResponse.Companion.from(category) }
                .collect(Collectors.toList())

        return FacilityCategoryAllResponse.Companion.of(
            FacilityCategory.builder().build().maxDepth,
            MappingUtils.makeCategoryTree<FacilityCategoryResponse?>(
                list,
                FacilityCategoryResponse::id,
                FacilityCategoryResponse::parentId,
                FacilityCategoryResponse::children
            )
        )
    }

    @Transactional
    fun update(id: Long, request: FacilityCategoryUpdateRequest) {
        super.update(id, request.name, request.parentId)
    }

    @Transactional
    fun delete(id: Long) {
        val facility = findById(id)

        if (!facility.children.isEmpty()) {
            throw CustomException(ErrorCode.CATEGORY_HAS_CHILDREN)
        }

        if (!facility.getFacilities().isEmpty()) {
            throw CustomException(ErrorCode.FACILITY_CATEGORY_HAS_FACILITY)
        }

        repository!!.delete(facility)
    }
}
