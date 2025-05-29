package com.pluxity.facility.category;

import com.pluxity.facility.category.dto.FacilityCategoryCreateRequest;
import com.pluxity.facility.category.dto.FacilityCategoryResponse;
import com.pluxity.facility.category.dto.FacilityCategoryUpdateRequest;
import com.pluxity.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional

class FacilityCategoryServiceTest {

    @Autowired
    private FacilityCategoryService categoryService;

    @Autowired
    private FacilityCategoryRepository categoryRepository;

    private FacilityCategoryCreateRequest createRequest;
    private Long parentCategoryId;

    @BeforeEach
    void setUp() {
        // 부모 카테고리 생성
        FacilityCategoryCreateRequest parentRequest = new FacilityCategoryCreateRequest(
                "부모 카테고리",
                null
        );
        FacilityCategoryResponse parentResponse = categoryService.create(parentRequest);
        parentCategoryId = parentResponse.id();

        // 테스트용 카테고리 요청 준비
        createRequest = new FacilityCategoryCreateRequest(
                "테스트 카테고리",
                parentCategoryId
        );
    }

    @Test
    @DisplayName("유효한 요청으로 카테고리 생성 시 카테고리가 저장된다")
    void create_WithValidRequest_SavesCategory() {
        // when
        FacilityCategoryResponse response = categoryService.create(createRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("테스트 카테고리");
        assertThat(response.parentId()).isEqualTo(parentCategoryId);
    }

    @Test
    @DisplayName("부모 카테고리 없이 카테고리 생성 시 카테고리가 저장된다")
    void create_WithoutParentCategory_SavesCategory() {
        // given
        FacilityCategoryCreateRequest requestWithoutParent = new FacilityCategoryCreateRequest(
                "부모 없는 카테고리",
                null
        );

        // when
        FacilityCategoryResponse response = categoryService.create(requestWithoutParent);

        // then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("부모 없는 카테고리");
        assertThat(response.parentId()).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 부모 카테고리로 생성 시 예외가 발생한다")
    void create_WithNonExistingParentId_ThrowsCustomException() {
        // given
        Long nonExistingParentId = 9999L;
        FacilityCategoryCreateRequest invalidRequest = new FacilityCategoryCreateRequest(
                "실패할 카테고리",
                nonExistingParentId
        );

        // when & then
        assertThrows(CustomException.class, () -> categoryService.create(invalidRequest));
    }

    @Test
    @DisplayName("모든 카테고리 조회 시 카테고리 목록이 반환된다")
    void findAll_ReturnsListOfCategoryResponses() {
        // given
        categoryService.create(createRequest);

        // when
        List<FacilityCategoryResponse> responses = categoryService.findAll();

        // then
        assertThat(responses).isNotEmpty();
        assertThat(responses.size()).isGreaterThanOrEqualTo(2); // 부모 카테고리 + 생성한 카테고리
    }

    @Test
    @DisplayName("ID로 카테고리 조회 시 카테고리 정보가 반환된다")
    void findById_WithExistingId_ReturnsCategoryResponse() {
        // given
        FacilityCategoryResponse savedCategory = categoryService.create(createRequest);

        // when
        FacilityCategoryResponse response = categoryService.findById(savedCategory.id());

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(savedCategory.id());
        assertThat(response.name()).isEqualTo("테스트 카테고리");
        assertThat(response.parentId()).isEqualTo(parentCategoryId);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 카테고리 조회 시 예외가 발생한다")
    void findById_WithNonExistingId_ThrowsCustomException() {
        // given
        Long nonExistingId = 9999L;

        // when & then
        assertThrows(CustomException.class, () -> categoryService.findById(nonExistingId));
    }

    @Test
    @DisplayName("유효한 요청으로 카테고리 정보 수정 시 카테고리 정보가 업데이트된다")
    void update_WithValidRequest_UpdatesCategory() {
        // given
        FacilityCategoryResponse savedCategory = categoryService.create(createRequest);
        FacilityCategoryUpdateRequest updateRequest = new FacilityCategoryUpdateRequest(
                "수정된 카테고리",
                null
        );

        // when
        categoryService.update(savedCategory.id(), updateRequest);

        // then
        FacilityCategoryResponse updatedCategory = categoryService.findById(savedCategory.id());
        assertThat(updatedCategory.name()).isEqualTo("수정된 카테고리");
        assertThat(updatedCategory.parentId()).isEqualTo(parentCategoryId); // 부모는 변경하지 않았으므로 그대로
    }

    @Test
    @DisplayName("부모 카테고리 변경 시 카테고리의 부모가 업데이트된다")
    void update_WithNewParentId_UpdatesParentCategory() {
        // given
        FacilityCategoryResponse savedCategory = categoryService.create(createRequest);
        
        // 새로운 부모 카테고리 생성
        FacilityCategoryCreateRequest newParentRequest = new FacilityCategoryCreateRequest(
                "새 부모 카테고리",
                null
        );
        FacilityCategoryResponse newParentResponse = categoryService.create(newParentRequest);
        
        FacilityCategoryUpdateRequest updateRequest = new FacilityCategoryUpdateRequest(
                null,
                newParentResponse.id()
        );

        // when
        categoryService.update(savedCategory.id(), updateRequest);

        // then
        FacilityCategoryResponse updatedCategory = categoryService.findById(savedCategory.id());
        assertThat(updatedCategory.parentId()).isEqualTo(newParentResponse.id());
    }

    @Test
    @DisplayName("존재하지 않는 부모 카테고리로 업데이트 시 예외가 발생한다")
    void update_WithNonExistingParentId_ThrowsCustomException() {
        // given
        FacilityCategoryResponse savedCategory = categoryService.create(createRequest);
        Long nonExistingParentId = 9999L;
        FacilityCategoryUpdateRequest invalidRequest = new FacilityCategoryUpdateRequest(
                null,
                nonExistingParentId
        );

        // when & then
        assertThrows(CustomException.class, () -> categoryService.update(savedCategory.id(), invalidRequest));
    }

    @Test
    @DisplayName("카테고리 삭제 시 데이터베이스에서 삭제된다")
    void delete_RemovesCategoryFromDatabase() {
        // given
        FacilityCategoryResponse savedCategory = categoryService.create(createRequest);
        
        // when
        categoryService.delete(savedCategory.id());
        
        // then
        assertThrows(CustomException.class, () -> categoryService.findById(savedCategory.id()));
    }
}