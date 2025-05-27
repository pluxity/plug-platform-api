package com.pluxity.domains.device.service;

import com.pluxity.SasangApplication;
import com.pluxity.device.dto.DeviceCategoryRequest;
import com.pluxity.device.service.DeviceCategoryService;
import com.pluxity.domains.device.dto.NfluxCategoryCreateRequest;
import com.pluxity.domains.device.dto.NfluxCategoryResponse;
import com.pluxity.domains.device.dto.NfluxCategoryUpdateRequest;
import com.pluxity.domains.device.entity.NfluxCategory;
import com.pluxity.domains.device.repository.NfluxCategoryRepository;
import com.pluxity.global.exception.CustomException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = SasangApplication.class)
@Transactional
class NfluxCategoryServiceTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private NfluxCategoryService nfluxCategoryService;

    @Autowired
    private NfluxCategoryRepository nfluxCategoryRepository;

    @Autowired
    private DeviceCategoryService deviceCategoryService;

    private NfluxCategory rootCategory; // 루트 카테고리 (depth=1)
    private NfluxCategoryCreateRequest createRequestWithoutParent; // 루트 카테고리 생성용
    private NfluxCategoryCreateRequest createRequestWithDeviceCategory;

    @BeforeEach
    void setUp() {
        // 루트 카테고리 생성 (depth=1)
        rootCategory = NfluxCategory.nfluxBuilder()
                .name("루트 카테고리")
                .contextPath("/root")
                .build();
        nfluxCategoryRepository.save(rootCategory);

        // 루트 카테고리 생성 요청 준비 (parent 없음 - 루트 카테고리 됨)
        createRequestWithoutParent = new NfluxCategoryCreateRequest(
                "테스트 루트 카테고리",
                null, // 부모 없음
                "/test-root"
        );
        
        // DeviceCategoryRequest를 포함한 생성 요청 준비 (부모 없는 루트 카테고리)
        DeviceCategoryRequest deviceCategoryRequest = new DeviceCategoryRequest(
                "테스트 디바이스 루트 카테고리",
                null, // 부모 없음
                null // iconFileId
        );
        
        createRequestWithDeviceCategory = new NfluxCategoryCreateRequest(
                "테스트 카테고리 with DeviceCategory",
                null, // 부모 없음
                "/test-with-device",
                deviceCategoryRequest
        );
    }

    @Test
    @DisplayName("루트 카테고리 생성 시 카테고리가 저장된다")
    void save_WithValidRequest_SavesCategory() {
        // when
        Long id = nfluxCategoryService.save(createRequestWithoutParent);

        // then
        assertThat(id).isNotNull();

        // 저장된 카테고리 확인
        NfluxCategoryResponse savedCategory = nfluxCategoryService.findById(id);
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.name()).isEqualTo("테스트 루트 카테고리");
        assertThat(savedCategory.parentId()).isNull(); // 루트 카테고리이므로 부모 없음
        assertThat(savedCategory.contextPath()).isEqualTo("/test-root");
    }
    
    @Test
    @DisplayName("DeviceCategoryRequest를 포함한 요청으로 루트 카테고리 생성 시 카테고리가 저장된다")
    void save_WithDeviceCategoryRequest_SavesCategory() {
        // when
        Long id = nfluxCategoryService.save(createRequestWithDeviceCategory);

        // then
        assertThat(id).isNotNull();

        // 저장된 카테고리 확인
        NfluxCategoryResponse savedCategory = nfluxCategoryService.findById(id);
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.name()).isEqualTo("테스트 디바이스 루트 카테고리");
        assertThat(savedCategory.parentId()).isNull(); // 루트 카테고리이므로 부모 없음
        assertThat(savedCategory.contextPath()).isEqualTo("/test-with-device");
    }

    @Test
    @DisplayName("깊이 제한을 초과하면 예외가 발생한다")
    void save_ExceedingMaxDepth_ThrowsException() {
        // given - 이미 루트 카테고리가 있음 (depth=1)
        
        // 루트 카테고리의 자식 카테고리 생성 요청 (depth=2)
        NfluxCategoryCreateRequest childRequest = new NfluxCategoryCreateRequest(
                "자식 카테고리",
                rootCategory.getId(), // 루트 카테고리를 부모로 지정
                "/child"
        );
        
        // when & then
        // NfluxCategory의 maxDepth가 1이므로, 자식 카테고리 생성 시 예외가 발생해야 함
        assertThrows(CustomException.class, () -> 
            nfluxCategoryService.save(childRequest)
        );
    }

    @Test
    @DisplayName("모든 카테고리 조회 시 카테고리 목록이 반환된다")
    void findAll_ReturnsListOfCategoryResponses() {
        // given
        nfluxCategoryService.save(createRequestWithoutParent);

        // when
        List<NfluxCategoryResponse> responses = nfluxCategoryService.findAll();

        // then
        assertThat(responses).isNotEmpty();
        assertThat(responses.size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("루트 카테고리만 조회 시 부모가 없는 카테고리만 반환된다")
    void findAllRoots_ReturnsOnlyRootCategories() {
        // given
        nfluxCategoryService.save(createRequestWithoutParent);

        // when
        List<NfluxCategoryResponse> rootCategories = nfluxCategoryService.findAllRoots();

        // then
        assertThat(rootCategories).isNotEmpty();
        assertThat(rootCategories).allMatch(category -> category.parentId() == null);
    }

    @Test
    @DisplayName("ID로 카테고리 조회 시 카테고리 정보가 반환된다")
    void findById_WithExistingId_ReturnsCategoryResponse() {
        // given
        Long id = nfluxCategoryService.save(createRequestWithoutParent);

        // when
        NfluxCategoryResponse response = nfluxCategoryService.findById(id);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.name()).isEqualTo("테스트 루트 카테고리");
        assertThat(response.contextPath()).isEqualTo("/test-root");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 카테고리 조회 시 예외가 발생한다")
    void findById_WithNonExistingId_ThrowsCustomException() {
        // given
        Long nonExistingId = 9999L;

        // when & then
        assertThrows(CustomException.class, () -> 
            nfluxCategoryService.findById(nonExistingId)
        );
    }

    @Test
    @DisplayName("유효한 요청으로 카테고리 정보 수정 시 카테고리 정보가 업데이트된다")
    void update_WithValidRequest_UpdatesCategory() {
        // given
        Long id = nfluxCategoryService.save(createRequestWithoutParent);
        
        NfluxCategoryUpdateRequest updateRequest = new NfluxCategoryUpdateRequest(
                "수정된 카테고리",
                null, // 부모 ID는 변경하지 않음
                "/updated"
        );

        // when
        NfluxCategoryResponse updatedCategory = nfluxCategoryService.update(id, updateRequest);

        // then
        assertThat(updatedCategory.name()).isEqualTo("수정된 카테고리");
        assertThat(updatedCategory.parentId()).isNull(); // 여전히 루트 카테고리
        assertThat(updatedCategory.contextPath()).isEqualTo("/updated");
        
        // 데이터베이스에서 직접 확인
        NfluxCategory categoryFromDb = nfluxCategoryRepository.findById(id).orElseThrow();
        assertThat(categoryFromDb.getName()).isEqualTo("수정된 카테고리");
        assertThat(categoryFromDb.getParent()).isNull(); // 여전히 루트 카테고리
        assertThat(categoryFromDb.getContextPath()).isEqualTo("/updated");
    }

    @Test
    @DisplayName("부분 업데이트 시 지정된 필드만 변경된다")
    void update_WithPartialRequest_UpdatesOnlySpecifiedFields() {
        // given
        Long id = nfluxCategoryService.save(createRequestWithoutParent);
        
        // 이름만 변경
        NfluxCategoryUpdateRequest nameOnlyRequest = new NfluxCategoryUpdateRequest(
                "이름만 변경",
                null,
                null
        );

        // when
        NfluxCategoryResponse updatedCategory = nfluxCategoryService.update(id, nameOnlyRequest);

        // then
        assertThat(updatedCategory.name()).isEqualTo("이름만 변경");
        assertThat(updatedCategory.parentId()).isNull(); // 변경 안됨
        assertThat(updatedCategory.contextPath()).isEqualTo("/test-root"); // 변경 안됨
    }
    
    @Test
    @DisplayName("DeviceCategoryRequest를 통한 업데이트 시 정보가 업데이트된다")
    void update_WithDeviceCategoryRequest_UpdatesCategory() {
        // given
        Long id = nfluxCategoryService.save(createRequestWithoutParent);
        
        DeviceCategoryRequest deviceRequest = new DeviceCategoryRequest(
                "디바이스 요청으로 수정",
                null, // 부모 없음
                null // 아이콘 없음
        );
        
        NfluxCategoryUpdateRequest updateRequest = new NfluxCategoryUpdateRequest(
                null,
                null,
                "/updated-via-device",
                deviceRequest
        );

        // when
        NfluxCategoryResponse updatedCategory = nfluxCategoryService.update(id, updateRequest);

        // then
        assertThat(updatedCategory.name()).isEqualTo("디바이스 요청으로 수정"); // deviceRequest에서 가져온 값
        assertThat(updatedCategory.parentId()).isNull(); // 여전히 루트 카테고리
        assertThat(updatedCategory.contextPath()).isEqualTo("/updated-via-device"); // 직접 지정된 값
    }

    @Test
    @DisplayName("카테고리 삭제 시 카테고리가 삭제된다")
    void delete_WithExistingId_DeletesCategory() {
        // given
        Long id = nfluxCategoryService.save(createRequestWithoutParent);
        
        // when
        nfluxCategoryService.delete(id);
        
        // then
        // 삭제 후에는 해당 ID로 카테고리를 찾을 수 없어야 함
        assertThrows(CustomException.class, () -> nfluxCategoryService.findById(id));
    }
} 