package com.pluxity.asset.service;

import com.pluxity.asset.dto.AssetCategoryCreateRequest;
import com.pluxity.asset.dto.AssetCreateRequest;
import com.pluxity.asset.dto.AssetResponse;
import com.pluxity.asset.dto.AssetUpdateRequest;
import com.pluxity.asset.entity.Asset;
import com.pluxity.asset.repository.AssetRepository;
import com.pluxity.feature.repository.FeatureRepository;
import com.pluxity.feature.service.FeatureService;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
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

    @Autowired
    private FeatureService featureService;

    @Autowired
    private FeatureRepository featureRepository;

    private Long assetFileId;
    private Long thumbnailFileId;
    private Long categoryId;
    private String categoryCode = "TCC";
    private String categoryName = "테스트 카테고리";
    private AssetCreateRequest createRequest;
    private byte[] fileContent;

    @BeforeEach
    void setUp() throws IOException {
        // 테스트 이미지 파일 준비
        ClassPathResource resource = new ClassPathResource("temp/temp.png");
        fileContent = Files.readAllBytes(Path.of(resource.getURI()));

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

    // 새로운 파일 ID를 생성하는 헬퍼 메서드
    private Long createNewFileId() throws IOException {
        MultipartFile file = new MockMultipartFile(
                "new_file.png", "new_file.png", "image/png", fileContent);
        return fileService.initiateUpload(file);
    }

    @Test
    @DisplayName("유효한 요청으로 에셋 생성 시 에셋이 저장된다")
    void createAsset_WithValidRequest_SavesAsset() throws IOException {
        // 새로운 파일 ID 생성
        Long newAssetFileId = createNewFileId();
        Long newThumbnailFileId = createNewFileId();
        
        // 생성 요청 객체 업데이트
        AssetCreateRequest request = new AssetCreateRequest(
                createRequest.name(),
                createRequest.code(),
                newAssetFileId,
                newThumbnailFileId,
                createRequest.categoryId()
        );
        
        // when
        Long id = assetService.createAsset(request);

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
    void getAssets_ReturnsListOfAssetResponses() throws IOException {
        // 새로운 파일 ID 생성
        Long newAssetFileId = createNewFileId();
        Long newThumbnailFileId = createNewFileId();
        
        // 생성 요청 객체 업데이트
        AssetCreateRequest request = new AssetCreateRequest(
                createRequest.name(),
                createRequest.code(),
                newAssetFileId,
                newThumbnailFileId,
                createRequest.categoryId()
        );
        
        // given
        assetService.createAsset(request);

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
    void getAsset_WithExistingId_ReturnsAssetResponse() throws IOException {
        // 새로운 파일 ID 생성
        Long newAssetFileId = createNewFileId();
        Long newThumbnailFileId = createNewFileId();
        
        // 생성 요청 객체 업데이트
        AssetCreateRequest request = new AssetCreateRequest(
                createRequest.name(),
                createRequest.code(),
                newAssetFileId,
                newThumbnailFileId,
                createRequest.categoryId()
        );
        
        // given
        Long id = assetService.createAsset(request);

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
        // 새로운 파일 ID 생성 (에셋 생성용)
        Long newAssetFileId = createNewFileId();
        Long newThumbnailFileId = createNewFileId();
        
        // 생성 요청 객체 업데이트
        AssetCreateRequest request = new AssetCreateRequest(
                createRequest.name(),
                createRequest.code(),
                newAssetFileId,
                newThumbnailFileId,
                createRequest.categoryId()
        );
        
        // given
        Long id = assetService.createAsset(request);

        // 새로운 파일 업로드 (업데이트용)
        ClassPathResource newResource = new ClassPathResource("temp/temp2.png");
        byte[] newFileContent = Files.readAllBytes(Path.of(newResource.getURI()));
        MultipartFile newAssetFile = new MockMultipartFile("new_asset.png", "new_asset.png", "image/png", newFileContent);
        Long updateAssetFileId = fileService.initiateUpload(newAssetFile);

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
                updateAssetFileId,
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
    void deleteAsset_RemovesAsset() throws IOException {
        // 새로운 파일 ID 생성
        Long newAssetFileId = createNewFileId();
        Long newThumbnailFileId = createNewFileId();
        
        // 생성 요청 객체 업데이트
        AssetCreateRequest request = new AssetCreateRequest(
                createRequest.name(),
                createRequest.code(),
                newAssetFileId,
                newThumbnailFileId,
                createRequest.categoryId()
        );
        
        // given
        Long id = assetService.createAsset(request);
        AssetResponse response = assetService.getAsset(id);
        assertThat(response).isNotNull();

        // when
        assetService.deleteAsset(id);

        // then
        assertThrows(CustomException.class, () -> assetService.getAsset(id));
    }

    @Test
    @DisplayName("에셋에 카테고리 할당 시 에셋의 카테고리가 업데이트된다")
    void assignCategory_UpdatesAssetCategory() throws IOException {
        // 새로운 파일 ID 생성
        Long newAssetFileId = createNewFileId();
        Long newThumbnailFileId = createNewFileId();
        
        // given
        // 카테고리 없이 에셋 생성
        AssetCreateRequest requestWithoutCategory = new AssetCreateRequest(
                "카테고리 없는 에셋",
                "NCA",
                newAssetFileId,
                newThumbnailFileId,
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
    void removeCategory_SetsAssetCategoryToNull() throws IOException {
        // 새로운 파일 ID 생성
        Long newAssetFileId = createNewFileId();
        Long newThumbnailFileId = createNewFileId();
        
        // 생성 요청 객체 업데이트
        AssetCreateRequest request = new AssetCreateRequest(
                createRequest.name(),
                createRequest.code(),
                newAssetFileId,
                newThumbnailFileId,
                createRequest.categoryId()
        );
        
        // given
        Long assetId = assetService.createAsset(request);
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
    void removeCategory_FromAssetWithNoCategory_ThrowsCustomException() throws IOException {
        // 새로운 파일 ID 생성
        Long newAssetFileId = createNewFileId();
        Long newThumbnailFileId = createNewFileId();
        
        // given
        AssetCreateRequest requestWithoutCategory = new AssetCreateRequest(
                "카테고리 없는 에셋",
                "NCA",
                newAssetFileId,
                newThumbnailFileId,
                null
        );
        Long assetId = assetService.createAsset(requestWithoutCategory);

        // when & then
        CustomException exception = assertThrows(CustomException.class, () -> assetService.removeCategory(assetId));
        assertThat(exception.getHttpStatus()).isEqualTo(org.springframework.http.HttpStatus.BAD_REQUEST);
        assertThat(exception.getMessage()).isEqualTo(String.format("에셋 [%d]에 할당된 카테고리가 없습니다", assetId));
    }

    @Test
    @DisplayName("중복된 코드로 에셋 생성 시 예외가 발생한다")
    void createAsset_WithDuplicateCode_ThrowsCustomException() throws IOException {
        // 새로운 파일 ID 생성 (첫 번째 에셋용)
        Long firstAssetFileId = createNewFileId();
        Long firstThumbnailFileId = createNewFileId();
        
        // 첫 번째 에셋 생성 요청
        AssetCreateRequest firstRequest = new AssetCreateRequest(
                createRequest.name(),
                createRequest.code(),
                firstAssetFileId,
                firstThumbnailFileId,
                createRequest.categoryId()
        );
        
        // 1. 첫 번째 에셋 생성
        Long id1 = assetService.createAsset(firstRequest);

        // 새로운 파일 ID 생성 (두 번째 에셋용)
        Long secondAssetFileId = createNewFileId();
        Long secondThumbnailFileId = createNewFileId();
        
        // 2. 동일한 코드로 두 번째 에셋 생성 시도
        AssetCreateRequest duplicateRequest = new AssetCreateRequest(
                "다른 에셋",
                "TES",  // 중복 코드
                secondAssetFileId,
                secondThumbnailFileId,
                categoryId
        );

        // when & then
        assertThrows(CustomException.class, () -> assetService.createAsset(duplicateRequest));
    }

    @Test
    @DisplayName("유효하지 않은 파일 ID로 에셋 생성 시 예외가 발생한다")
    void createAsset_WithInvalidFileId_ThrowsCustomException() {
        // given
        Long invalidFileId = 9999L;
        AssetCreateRequest invalidRequest = new AssetCreateRequest(
                "테스트 에셋",
                "INV",
                invalidFileId,
                thumbnailFileId,
                categoryId
        );

        // when & then
        assertThrows(CustomException.class, () -> assetService.createAsset(invalidRequest));
    }

    @Test
    @DisplayName("유효하지 않은 썸네일 파일 ID로 에셋 생성 시 예외가 발생한다")
    void createAsset_WithInvalidThumbnailFileId_ThrowsCustomException() throws IOException {
        // 새로운 파일 ID 생성
        Long newAssetFileId = createNewFileId();
        
        // given
        Long invalidThumbnailId = 9999L;
        AssetCreateRequest invalidRequest = new AssetCreateRequest(
                "테스트 에셋",
                "INV",
                newAssetFileId,
                invalidThumbnailId,
                categoryId
        );

        // when & then
        assertThrows(CustomException.class, () -> assetService.createAsset(invalidRequest));
    }

    @Test
    @DisplayName("유효하지 않은 카테고리 ID로 에셋 생성 시 예외가 발생한다")
    void createAsset_WithInvalidCategoryId_ThrowsCustomException() throws IOException {
        // 새로운 파일 ID 생성
        Long newAssetFileId = createNewFileId();
        Long newThumbnailFileId = createNewFileId();
        
        // given
        Long invalidCategoryId = 9999L;
        AssetCreateRequest invalidRequest = new AssetCreateRequest(
                "테스트 에셋",
                "CAT",
                newAssetFileId,
                newThumbnailFileId,
                invalidCategoryId
        );

        // when & then
        assertThrows(CustomException.class, () -> assetService.createAsset(invalidRequest));
    }

    @Test
    @DisplayName("에셋 업데이트 시 중복 코드로 변경 시도할 때 예외가 발생한다")
    void updateAsset_WithDuplicateCode_ThrowsCustomException() throws IOException {
        // 새로운 파일 ID 생성 (첫 번째 에셋용)
        Long firstAssetFileId = createNewFileId();
        Long firstThumbnailFileId = createNewFileId();
        
        // 첫 번째 에셋 생성
        AssetCreateRequest firstRequest = new AssetCreateRequest(
                "첫 번째 에셋",
                "TES",
                firstAssetFileId,
                firstThumbnailFileId,
                categoryId
        );
        Long id1 = assetService.createAsset(firstRequest);

        // 새로운 파일 ID 생성 (두 번째 에셋용)
        Long secondAssetFileId = createNewFileId();
        Long secondThumbnailFileId = createNewFileId();
        
        // 두 번째 에셋 생성
        AssetCreateRequest secondRequest = new AssetCreateRequest(
                "두 번째 에셋",
                "SEC",
                secondAssetFileId,
                secondThumbnailFileId,
                categoryId
        );
        Long id2 = assetService.createAsset(secondRequest);

        // 새로운 파일 ID 생성 (업데이트용)
        Long updateAssetFileId = createNewFileId();
        Long updateThumbnailFileId = createNewFileId();
        
        // 두 번째 에셋을 첫 번째 에셋과 동일한 코드로 업데이트 시도
        AssetUpdateRequest updateRequest = new AssetUpdateRequest(
                "수정된 에셋",
                "TES",  // 첫 번째 에셋과 동일한 코드
                updateAssetFileId,
                updateThumbnailFileId,
                categoryId
        );

        // when & then
        assertThrows(CustomException.class, () -> assetService.updateAsset(id2, updateRequest));
    }

    @Test
    @DisplayName("존재하지 않는 에셋을 업데이트 시도할 때 예외가 발생한다")
    void updateAsset_WithNonExistingId_ThrowsCustomException() throws IOException {
        // 새로운 파일 ID 생성
        Long newAssetFileId = createNewFileId();
        Long newThumbnailFileId = createNewFileId();
        
        // given
        Long nonExistingId = 9999L;
        AssetUpdateRequest updateRequest = new AssetUpdateRequest(
                "수정된 에셋",
                "UPD",
                newAssetFileId,
                newThumbnailFileId,
                categoryId
        );

        // when & then
        assertThrows(CustomException.class, () -> assetService.updateAsset(nonExistingId, updateRequest));
    }

    @Test
    @DisplayName("존재하지 않는 에셋을 삭제 시도할 때 예외가 발생한다")
    void deleteAsset_WithNonExistingId_ThrowsCustomException() {
        // given
        Long nonExistingId = 9999L;

        // when & then
        assertThrows(CustomException.class, () -> assetService.deleteAsset(nonExistingId));
    }

    @Test
    @DisplayName("카테고리별 에셋 조회 시 해당 카테고리의 에셋만 반환된다")
    void getAssetsByCategory_ReturnsAssetsOfSpecifiedCategory() throws IOException {
        // 첫 번째 에셋 생성용 파일 ID
        Long firstAssetFileId = createNewFileId();
        Long firstThumbnailFileId = createNewFileId();
        
        // 1. 첫 번째 카테고리의 에셋 생성
        AssetCreateRequest firstAssetRequest = new AssetCreateRequest(
                "첫 번째 에셋",
                "AS1",
                firstAssetFileId,
                firstThumbnailFileId,
                categoryId
        );
        Long assetId1 = assetService.createAsset(firstAssetRequest);

        // 2. 두 번째 카테고리 생성
        AssetCategoryCreateRequest secondCategoryRequest = new AssetCategoryCreateRequest(
                "두 번째 카테고리",
                "SEC",
                null,
                null
        );
        Long secondCategoryId = assetCategoryService.createAssetCategory(secondCategoryRequest);

        // 두 번째 에셋 생성용 파일 ID
        Long secondAssetFileId = createNewFileId();
        Long secondThumbnailFileId = createNewFileId();
        
        // 3. 두 번째 카테고리의 에셋 생성
        AssetCreateRequest secondAssetRequest = new AssetCreateRequest(
                "두 번째 에셋",
                "AS2",
                secondAssetFileId,
                secondThumbnailFileId,
                secondCategoryId
        );
        Long assetId2 = assetService.createAsset(secondAssetRequest);

        // when
        List<AssetResponse> firstCategoryAssets = assetService.getAssetsByCategory(categoryId);
        List<AssetResponse> secondCategoryAssets = assetService.getAssetsByCategory(secondCategoryId);

        // then
        assertThat(firstCategoryAssets).hasSize(1);
        assertThat(firstCategoryAssets.getFirst().id()).isEqualTo(assetId1);
        assertThat(firstCategoryAssets.getFirst().categoryId()).isEqualTo(categoryId);

        assertThat(secondCategoryAssets).hasSize(1);
        assertThat(secondCategoryAssets.getFirst().id()).isEqualTo(assetId2);
        assertThat(secondCategoryAssets.getFirst().categoryId()).isEqualTo(secondCategoryId);
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 ID로 에셋에 카테고리 할당 시도 시 예외가 발생한다")
    void assignCategory_WithNonExistingCategoryId_ThrowsCustomException() throws IOException {
        // 새로운 파일 ID 생성
        Long newAssetFileId = createNewFileId();
        Long newThumbnailFileId = createNewFileId();
        
        // 생성 요청 객체 업데이트
        AssetCreateRequest request = new AssetCreateRequest(
                createRequest.name(),
                createRequest.code(),
                newAssetFileId,
                newThumbnailFileId,
                createRequest.categoryId()
        );
        
        // given
        Long assetId = assetService.createAsset(request);
        Long nonExistingCategoryId = 9999L;

        // when & then
        assertThrows(CustomException.class, () -> assetService.assignCategory(assetId, nonExistingCategoryId));
    }

    @Test
    @DisplayName("에셋 코드로 에셋 조회 시 해당 코드의 에셋이 반환된다")
    void getAssetByCode_ReturnsAssetWithSpecifiedCode() throws IOException {
        // 새로운 파일 ID 생성
        Long newAssetFileId = createNewFileId();
        Long newThumbnailFileId = createNewFileId();
        
        // 생성 요청 객체 업데이트
        AssetCreateRequest request = new AssetCreateRequest(
                createRequest.name(),
                createRequest.code(),
                newAssetFileId,
                newThumbnailFileId,
                createRequest.categoryId()
        );
        
        // given
        Long id = assetService.createAsset(request);
        String assetCode = "TES";

        // when
        AssetResponse asset = assetService.getAssetByCode(assetCode);

        // then
        assertThat(asset).isNotNull();
        assertThat(asset.id()).isEqualTo(id);
        assertThat(asset.code()).isEqualTo(assetCode);
    }

    @Test
    @DisplayName("존재하지 않는 코드로 에셋 조회 시 예외가 발생한다")
    void getAssetByCode_WithNonExistingCode_ThrowsCustomException() {
        // given
        String nonExistingCode = "NON";

        // when & then
        assertThrows(CustomException.class, () -> assetService.getAssetByCode(nonExistingCode));
    }

//    @Test
//    @DisplayName("에셋 삭제 시 연관된 모든 피처가 함께 삭제된다")
//    TODO: 이 테스트는 FeatureService와의 연관관계가 필요합니다.
//    void deleteAsset_RemovesAllRelatedFeatures() throws IOException {
//        // given
//        // 1. 에셋 생성
//        Long newAssetFileId = createNewFileId();
//        Long newThumbnailFileId = createNewFileId();
//
//        AssetCreateRequest request = new AssetCreateRequest(
//                "피처 테스트 에셋",
//                "FTR",
//                newAssetFileId,
//                newThumbnailFileId,
//                createRequest.categoryId()
//        );
//
//        Long assetId = assetService.createAsset(request);
//        Asset asset = assetRepository.findById(assetId).orElseThrow();
//
//        // 2. 피처 생성 및 에셋에 연결
//        String featureId1 = UUID.randomUUID().toString();
//        String featureId2 = UUID.randomUUID().toString();
//
//        // 피처 직접 생성하여 저장
//        Feature feature1 = Feature.builder()
//                .id(featureId1)
//                .position(new Spatial(1.0, 1.0, 1.0))
//                .rotation(new Spatial(0.0, 0.0, 0.0))
//                .scale(new Spatial(1.0, 1.0, 1.0))
//                .asset(asset)
//                .build();
//
//        Feature feature2 = Feature.builder()
//                .id(featureId2)
//                .position(new Spatial(2.0, 2.0, 2.0))
//                .rotation(new Spatial(0.0, 0.0, 0.0))
//                .scale(new Spatial(1.0, 1.0, 1.0))
//                .asset(asset)
//                .build();
//
//        featureRepository.save(feature1);
//        featureRepository.save(feature2);
//
//        // 피처가 에셋에 연결되었는지 확인
//        asset = assetRepository.findById(assetId).orElseThrow(); // 다시 로드하여 확인
//        assertEquals(2, asset.getFeatures().size());
//
//        // when
//        assetService.deleteAsset(assetId);
//
//        // then
//        // 1. 에셋이 삭제되었는지 확인
//        assertThrows(CustomException.class, () -> assetService.findById(assetId));
//
//        // 2. 연결되었던 피처들도 모두 삭제되었는지 확인
//        assertFalse(featureRepository.findById(featureId1).isPresent());
//        assertFalse(featureRepository.findById(featureId2).isPresent());
//    }

    @Test
    @DisplayName("에셋 삭제 시 연관관계 제거 실패하면 예외 발생")
    void deleteAsset_ThrowsExceptionWhenRelationRemovalFails() throws IOException {
        // given
        // 1. 에셋 생성
        Long newAssetFileId = createNewFileId();
        Long newThumbnailFileId = createNewFileId();
        
        AssetCreateRequest request = new AssetCreateRequest(
                "예외 테스트 에셋",
                "EXC",
                newAssetFileId,
                newThumbnailFileId,
                createRequest.categoryId()
        );
        
        Long assetId = assetService.createAsset(request);
        
        // 에셋 스파이 생성
        Asset asset = assetRepository.findById(assetId).orElseThrow();
        Asset spyAsset = Mockito.spy(asset);
        
        // clearAllRelations 호출 시 예외 발생하도록 설정
        Mockito.doThrow(new RuntimeException("관계 제거 실패")).when(spyAsset).clearAllRelations();
        
        // 목 레포지토리 설정
        AssetRepository mockRepo = Mockito.mock(AssetRepository.class);
        Mockito.when(mockRepo.findById(assetId)).thenReturn(java.util.Optional.of(spyAsset));
        
        // 원본 레포지토리 저장
        AssetRepository originalRepo = (AssetRepository) ReflectionTestUtils.getField(
                assetService, "assetRepository");
        
        // 목 주입
        ReflectionTestUtils.setField(assetService, "assetRepository", mockRepo);
        
        try {
            // when & then
            assertThrows(RuntimeException.class, () -> assetService.deleteAsset(assetId));
            
        } finally {
            // 원래 레포지토리 복원
            ReflectionTestUtils.setField(assetService, "assetRepository", originalRepo);
        }
    }
}
