package com.pluxity.asset.service;

import com.pluxity.asset.dto.AssetCreateRequest;
import com.pluxity.asset.dto.AssetResponse;
import com.pluxity.asset.dto.AssetUpdateRequest;
import com.pluxity.asset.entity.Asset;
import com.pluxity.asset.repository.AssetRepository;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
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
    private AssetRepository assetRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private AssetService assetService;

    private Long fileId;

    @BeforeEach
    void setup() throws IOException {
        // 테스트 이미지 파일 준비
        ClassPathResource resource = new ClassPathResource("temp/temp.png");
        byte[] fileContent = Files.readAllBytes(Path.of(resource.getURI()));

        // MockMultipartFile 생성
        MultipartFile multipartFile = new MockMultipartFile(
                "temp.png",  // 파일명
                "temp.png",  // 원본 파일명
                "image/png", // 컨텐츠 타입
                fileContent  // 파일 내용
        );

        // FileService를 통해 파일 업로드 초기화
        fileId = fileService.initiateUpload(multipartFile);
    }

    @Test
    @DisplayName("유효한 요청으로 에셋 생성 시 에셋과 관련 파일이 저장된다")
    void createAsset_WithValidRequest_SavesAssetAndFinalizeFile() {
        // given
        AssetCreateRequest request = new AssetCreateRequest(
                "테스트 에셋",
                fileId
        );

        // when
        Long savedId = assetService.createAsset(request);

        // then
        assertThat(savedId).isNotNull();
        
        // 저장된 에셋 확인
        AssetResponse savedAsset = assetService.getAsset(savedId);
        assertThat(savedAsset).isNotNull();
        assertThat(savedAsset.name()).isEqualTo("테스트 에셋");
        assertThat(savedAsset.file()).isNotNull();
    }

    @Test
    @DisplayName("파일 ID가 없는 요청으로 에셋 생성 시 에셋만 저장된다")
    void createAsset_WithoutFileId_SavesOnlyAsset() {
        // given
        AssetCreateRequest request = new AssetCreateRequest(
                "테스트 에셋",
                null
        );

        // when
        Long savedId = assetService.createAsset(request);

        // then
        assertThat(savedId).isNotNull();
        
        // 저장된 에셋 확인
        AssetResponse savedAsset = assetService.getAsset(savedId);
        assertThat(savedAsset).isNotNull();
        assertThat(savedAsset.name()).isEqualTo("테스트 에셋");
        assertThat(savedAsset.file().id()).isNull();
    }

    @Test
    @DisplayName("존재하는 ID로 에셋 조회 시 에셋 정보가 반환된다")
    void getAsset_WithExistingId_ReturnsAssetResponse() {
        // given
        Asset asset = Asset.builder()
                .name("테스트 에셋")
                .build();
        Asset savedAsset = assetRepository.save(asset);

        // when
        AssetResponse response = assetService.getAsset(savedAsset.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(savedAsset.getId());
        assertThat(response.name()).isEqualTo("테스트 에셋");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 에셋 조회 시 예외가 발생한다")
    void getAsset_WithNonExistingId_ThrowsCustomException() {
        // given
        Long nonExistingId = 999L;

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> assetService.getAsset(nonExistingId));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("모든 에셋 조회 시 에셋 목록이 반환된다")
    void getAssets_ReturnsListOfAssetResponses() {
        // given
        Asset asset1 = Asset.builder()
                .name("에셋 1")
                .build();

        Asset asset2 = Asset.builder()
                .name("에셋 2")
                .build();

        assetRepository.save(asset1);
        assetRepository.save(asset2);

        // when
        List<AssetResponse> responses = assetService.getAssets();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses).extracting("name")
                .contains("에셋 1", "에셋 2");
    }

    @Test
    @DisplayName("유효한 요청으로 에셋 수정 시 에셋 정보가 업데이트된다")
    void updateAsset_WithValidRequest_UpdatesAsset() {
        // given
        Asset asset = Asset.builder()
                .name("원본 에셋")
                .build();
        Asset savedAsset = assetRepository.save(asset);

        AssetUpdateRequest request = new AssetUpdateRequest(
                "수정된 에셋",
                null
        );

        // when
        assetService.updateAsset(savedAsset.getId(), request);

        // then
        AssetResponse updatedAsset = assetService.getAsset(savedAsset.getId());
        assertThat(updatedAsset).isNotNull();
        assertThat(updatedAsset.name()).isEqualTo("수정된 에셋");
    }

    @Test
    @DisplayName("파일 ID가 포함된 요청으로 에셋 수정 시 에셋 정보와 파일 정보가 업데이트된다")
    void updateAsset_WithFileId_UpdatesAssetAndFileId() {
        // given
        Asset asset = Asset.builder()
                .name("원본 에셋")
                .build();
        Asset savedAsset = assetRepository.save(asset);

        AssetUpdateRequest request = new AssetUpdateRequest(
                "수정된 에셋",
                fileId
        );

        // when
        assetService.updateAsset(savedAsset.getId(), request);

        // then
        AssetResponse updatedAsset = assetService.getAsset(savedAsset.getId());
        assertThat(updatedAsset).isNotNull();
        assertThat(updatedAsset.name()).isEqualTo("수정된 에셋");
        assertThat(updatedAsset.file()).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 ID로 에셋 수정 시 예외가 발생한다")
    void updateAsset_WithNonExistingId_ThrowsCustomException() {
        // given
        Long nonExistingId = 999L;
        AssetUpdateRequest request = new AssetUpdateRequest(
                "수정된 에셋",
                null
        );

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> assetService.updateAsset(nonExistingId, request));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    @DisplayName("에셋 삭제 시 에셋이 삭제된다")
    void deleteAsset_DeletesAsset() {
        // given
        Asset asset = Asset.builder()
                .name("테스트 에셋")
                .build();
        Asset savedAsset = assetRepository.save(asset);

        // when
        assetService.deleteAsset(savedAsset.getId());

        // then
        assertThat(assetRepository.findById(savedAsset.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 ID로 에셋 삭제 시 예외가 발생한다")
    void deleteAsset_WithNonExistingId_ThrowsCustomException() {
        // given
        Long nonExistingId = 999L;

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> assetService.deleteAsset(nonExistingId));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}