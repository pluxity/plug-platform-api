package com.pluxity.asset.service;

import com.pluxity.asset.dto.AssetCategoryCreateRequest;
import com.pluxity.asset.dto.AssetCreateRequest;
import com.pluxity.asset.dto.AssetResponse;
import com.pluxity.asset.dto.AssetUpdateRequest;
import com.pluxity.asset.repository.AssetRepository;
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
class AssetServiceTest {

    @Autowired
    private AssetService assetService;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetCategoryService assetCategoryService;

    @Autowired
    private FileService fileService;

    private Long assetFileId;
    private Long thumbnailFileId;
    private Long categoryId;
    private String categoryCode = "TCC";
    private String categoryName = "테스트 카테고리";
    private AssetCreateRequest createRequest;

    @BeforeEach
    void setUp() throws IOException {
        // 테스트 이미지 파일 준비
        ClassPathResource resource = new ClassPathResource("temp/temp.png");
        byte[] fileContent = Files.readAllBytes(Path.of(resource.getURI()));

        // MockMultipartFile 생성
        MultipartFile assetFile = new MockMultipartFile(
                "asset.png", "asset.png", "image/png", fileContent);
        MultipartFile thumbnailFile = new MockMultipartFile(
                "thumbnail.png", "thumbnail.png", "image/png", fileContent);

        // 파일 업로드 초기화
        assetFileId = fileService.initiateUpload(assetFile);
        thumbnailFileId = fileService.initiateUpload(thumbnailFile);

        // 테스트 카테고리 생성
        AssetCategoryCreateRequest categoryRequest = new AssetCategoryCreateRequest(
                categoryName,
                categoryCode,
                null,
                null
        );
        categoryId = assetCategoryService.createAssetCategory(categoryRequest);

        // 테스트 데이터 준비
        createRequest = new AssetCreateRequest(
                "테스트 에셋",
                "TES",
                assetFileId,
                thumbnailFileId,
                categoryId
        );
    }

    @Test
    @DisplayName("유효한 요청으로 에셋 생성 시 에셋이 저장된다")
    void createAsset_WithValidRequest_SavesAsset() {
        // when
        Long id = assetService.createAsset(createRequest);

        // then
        assertThat(id).isNotNull();

        // 저장된 에셋 확인
        AssetResponse savedAsset = assetService.getAsset(id);
        assertThat(savedAsset).isNotNull();
        assertThat(savedAsset.name()).isEqualTo("테스트 에셋");
        assertThat(savedAsset.code()).isEqualTo("TES");
        assertThat(savedAsset.categoryId()).isEqualTo(categoryId);
        assertThat(savedAsset.categoryName()).isEqualTo(categoryName);
        assertThat(savedAsset.categoryCode()).isEqualTo(categoryCode);
        assertThat(savedAsset.file()).isNotNull();
        assertThat(savedAsset.thumbnailFile()).isNotNull();
    }

    @Test
    @DisplayName("모든 에셋 조회 시 에셋 목록이 반환된다")
    void getAssets_ReturnsListOfAssetResponses() {
        // given
        assetService.createAsset(createRequest);

        // when
        List<AssetResponse> responses = assetService.getAssets();

        // then
        assertThat(responses).isNotEmpty();
        AssetResponse firstAsset = responses.getFirst();
        assertThat(firstAsset.name()).isEqualTo("테스트 에셋");
        assertThat(firstAsset.code()).isEqualTo("TES");
        assertThat(firstAsset.categoryId()).isEqualTo(categoryId);
    }

    @Test
    @DisplayName("ID로 에셋 조회 시 에셋 정보가 반환된다")
    void getAsset_WithExistingId_ReturnsAssetResponse() {
        // given
        Long id = assetService.createAsset(createRequest);

        // when
        AssetResponse response = assetService.getAsset(id);

        // then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("테스트 에셋");
        assertThat(response.code()).isEqualTo("TES");
        assertThat(response.categoryId()).isEqualTo(categoryId);
        assertThat(response.categoryName()).isEqualTo(categoryName);
        assertThat(response.categoryCode()).isEqualTo(categoryCode);
        assertThat(response.file()).isNotNull();
        assertThat(response.thumbnailFile()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 ID로 에셋 조회 시 예외가 발생한다")
    void getAsset_WithNonExistingId_ThrowsCustomException() {
        // given
        Long nonExistingId = 9999L;

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> assetService.getAsset(nonExistingId));
        assertThat(exception.getHttpStatus()).isEqualTo(org.springframework.http.HttpStatus.NOT_FOUND);
        assertThat(exception.getMessage()).isEqualTo("해당 자원을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("유효한 요청으로 에셋 정보 수정 시 에셋 정보가 업데이트된다")
    void updateAsset_WithValidRequest_UpdatesAsset() throws IOException {
        // given
        Long id = assetService.createAsset(createRequest);

        // 새로운 파일 업로드
        ClassPathResource newResource = new ClassPathResource("temp/temp2.png");
        byte[] newFileContent = Files.readAllBytes(Path.of(newResource.getURI()));
        MultipartFile newAssetFile = new MockMultipartFile("new_asset.png", "new_asset.png", "image/png", newFileContent);
        Long newAssetFileId = fileService.initiateUpload(newAssetFile);

        AssetCategoryCreateRequest newCategoryRequest = new AssetCategoryCreateRequest(
                "새로운 카테고리",
                "NC1",
                null,
                null
        );
        Long newCategoryId = assetCategoryService.createAssetCategory(newCategoryRequest);

        AssetUpdateRequest updateRequest = new AssetUpdateRequest(
                "수정된 에셋",
                "UPD",
                newAssetFileId,
                null,
                newCategoryId
        );

        // when
        assetService.updateAsset(id, updateRequest);

        // then
        AssetResponse updatedAsset = assetService.getAsset(id);
        assertThat(updatedAsset.name()).isEqualTo("수정된 에셋");
        assertThat(updatedAsset.code()).isEqualTo("UPD");
        assertThat(updatedAsset.categoryId()).isEqualTo(newCategoryId);
        assertThat(updatedAsset.categoryName()).isEqualTo("새로운 카테고리");
        assertThat(updatedAsset.categoryCode()).isEqualTo("NC1");
        assertThat(updatedAsset.file()).isNotNull();
    }

    @Test
    @DisplayName("에셋 삭제 시 해당 에셋이 삭제된다")
    void deleteAsset_RemovesAsset() {
        // given
        Long id = assetService.createAsset(createRequest);
        AssetResponse response = assetService.getAsset(id);
        assertThat(response).isNotNull();

        // when
        assetService.deleteAsset(id);

        // then
        assertThrows(CustomException.class, () -> assetService.getAsset(id));
    }

    @Test
    @DisplayName("에셋에 카테고리 할당 시 에셋의 카테고리가 업데이트된다")
    void assignCategory_UpdatesAssetCategory() {
        // given
        // 카테고리 없이 에셋 생성
        AssetCreateRequest requestWithoutCategory = new AssetCreateRequest(
                "카테고리 없는 에셋",
                "NCA",
                assetFileId,
                thumbnailFileId,
                null
        );
        Long assetId = assetService.createAsset(requestWithoutCategory);
        AssetResponse assetBeforeAssign = assetService.getAsset(assetId);
        assertThat(assetBeforeAssign.categoryId()).isNull();

        // when
        assetService.assignCategory(assetId, categoryId);

        // then
        AssetResponse updatedAsset = assetService.getAsset(assetId);
        assertThat(updatedAsset.categoryId()).isEqualTo(categoryId);
        assertThat(updatedAsset.categoryName()).isEqualTo(categoryName);
        assertThat(updatedAsset.categoryCode()).isEqualTo(categoryCode);
    }

    @Test
    @DisplayName("에셋에서 카테고리 제거 시 에셋의 카테고리가 null이 된다")
    void removeCategory_SetsAssetCategoryToNull() {
        // given
        Long assetId = assetService.createAsset(createRequest);
        AssetResponse asset = assetService.getAsset(assetId);
        assertThat(asset.categoryId()).isNotNull();

        // when
        assetService.removeCategory(assetId);

        // then
        AssetResponse updatedAsset = assetService.getAsset(assetId);
        assertThat(updatedAsset.categoryId()).isNull();
        assertThat(updatedAsset.categoryName()).isNull();
        assertThat(updatedAsset.categoryCode()).isNull();
    }

    @Test
    @DisplayName("카테고리가 없는 에셋에서 카테고리 제거 시도 시 예외가 발생한다")
    void removeCategory_FromAssetWithNoCategory_ThrowsCustomException() {
        // given
        AssetCreateRequest requestWithoutCategory = new AssetCreateRequest(
                "카테고리 없는 에셋",
                "NCA",
                assetFileId,
                thumbnailFileId,
                null
        );
        Long assetId = assetService.createAsset(requestWithoutCategory);

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> assetService.removeCategory(assetId));
        assertThat(exception.getHttpStatus()).isEqualTo(org.springframework.http.HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo(String.format("에셋 [%d]에 할당된 카테고리가 없습니다", assetId));
    }
}
