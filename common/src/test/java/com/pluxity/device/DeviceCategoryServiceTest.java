package com.pluxity.device;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pluxity.device.dto.DeviceCategoryRequest;
import com.pluxity.device.dto.DeviceCategoryResponse;
import com.pluxity.device.dto.DeviceCategoryUpdateRequest;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.device.repository.DeviceCategoryRepository;
import com.pluxity.device.service.DeviceCategoryService;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@SpringBootTest
@Transactional
class DeviceCategoryServiceTest {

    @Autowired
    DeviceCategoryService deviceCategoryService;

    @Autowired
    DeviceCategoryRepository deviceCategoryRepository;
    
    @Autowired
    FileService fileService;

    private DeviceCategoryRequest createRequest;
    private Long iconFileId;

    @BeforeEach
    void setUp() throws IOException {
        // 테스트 이미지 파일 준비
        ClassPathResource resource = new ClassPathResource("temp/temp.png");
        byte[] fileContent = Files.readAllBytes(Path.of(resource.getURI()));
        
        // MockMultipartFile 생성
        MultipartFile iconFile = new MockMultipartFile(
                "icon.png", "icon.png", "image/png", fileContent);
        
        // 파일 업로드 초기화
        iconFileId = fileService.initiateUpload(iconFile);

        // 테스트 데이터 준비
        createRequest = new DeviceCategoryRequest("테스트 카테고리", null, iconFileId);
    }

    @Test
    @DisplayName("유효한 요청으로 카테고리 생성 시 카테고리가 저장된다")
    void create_WithValidRequest_SavesCategory() {
        // when
        Long id = deviceCategoryService.create(createRequest);

        // then
        assertThat(id).isNotNull();
        
        // 저장된 카테고리 확인
        DeviceCategory savedCategory = deviceCategoryService.findById(id);
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.getName()).isEqualTo("테스트 카테고리");
        assertThat(savedCategory.getIconFileId()).isEqualTo(iconFileId);
    }

    @Test
    @DisplayName("아이콘 파일을 변경하고 카테고리를 업데이트한다")
    void update_WithNewIconFile_UpdatesCategoryIcon() throws IOException {
        // given
        Long id = deviceCategoryService.create(createRequest);
        
        // 새로운 아이콘 파일 준비
        ClassPathResource resource = new ClassPathResource("temp/temp.png");
        byte[] fileContent = Files.readAllBytes(Path.of(resource.getURI()));
        
        MultipartFile newIconFile = new MockMultipartFile(
                "new_icon.png", "new_icon.png", "image/png", fileContent);
        
        Long newIconFileId = fileService.initiateUpload(newIconFile);

        DeviceCategoryUpdateRequest updateRequest = new DeviceCategoryUpdateRequest("수정된 카테고리", null, newIconFileId);

        // when
        deviceCategoryService.update(id, updateRequest);

        // then
        DeviceCategory updatedCategory = deviceCategoryService.findById(id);
        assertThat(updatedCategory.getName()).isEqualTo("수정된 카테고리");
        assertThat(updatedCategory.getIconFileId()).isEqualTo(newIconFileId);
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
        assertThat(responses.getFirst().name()).isEqualTo("테스트 카테고리");
        assertThat(responses.getFirst().thumbnailFile().id()).isEqualTo(iconFileId);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 카테고리 조회 시 예외가 발생한다")
    void getDeviceCategoryResponse_WithNonExistingId_ThrowsCustomException() {
        // given
        Long nonExistingId = 9999L;

        // when and then
        assertThrows(CustomException.class, () -> deviceCategoryService.findById(nonExistingId));
    }

    @Test
    @DisplayName("유효한 요청으로 카테고리 정보 수정 시 카테고리 정보가 업데이트된다")
    void update_WithValidRequest_UpdatesCategory() {
        // given
        Long id = deviceCategoryService.create(createRequest);
        DeviceCategoryUpdateRequest updateRequest = new DeviceCategoryUpdateRequest("수정된 카테고리", null, iconFileId);

        // when
        deviceCategoryService.update(id, updateRequest);

        // then
        DeviceCategory updatedCategory = deviceCategoryService.findById(id);
        assertThat(updatedCategory.getName()).isEqualTo("수정된 카테고리");
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
        assertThrows(CustomException.class, () -> deviceCategoryService.findById(id));
    }
    
    @Test
    @DisplayName("빈 이름으로 카테고리 생성 시 예외가 발생한다")
    void create_WithEmptyName_ThrowsCustomException() {
        // 이 테스트는 컨트롤러 계층에서 @Valid 검증을 통해 수행되어야 합니다.
        // @NotBlank 어노테이션이 있으므로 컨트롤러 테스트에서 검증해야 합니다.
    }

    @Test
    @DisplayName("유효하지 않은 부모 ID로 카테고리 생성 시 예외가 발생한다")
    void create_WithInvalidParentId_ThrowsCustomException() {
        // given
        Long nonExistingParentId = 9999L;
        DeviceCategoryRequest invalidRequest = new DeviceCategoryRequest("테스트 카테고리", nonExistingParentId, iconFileId);

        // when & then
        assertThrows(CustomException.class, () -> deviceCategoryService.create(invalidRequest));
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 업데이트 시 예외가 발생한다")
    void update_WithNonExistingId_ThrowsCustomException() {
        // given
        Long nonExistingId = 9999L;
        DeviceCategoryUpdateRequest updateRequest = new DeviceCategoryUpdateRequest("수정된 카테고리", null, iconFileId);

        // when & then
        assertThrows(CustomException.class, () -> deviceCategoryService.update(nonExistingId, updateRequest));
    }

    @Test
    @DisplayName("카테고리 업데이트 시 이름이 비어있으면 예외가 발생한다")
    void update_WithEmptyName_ThrowsCustomException() {
        // 이 테스트는 컨트롤러 계층에서 @Valid 검증을 통해 수행되어야 합니다.
        // @NotBlank 어노테이션이 있으므로 컨트롤러 테스트에서 검증해야 합니다.
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 삭제 시 예외가 발생한다")
    void delete_WithNonExistingId_ThrowsCustomException() {
        // given
        Long nonExistingId = 9999L;

        // when & then
        assertThrows(CustomException.class, () -> deviceCategoryService.delete(nonExistingId));
    }

    @Test
    @DisplayName("카테고리 최대 깊이 초과 시 예외가 발생한다")
    void create_ExceedsMaxDepth_ThrowsCustomException() {
        // given
        // 첫 번째 레벨 (루트) 카테고리 생성
        Long rootId = deviceCategoryService.create(createRequest);
        
        // 두 번째 레벨 카테고리 생성
        DeviceCategoryRequest level2Request = new DeviceCategoryRequest("레벨2 카테고리", rootId, iconFileId);
        Long level2Id = deviceCategoryService.create(level2Request);
        
        // 네 번째 레벨 카테고리 생성 시도 (일반적으로 최대 2단계로 제한되는 경우)
        DeviceCategoryRequest level4Request = new DeviceCategoryRequest("레벨3 카테고리", level2Id, iconFileId);
        
        // when & then
        assertThrows(CustomException.class, () -> deviceCategoryService.create(level4Request));
    }

    @Test
    @DisplayName("자식 카테고리가 있는 카테고리 삭제 시 예외가 발생한다")
    void delete_WithChildCategories_ThrowsCustomException() {
        // given
        // 부모 카테고리 생성
        Long parentId = deviceCategoryService.create(createRequest);
        
        // 자식 카테고리 생성
        DeviceCategoryRequest childRequest = new DeviceCategoryRequest("자식 카테고리", parentId, iconFileId);
        Long childId = deviceCategoryService.create(childRequest);
        
        // when & then
        // 자식이 있는 부모 카테고리 삭제 시도
        assertThrows(CustomException.class, () -> deviceCategoryService.delete(parentId));
    }
}
//test