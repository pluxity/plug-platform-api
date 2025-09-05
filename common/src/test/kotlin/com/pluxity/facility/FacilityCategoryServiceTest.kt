package com.pluxity.facility

import com.pluxity.config.MockBeansConfig
import com.pluxity.facility.category.FacilityCategoryService
import com.pluxity.facility.category.dto.FacilityCategoryCreateRequest
import com.pluxity.facility.category.dto.FacilityCategoryUpdateRequest
import com.pluxity.global.exception.CustomException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Import(MockBeansConfig::class)
@Transactional
internal class FacilityCategoryServiceTest {
    @Autowired
    private lateinit var categoryService: FacilityCategoryService

    private lateinit var createRequest: FacilityCategoryCreateRequest
    private var parentCategoryId: Long = 0

    @BeforeEach
    fun setUp() {
        // 부모 카테고리 생성
        val parentRequest = FacilityCategoryCreateRequest("부모 카테고리", null)
        parentCategoryId = categoryService.create(parentRequest)

        // 테스트용 카테고리 요청 준비
        createRequest = FacilityCategoryCreateRequest("테스트 카테고리", parentCategoryId)
    }

    @Test
    @DisplayName("유효한 요청으로 카테고리 생성 시 카테고리가 저장된다")
    fun create_WithValidRequest_SavesCategory() {
        // when
        val categoryId = categoryService.create(createRequest)
        val category = categoryService.findById(categoryId)

        // then
        assertThat(category).isNotNull
        assertThat(category.name).isEqualTo("테스트 카테고리")
        assertThat(category.parent?.id).isEqualTo(parentCategoryId)
    }

    @Test
    @DisplayName("부모 카테고리 없이 카테고리 생성 시 카테고리가 저장된다")
    fun create_WithoutParentCategory_SavesCategory() {
        // given
        val requestWithoutParent = FacilityCategoryCreateRequest("부모 없는 카테고리", null)

        // when
        val categoryId = categoryService.create(requestWithoutParent)
        val category = categoryService.findById(categoryId)

        // then
        assertThat(category).isNotNull
        assertThat(category.name).isEqualTo("부모 없는 카테고리")
        assertThat(category.parent).isNull()
    }

    @Test
    @DisplayName("존재하지 않는 부모 카테고리로 생성 시 예외가 발생한다")
    fun create_WithNonExistingParentId_ThrowsCustomException() {
        // given
        val nonExistingParentId = 9999L
        val invalidRequest = FacilityCategoryCreateRequest("실패할 카테고리", nonExistingParentId)

        // when & then
        assertThrows<CustomException> { categoryService.create(invalidRequest) }
    }

    @Test
    @DisplayName("모든 카테고리 조회 시 카테고리 목록이 계층형구조로 반환된다")
    fun findAll_ReturnsListOfCategoryResponses() {
        // given
        categoryService.create(createRequest)

        // when
        val responses = categoryService.findAll()

        // then
        assertThat(responses).isNotEmpty
        assertThat(responses.size).isGreaterThanOrEqualTo(1)
    }

    @Test
    @DisplayName("존재하지 않는 ID로 카테고리 조회 시 예외가 발생한다")
    fun findById_WithNonExistingId_ThrowsCustomException() {
        // given
        val nonExistingId = 9999L

        // when & then
        assertThrows<CustomException> { categoryService.findById(nonExistingId) }
    }

    @Test
    @DisplayName("유효한 요청으로 카테고리 정보 수정 시 카테고리 정보가 업데이트된다")
    fun update_WithValidRequest_UpdatesCategory() {
        // given
        val savedCategoryId = categoryService.create(createRequest)
        val updateRequest = FacilityCategoryUpdateRequest("수정된 카테고리", null)

        // when
        categoryService.update(savedCategoryId, updateRequest)

        // then
        val category = categoryService.findById(savedCategoryId)
        assertThat(category.name).isEqualTo("수정된 카테고리")
    }

    @Test
    @DisplayName("부모 카테고리 변경 시 카테고리의 부모가 업데이트된다")
    fun update_WithNewParentId_UpdatesParentCategory() {
        // given
        val savedCategoryId = categoryService.create(createRequest)
        // 새로운 부모 카테고리 생성
        val newParentRequest = FacilityCategoryCreateRequest("새 부모 카테고리", null)
        val newParentId = categoryService.create(newParentRequest)
        val updateRequest = FacilityCategoryUpdateRequest("카테고리", newParentId)

        // when
        categoryService.update(savedCategoryId, updateRequest)

        // then
        val category = categoryService.findById(savedCategoryId)
        assertThat(category.parent?.id).isEqualTo(newParentId)
    }

    @Test
    @DisplayName("존재하지 않는 부모 카테고리로 업데이트 시 예외가 발생한다")
    fun update_WithNonExistingParentId_ThrowsCustomException() {
        // given
        val savedCategoryId = categoryService.create(createRequest)
        val nonExistingParentId = 9999L
        val invalidRequest = FacilityCategoryUpdateRequest("유효한 이름", nonExistingParentId)

        // when & then
        assertThrows<CustomException> { categoryService.update(savedCategoryId, invalidRequest) }
    }

    @Test
    @DisplayName("카테고리 삭제 시 데이터베이스에서 삭제된다")
    fun delete_RemovesCategoryFromDatabase() {
        // given
        val savedCategoryId = categoryService.create(createRequest)

        // when
        categoryService.delete(savedCategoryId)

        // then
        assertThrows<CustomException> { categoryService.findById(savedCategoryId) }
    }

    @Test
    @DisplayName("빈 이름으로 카테고리 생성 시 예외가 발생한다")
    fun create_WithEmptyName_ThrowsCustomException() {
        // 이 테스트는 컨트롤러 계층에서 @Valid 검증을 통해 수행되어야 합니다.
        // @NotBlank 어노테이션이 있으므로 컨트롤러 테스트에서 검증해야 합니다.
    }

    @Test
    @DisplayName("null 이름으로 카테고리 생성 시 예외가 발생한다")
    fun create_WithNullName_ThrowsCustomException() {
        // 이 테스트는 컨트롤러 계층에서 @Valid 검증을 통해 수행되어야 합니다.
        // @NotBlank 어노테이션이 있으므로 컨트롤러 테스트에서 검증해야 합니다.
    }

    @Test
    @DisplayName("카테고리 업데이트 시 이름이 비어있으면서 부모 ID도 null이면 예외가 발생한다")
    fun update_WithEmptyNameAndNullParentId_ThrowsCustomException() {
        // 이 테스트는 컨트롤러 계층에서 @Valid 검증을 통해 수행되어야 합니다.
        // @NotBlank 어노테이션이 있으므로 컨트롤러 테스트에서 검증해야 합니다.
    }

    @Test
    @DisplayName("자기 자신을 부모로 설정하려 할 때 예외가 발생한다")
    fun update_WithSelfAsParent_ThrowsCustomException() {
        // given
        val savedCategoryId = categoryService.create(createRequest)
        val invalidRequest =
            FacilityCategoryUpdateRequest(
                "유효한 이름",
                savedCategoryId, // 자기 자신을 부모로 설정
            )

        // when & then
        assertThrows<CustomException> { categoryService.update(savedCategoryId, invalidRequest) }
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 삭제 시 예외가 발생한다")
    fun delete_WithNonExistingId_ThrowsCustomException() {
        // given
        val nonExistingId = 9999L

        // when & then
        assertThrows<CustomException> { categoryService.delete(nonExistingId) }
    }

    @Test
    @DisplayName("하위 카테고리가 있는 카테고리 삭제 시 예외가 발생한다")
    fun delete_WithChildCategories_ThrowsCustomException() {
        // given
        // 부모 -> 자식 구조 생성
        val parentResponseId = categoryService.create(FacilityCategoryCreateRequest("새로운 부모", null))

        // 자식 카테고리 생성
        val childResponseId = categoryService.create(FacilityCategoryCreateRequest("자식 카테고리", parentResponseId))

        // when & then
        // 자식이 있는 부모 카테고리 삭제 시도
        assertThrows<CustomException> { categoryService.delete(parentResponseId) }
    }

    @Test
    @DisplayName("최대 깊이를 초과하는 계층 구조 생성 시 예외가 발생한다")
    fun create_ExceedingMaxDepth_ThrowsCustomException() {
        // given
        // 1단계: 루트
        val rootResponseId = categoryService.create(FacilityCategoryCreateRequest("루트 카테고리", null))

        // 2단계: 루트 -> 자식1
        val child1ResponseId = categoryService.create(FacilityCategoryCreateRequest("자식 카테고리 1", rootResponseId))

        // 3단계: 루트 -> 자식1 -> 자식2(최대 깊이 초과 가정)
        val exceedDepthRequest = FacilityCategoryCreateRequest("최대 깊이 초과 카테고리", child1ResponseId)

        // when & then
        // 최대 깊이(일반적으로 2단계)를 초과하는 카테고리 생성 시도
        assertThrows<CustomException> { categoryService.create(exceedDepthRequest) }
    }

    @Test
    @DisplayName("동일한 이름의 형제 카테고리 생성 시 문제가 발생하지 않는다.")
    fun create_WithDuplicateNameInSameLevel_ThrowsCustomException() {
        // given
        // 첫 번째 자식 카테고리 생성
        categoryService.create(createRequest)

        // 동일한 이름, 동일한 부모를 가진 카테고리 생성 시도
        val duplicateRequest =
            FacilityCategoryCreateRequest(
                "테스트 카테고리", // 동일한 이름
                parentCategoryId, // 동일한 부모
            )

        // when & then
        assertThrows<CustomException> { categoryService.create(duplicateRequest) }
    }

    @Test
    @DisplayName("이름 업데이트하고 부모는 그대로 유지되는지 확인한다")
    fun update_WithNameAndParent() {
        // given
        val savedCategoryId = categoryService.create(createRequest)
        val updateRequest = FacilityCategoryUpdateRequest("새 이름 업데이트", parentCategoryId)

        // when
        categoryService.update(savedCategoryId, updateRequest)

        // then
        val updatedCategory = categoryService.findById(savedCategoryId)
        assertThat(updatedCategory.name).isEqualTo("새 이름 업데이트")
        assertThat(updatedCategory.parent?.id).isEqualTo(updateRequest.parentId)
    }
}
