package com.pluxity.domains.device.service;

import com.pluxity.SasangApplication;
import com.pluxity.device.service.DeviceCategoryService;
import com.pluxity.domains.device.dto.NfluxCategoryCreateRequest;
import com.pluxity.domains.device.dto.NfluxCategoryResponse;
import com.pluxity.domains.device.dto.NfluxCategoryUpdateRequest;
import com.pluxity.domains.device.entity.NfluxCategory;
import com.pluxity.domains.device.repository.NfluxCategoryRepository;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
    
    @Autowired
    private FileService fileService;

    private NfluxCategory rootCategory; // 루트 카테고리 (depth=1)
    private NfluxCategoryCreateRequest createRequest; // 루트 카테고리 생성용
    private Long iconFileId; // 아이콘 파일 ID

    @BeforeEach
    void setUp() throws IOException {
        // 루트 카테고리 생성 (depth=1)
        rootCategory = NfluxCategory.nfluxBuilder()
                .name("루트 카테고리")
                .contextPath("/root")
                .build();
        nfluxCategoryRepository.save(rootCategory);
        
        // 테스트 아이콘 이미지 파일 준비
        ClassPathResource resource = new ClassPathResource("temp/temp.png");
        byte[] fileContent = Files.readAllBytes(Path.of(resource.getURI()));
        
        // MockMultipartFile 생성
        MultipartFile iconFile = new MockMultipartFile(
                "icon.png", "icon.png", "image/png", fileContent);
        
        // 파일 업로드 초기화 및 아이콘 파일 ID 설정
        iconFileId = fileService.initiateUpload(iconFile);

        // 생성 요청 준비
        createRequest = NfluxCategoryCreateRequest.of(
                "테스트 루트 카테고리",
                "/test-root",
                null
        );
    }

    @Test
    @DisplayName("루트 카테고리 생성 시 카테고리가 저장된다")
    void save_WithValidRequest_SavesCategory() {
        // when
        Long id = nfluxCategoryService.save(createRequest);

        // then
        assertThat(id).isNotNull();

        // 저장된 카테고리 확인
        NfluxCategoryResponse savedCategory = nfluxCategoryService.findById(id);
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.name()).isEqualTo("테스트 루트 카테고리");
        assertThat(savedCategory.contextPath()).isEqualTo("/test-root");
    }
    
    @Test
    @DisplayName("아이콘 ID를 포함한 요청으로 카테고리 생성 시 카테고리가 저장된다")
    void save_WithIconFileId_SavesCategory() {
        // given
        NfluxCategoryCreateRequest requestWithIcon = NfluxCategoryCreateRequest.of(
                "아이콘 포함 카테고리",
                "/with-icon",
                iconFileId
        );
                
        // when
        Long id = nfluxCategoryService.save(requestWithIcon);

        // then
        NfluxCategoryResponse savedCategory = nfluxCategoryService.findById(id);
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.name()).isEqualTo("아이콘 포함 카테고리");
        assertThat(savedCategory.iconFile()).isNotNull();
        assertThat(savedCategory.iconFile().id()).isEqualTo(iconFileId);
    }

    @Test
    @DisplayName("모든 카테고리 조회 시 카테고리 목록이 반환된다")
    void findAll_ReturnsListOfCategoryResponses() {
        // given
        nfluxCategoryService.save(createRequest);

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
        nfluxCategoryService.save(createRequest);

        // when
        List<NfluxCategoryResponse> rootCategories = nfluxCategoryService.findAllRoots();

        // then
        assertThat(rootCategories).isNotEmpty();
    }

    @Test
    @DisplayName("ID로 카테고리 조회 시 카테고리 정보가 반환된다")
    void findById_WithExistingId_ReturnsCategoryResponse() {
        // given
        Long id = nfluxCategoryService.save(createRequest);

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
        Long id = nfluxCategoryService.save(createRequest);
        
        NfluxCategoryUpdateRequest updateRequest = NfluxCategoryUpdateRequest.of(
                "수정된 카테고리",
                "/updated",
                null
        );

        // when
        NfluxCategoryResponse updatedCategory = nfluxCategoryService.update(id, updateRequest);

        // then
        assertThat(updatedCategory.name()).isEqualTo("수정된 카테고리");
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
        Long id = nfluxCategoryService.save(createRequest);
        
        // 이름만 변경
        NfluxCategoryUpdateRequest nameOnlyRequest = NfluxCategoryUpdateRequest.of(
                "이름만 변경",
                null,
                null
        );

        // when
        NfluxCategoryResponse updatedCategory = nfluxCategoryService.update(id, nameOnlyRequest);

        // then
        assertThat(updatedCategory.name()).isEqualTo("이름만 변경");
        assertThat(updatedCategory.contextPath()).isEqualTo("/test-root"); // 변경 안됨
    }
    
    @Test
    @DisplayName("아이콘 ID를 포함한 업데이트 시 정보가 업데이트된다")
    void update_WithIconFileId_UpdatesCategory() throws IOException {
        // given
        Long id = nfluxCategoryService.save(createRequest);
        
        // 새 아이콘 파일 준비
        ClassPathResource resource = new ClassPathResource("temp/temp.png");
        byte[] fileContent = Files.readAllBytes(Path.of(resource.getURI()));
        
        // 새 MockMultipartFile 생성
        MultipartFile newIconFile = new MockMultipartFile(
                "new-icon.png", "new-icon.png", "image/png", fileContent);
        
        // 새 아이콘 파일 업로드
        Long newIconFileId = fileService.initiateUpload(newIconFile);
        
        NfluxCategoryUpdateRequest updateRequest = NfluxCategoryUpdateRequest.of(
                "아이콘 추가 카테고리",
                null,
                newIconFileId
        );
        
        // when
        NfluxCategoryResponse updatedCategory = nfluxCategoryService.update(id, updateRequest);
        
        // then
        assertThat(updatedCategory.name()).isEqualTo("아이콘 추가 카테고리");
        assertThat(updatedCategory.iconFile()).isNotNull();
        assertThat(updatedCategory.iconFile().id()).isEqualTo(newIconFileId);
    }

    @Test
    @DisplayName("카테고리 삭제 시 카테고리가 삭제된다")
    void delete_WithExistingId_DeletesCategory() {
        // given
        Long id = nfluxCategoryService.save(createRequest);
        
        // when
        nfluxCategoryService.delete(id);
        
        // then
        assertThrows(CustomException.class, () -> nfluxCategoryService.findById(id));
    }
} 