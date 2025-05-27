package com.pluxity.device.service;

import com.pluxity.device.dto.DeviceCategoryRequest;
import com.pluxity.device.dto.DeviceCategoryResponse;
import com.pluxity.device.dto.DeviceCategoryTreeResponse;
import com.pluxity.device.repository.DeviceCategoryRepository;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
        DeviceCategoryResponse savedCategory = deviceCategoryService.getDeviceCategoryResponse(id);
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.name()).isEqualTo("테스트 카테고리");
        assertThat(savedCategory.iconFile().id()).isEqualTo(iconFileId);
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
        
        DeviceCategoryRequest updateRequest = new DeviceCategoryRequest("수정된 카테고리", null, newIconFileId);

        // when
        deviceCategoryService.update(id, updateRequest);

        // then
        DeviceCategoryResponse updatedCategory = deviceCategoryService.getDeviceCategoryResponse(id);
        assertThat(updatedCategory.name()).isEqualTo("수정된 카테고리");
        assertThat(updatedCategory.iconFile().id()).isEqualTo(newIconFileId);
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
        assertThat(responses.getFirst().iconFile().id()).isEqualTo(iconFileId);
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
        assertThat(response.iconFile().id()).isEqualTo(iconFileId);
    }

    @Test
    @DisplayName("존재하지 않는 ID로 카테고리 조회 시 예외가 발생한다")
    void getDeviceCategoryResponse_WithNonExistingId_ThrowsCustomException() {
        // given
        Long nonExistingId = 9999L;

        // when and then
        assertThrows(CustomException.class, () -> deviceCategoryService.getDeviceCategoryResponse(nonExistingId));
    }

    @Test
    @DisplayName("유효한 요청으로 카테고리 정보 수정 시 카테고리 정보가 업데이트된다")
    void update_WithValidRequest_UpdatesCategory() {
        // given
        Long id = deviceCategoryService.create(createRequest);
        DeviceCategoryRequest updateRequest = new DeviceCategoryRequest("수정된 카테고리", null, iconFileId);

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
        
        DeviceCategoryRequest childRequest = new DeviceCategoryRequest("하위 카테고리", parentId, iconFileId);
        Long childId = deviceCategoryService.create(childRequest);
        
        // when
        List<DeviceCategoryTreeResponse> treeResponses = deviceCategoryService.getDeviceCategoryTree();
        
        // then
        assertThat(treeResponses).isNotEmpty();
        assertThat(treeResponses.getFirst().name()).isEqualTo("테스트 카테고리");
        assertThat(treeResponses.getFirst().children()).isNotEmpty();
        assertThat(treeResponses.getFirst().children().getFirst().name()).isEqualTo("하위 카테고리");
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