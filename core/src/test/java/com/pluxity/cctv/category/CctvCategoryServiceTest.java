package com.pluxity.cctv.category;

import com.pluxity.device.dto.DeviceCategoryRequest;
import com.pluxity.device.dto.DeviceCategoryUpdateRequest;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.device.service.DeviceCategoryService;
import com.pluxity.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class CctvCategoryServiceTest {

    @Autowired
    private DeviceCategoryService categoryService;

    private DeviceCategoryRequest createRequest;
    private Long parentCategoryId;

    @BeforeEach
    void setUp() {
        // 부모 카테고리 생성
        DeviceCategoryRequest parentRequest = new DeviceCategoryRequest("부모 카테고리", null, null);
        parentCategoryId = categoryService.create(parentRequest);

        createRequest = new DeviceCategoryRequest("테스트 카테고리", parentCategoryId, null);
    }

    @Test
    @DisplayName("유효한 요청으로 카테고리 생성 시 카테고리가 저장된다")
    void create_WithValidRequest_SavesCategory() {
        // when
        Long categoryId = categoryService.create(createRequest);
        DeviceCategory category = categoryService.findById(categoryId);

        // then
        assertThat(category).isNotNull();
        assertThat(category.getName()).isEqualTo("테스트 카테고리");
        assertThat(category.getParent().getId()).isEqualTo(parentCategoryId);
    }

    @Test
    @DisplayName("부모 카테고리 없이 카테고리 생성 시 카테고리가 저장된다")
    void create_WithoutParentCategory_SavesCategory() {
        // given
        DeviceCategoryRequest requestWithoutParent = new DeviceCategoryRequest(
                "부모 없는 카테고리",
                null,
                null
        );

        // when
        Long categoryId = categoryService.create(requestWithoutParent);
        DeviceCategory category = categoryService.findById(categoryId);

        // then
        assertThat(category).isNotNull();
        assertThat(category.getName()).isEqualTo("부모 없는 카테고리");
        assertThat(category.getParent()).isNull();
    }
    
    @Test
    @DisplayName("존재하지 않는 부모 카테고리로 생성 시 예외가 발생한다")
    void create_WithNonExistingParentId_ThrowsCustomException() {
        // given
        Long nonExistingParentId = 9999L;
        DeviceCategoryRequest invalidRequest = new DeviceCategoryRequest(
                "실패할 카테고리",
                nonExistingParentId,
                null
        );

        // when & then
        assertThrows(CustomException.class, () -> categoryService.create(invalidRequest));
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
        Long savedCategoryId = categoryService.create(createRequest);
        DeviceCategoryUpdateRequest updateRequest = new DeviceCategoryUpdateRequest(
                "수정된 카테고리",
                null,
                null
        );

        // when
        categoryService.update(savedCategoryId, updateRequest);

        // then
        DeviceCategory category = categoryService.findById(savedCategoryId);
        assertThat(category.getName()).isEqualTo("수정된 카테고리");
    }

    @Test
    @DisplayName("부모 카테고리 변경 시 카테고리의 부모가 업데이트된다")
    void update_WithNewParentId_UpdatesParentCategory() {
        // given
        Long savedCategoryId = categoryService.create(createRequest);
        // 새로운 부모 카테고리 생성
        DeviceCategoryRequest newParentRequest = new DeviceCategoryRequest(
                "새 부모 카테고리",
                null,
                null
        );
        Long newParentId = categoryService.create(newParentRequest);
        DeviceCategoryUpdateRequest updateRequest = new DeviceCategoryUpdateRequest(
                "카테고리",
                newParentId,
                null
        );

        // when
        categoryService.update(savedCategoryId, updateRequest);

        // then
        DeviceCategory category = categoryService.findById(savedCategoryId);
        assertThat(category.getParent().getId()).isEqualTo(newParentId);
    }

    @Test
    @DisplayName("존재하지 않는 부모 카테고리로 업데이트 시 예외가 발생한다")
    void update_WithNonExistingParentId_ThrowsCustomException() {
        // given
        Long savedCategoryId = categoryService.create(createRequest);
        Long nonExistingParentId = 9999L;
        DeviceCategoryUpdateRequest invalidRequest = new DeviceCategoryUpdateRequest(
                null,
                nonExistingParentId,
                null
        );

        // when & then
        assertThrows(CustomException.class, () -> categoryService.update(savedCategoryId, invalidRequest));
    }

    @Test
    @DisplayName("카테고리 삭제 시 데이터베이스에서 삭제된다")
    void delete_RemovesCategoryFromDatabase() {
        // given
        Long savedCategoryId = categoryService.create(createRequest);

        // when
        categoryService.delete(savedCategoryId);

        // then
        assertThrows(CustomException.class, () -> categoryService.findById(savedCategoryId));
    }

    @Test
    @DisplayName("자기 자신을 부모로 설정하려 할 때 예외가 발생한다")
    void update_WithSelfAsParent_ThrowsCustomException() {
        // given
        Long savedCategoryId = categoryService.create(createRequest);
        DeviceCategoryUpdateRequest invalidRequest = new DeviceCategoryUpdateRequest(
                null,
                savedCategoryId,  // 자기 자신을 부모로 설정
                null
        );

        // when & then
        assertThrows(CustomException.class, () -> categoryService.update(savedCategoryId, invalidRequest));
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 삭제 시 예외가 발생한다")
    void delete_WithNonExistingId_ThrowsCustomException() {
        // given
        Long nonExistingId = 9999L;

        // when & then
        assertThrows(CustomException.class, () -> categoryService.delete(nonExistingId));
    }

    @Test
    @DisplayName("하위 카테고리가 있는 카테고리 삭제 시 예외가 발생한다")
    void delete_WithChildCategories_ThrowsCustomException() {
        // given
        // 부모 -> 자식 구조 생성
        Long parentResponseId = categoryService.create(new DeviceCategoryRequest(
                "새로운 부모",
                null,
                null
        ));

        // 자식 카테고리 생성
        Long childResponseId = categoryService.create(new DeviceCategoryRequest(
                "자식 카테고리",
                parentResponseId,
                null
        ));

        // when & then
        // 자식이 있는 부모 카테고리 삭제 시도
        assertThrows(CustomException.class, () -> categoryService.delete(parentResponseId));

    }

    @Test
    @DisplayName("최대 깊이를 초과하는 계층 구조 생성 시 예외가 발생한다")
    void create_ExceedingMaxDepth_ThrowsCustomException() {
        // given
        // 1단계: 루트
        Long rootResponseId = categoryService.create(new DeviceCategoryRequest(
                "루트 카테고리",
                null,
                null
        ));

        // 2단계: 루트 -> 자식1
        Long child1ResponseId = categoryService.create(new DeviceCategoryRequest(
                "자식 카테고리 1",
                rootResponseId,
                null
        ));

        // 3단계: 루트 -> 자식1 -> 자식2(최대 깊이 초과 가정)
        DeviceCategoryRequest exceedDepthRequest = new DeviceCategoryRequest(
                "최대 깊이 초과 카테고리",
                child1ResponseId,
                null
        );

        // when & then
        // 최대 깊이(일반적으로 2단계)를 초과하는 카테고리 생성 시도
        assertThrows(CustomException.class, () -> categoryService.create(exceedDepthRequest));
    }

    @Test
    @DisplayName("이름 업데이트하고 부모는 그대로 유지되는지 확인한다")
    void update_WithNameAndParent() {
        // given
        Long savedCategoryId = categoryService.create(createRequest);
        DeviceCategoryUpdateRequest updateRequest = new DeviceCategoryUpdateRequest(
                "새 이름 업데이트",
                parentCategoryId,
                null
        );

        // when
        categoryService.update(savedCategoryId, updateRequest);

        // then
        DeviceCategory updatedCategory = categoryService.findById(savedCategoryId);
        assertThat(updatedCategory.getName()).isEqualTo("새 이름 업데이트");
        assertThat(updatedCategory.getParent().getId()).isEqualTo(updateRequest.parentId());
    }
}