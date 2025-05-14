package com.pluxity.device.service;

import com.pluxity.device.dto.DeviceCategoryRequest;
import com.pluxity.device.dto.DeviceCategoryResponse;
import com.pluxity.device.dto.DeviceCategoryTreeResponse;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.device.repository.DeviceCategoryRepository;
import com.pluxity.global.exception.CustomException;
import com.pluxity.icon.entity.Icon;
import com.pluxity.icon.repository.IconRepository;
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
class DeviceCategoryServiceTest {

    @Autowired
    DeviceCategoryService deviceCategoryService;

    @Autowired
    DeviceCategoryRepository deviceCategoryRepository;

    @Autowired
    IconRepository iconRepository;

    private DeviceCategoryRequest createRequest;
    private Long iconId;

    @BeforeEach
    void setUp() {
        // 테스트용 아이콘 생성
        Icon icon = Icon.builder()
                .name("테스트 아이콘")
                .build();
        Icon savedIcon = iconRepository.save(icon);
        iconId = savedIcon.getId();

        // 테스트 데이터 준비
        createRequest = new DeviceCategoryRequest("테스트 카테고리", null, iconId);
    }

    @Test
    @DisplayName("유효한 요청으로 카테고리 생성 시 카테고리가 저장된다")
    void create_WithValidRequest_SavesCategory() {
        // when
        Long id = deviceCategoryService.create(createRequest);

        // then
        assertThat(id).isNotNull();
        
        // 저장된 카테고리 확인
        DeviceCategoryResponse savedCategory = deviceCategoryService.getDeviceCategoryResponse(id);
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.name()).isEqualTo("테스트 카테고리");
        assertThat(savedCategory.iconId()).isEqualTo(iconId);
    }

    @Test
    @DisplayName("모든 루트 카테고리 조회 시 최상위 카테고리 목록이 반환된다")
    void getRootDeviceCategoryResponses_ReturnsListOfResponses() {
        // given
        Long id = deviceCategoryService.create(createRequest);
        
        // when
        List<DeviceCategoryResponse> responses = deviceCategoryService.getRootDeviceCategoryResponses();

        // then
        assertThat(responses).isNotEmpty();
        assertThat(responses.get(0).name()).isEqualTo("테스트 카테고리");
        assertThat(responses.get(0).iconId()).isEqualTo(iconId);
    }

    @Test
    @DisplayName("ID로 카테고리 조회 시 카테고리 정보가 반환된다")
    void getDeviceCategoryResponse_WithExistingId_ReturnsCategoryResponse() {
        // given
        Long id = deviceCategoryService.create(createRequest);

        // when
        DeviceCategoryResponse response = deviceCategoryService.getDeviceCategoryResponse(id);

        // then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("테스트 카테고리");
        assertThat(response.iconId()).isEqualTo(iconId);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 카테고리 조회 시 예외가 발생한다")
    void getDeviceCategoryResponse_WithNonExistingId_ThrowsCustomException() {
        // given
        Long nonExistingId = 9999L;

        // when & then
        assertThrows(CustomException.class, () -> deviceCategoryService.getDeviceCategoryResponse(nonExistingId));
    }

    @Test
    @DisplayName("유효한 요청으로 카테고리 정보 수정 시 카테고리 정보가 업데이트된다")
    void update_WithValidRequest_UpdatesCategory() {
        // given
        Long id = deviceCategoryService.create(createRequest);
        DeviceCategoryRequest updateRequest = new DeviceCategoryRequest("수정된 카테고리", null, iconId);

        // when
        deviceCategoryService.update(id, updateRequest);

        // then
        DeviceCategoryResponse updatedCategory = deviceCategoryService.getDeviceCategoryResponse(id);
        assertThat(updatedCategory.name()).isEqualTo("수정된 카테고리");
    }

    @Test
    @DisplayName("하위 카테고리를 만들고 계층 구조를 조회한다")
    void getDeviceCategoryTree_WithChildCategories_ReturnsTreeStructure() {
        // given
        Long parentId = deviceCategoryService.create(createRequest);
        System.out.println("부모 카테고리 ID: " + parentId);
        
        DeviceCategoryRequest childRequest = new DeviceCategoryRequest("하위 카테고리", parentId, iconId);
        Long childId = deviceCategoryService.create(childRequest);
        System.out.println("자식 카테고리 ID: " + childId);
        
        // 데이터베이스에서 직접 부모 카테고리 조회하여 자식 확인
        DeviceCategory parent = deviceCategoryRepository.findById(parentId).orElseThrow();
        System.out.println("부모 카테고리의 자식 수: " + parent.getChildren().size());
        
        // when
        List<DeviceCategoryTreeResponse> treeResponses = deviceCategoryService.getDeviceCategoryTree();
        System.out.println("트리 응답 개수: " + treeResponses.size());
        if (!treeResponses.isEmpty()) {
            System.out.println("첫 번째 트리 응답의 자식 수: " + treeResponses.get(0).children().size());
        }
        
        // then
        assertThat(treeResponses).isNotEmpty();
        assertThat(treeResponses.get(0).name()).isEqualTo("테스트 카테고리");
        assertThat(treeResponses.get(0).children()).isNotEmpty();
        assertThat(treeResponses.get(0).children().get(0).name()).isEqualTo("하위 카테고리");
    }

    @Test
    @DisplayName("디바이스가 없는 카테고리 삭제 시 정상적으로 삭제된다")
    void delete_WithEmptyCategory_DeletesCategory() {
        // given
        Long id = deviceCategoryService.create(createRequest);
        
        // when
        deviceCategoryService.delete(id);
        
        // then
        // 삭제 후에는 해당 ID로 카테고리를 찾을 수 없어야 함
        assertThrows(CustomException.class, () -> deviceCategoryService.getDeviceCategoryResponse(id));
    }
}