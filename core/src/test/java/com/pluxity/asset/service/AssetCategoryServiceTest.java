package com.pluxity.asset.service;

import com.pluxity.asset.dto.AssetCategoryCreateRequest;
import com.pluxity.asset.dto.AssetCategoryResponse;
import com.pluxity.asset.dto.AssetCategoryUpdateRequest;
import com.pluxity.asset.repository.AssetCategoryRepository;
import com.pluxity.file.service.FileService;
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
class AssetCategoryServiceTest {

    @Autowired
    private AssetCategoryService assetCategoryService;

    @Autowired
    private AssetCategoryRepository assetCategoryRepository;

    @Autowired
    private FileService fileService;

    private AssetCategoryCreateRequest createRequest;
    private Long iconFileId;

    @BeforeEach
    void setUp() {
        // 파일 아이디는 실제 테스트 환경에서는 fileService를 통해 얻어야 하지만,
        // 여기서는 null로 설정하고 필요시 모킹하거나 실제 파일을 생성할 수 있습니다.
        iconFileId = null;

        // 테스트 데이터 준비
        createRequest = new AssetCategoryCreateRequest(
                "테스트 카테고리",
                "TC1",
                null,
                iconFileId
        );
    }

    @Test
    @DisplayName("유효한 요청으로 에셋 카테고리 생성 시 카테고리가 저장된다")
    void createAssetCategory_WithValidRequest_SavesCategory() {
        // when
        Long id = assetCategoryService.createAssetCategory(createRequest);

        // then
        assertThat(id).isNotNull();

        // 저장된 카테고리 확인
        AssetCategoryResponse savedCategory = assetCategoryService.getAssetCategory(id);
        assertThat(savedCategory).isNotNull();
        assertThat(savedCategory.name()).isEqualTo("테스트 카테고리");
        assertThat(savedCategory.code()).isEqualTo("TC1");
    }

    @Test
    @DisplayName("모든 에셋 카테고리 조회 시 카테고리 목록이 반환된다")
    void getAssetCategories_ReturnsListOfCategoryResponses() {
        // given
        Long id = assetCategoryService.createAssetCategory(createRequest);

        // when
        List<AssetCategoryResponse> responses = assetCategoryService.getAssetCategories();

        // then
        assertThat(responses).isNotEmpty();
        assertThat(responses.stream().anyMatch(cat -> cat.name().equals("테스트 카테고리"))).isTrue();
    }

    @Test
    @DisplayName("ID로 에셋 카테고리 조회 시 카테고리 정보가 반환된다")
    void getAssetCategory_WithExistingId_ReturnsCategoryResponse() {
        // given
        Long id = assetCategoryService.createAssetCategory(createRequest);

        // when
        AssetCategoryResponse response = assetCategoryService.getAssetCategory(id);

        // then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("테스트 카테고리");
        assertThat(response.code()).isEqualTo("TC1");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 에셋 카테고리 조회 시 예외가 발생한다")
    void getAssetCategory_WithNonExistingId_ThrowsCustomException() {
        // given
        Long nonExistingId = 9999L;

        // when & then
        assertThrows(CustomException.class, () -> assetCategoryService.getAssetCategory(nonExistingId));
    }

    @Test
    @DisplayName("루트 카테고리 조회 시 상위 카테고리가 없는 카테고리만 반환된다")
    void getRootCategories_ReturnsOnlyRootCategories() {
        // given
        Long rootId1 = assetCategoryService.createAssetCategory(createRequest); // name: "테스트 카테고리", code: "TC1"
        AssetCategoryCreateRequest anotherRootRequest = new AssetCategoryCreateRequest(
                "두번째 루트 카테고리",
                "RC2", // 새로운 유니크 코드
                null, // parentId null
                null
        );
        Long rootId2 = assetCategoryService.createAssetCategory(anotherRootRequest);

        // when
        List<AssetCategoryResponse> rootCategories = assetCategoryService.getRootCategories();

        // then
        assertThat(rootCategories).hasSize(2);
        assertThat(rootCategories.stream().anyMatch(cat -> cat.name().equals("테스트 카테고리") && cat.code().equals("TC1"))).isTrue();
        assertThat(rootCategories.stream().anyMatch(cat -> cat.name().equals("두번째 루트 카테고리") && cat.code().equals("RC2"))).isTrue();
    }

    @Test
    @DisplayName("자식 카테고리 조회 시 (루트 카테고리의 경우) 빈 리스트가 반환된다")
    void getChildCategories_ReturnsChildrenOfSpecifiedParent() {
        // given
        Long rootId = assetCategoryService.createAssetCategory(createRequest); // 루트 카테고리 생성

        // AssetCategory는 최대 깊이가 1이므로, 루트 카테고리는 자식을 가질 수 없음
        // 따라서 이 테스트는 루트 카테고리의 자식 조회 시 빈 리스트를 반환하는지 확인

        // when
        List<AssetCategoryResponse> childCategories = assetCategoryService.getChildCategories(rootId);

        // then
        assertThat(childCategories).isEmpty();
    }

    @Test
    @DisplayName("유효한 요청으로 에셋 카테고리 수정 시 카테고리 정보가 업데이트된다")
    void updateAssetCategory_WithValidRequest_UpdatesCategory() {
        // given
        Long id = assetCategoryService.createAssetCategory(createRequest);
        AssetCategoryUpdateRequest updateRequest = new AssetCategoryUpdateRequest(
                "수정된 카테고리",
                "UC1",
                null,
                null
        );

        // when
        assetCategoryService.updateAssetCategory(id, updateRequest);

        // then
        AssetCategoryResponse updatedCategory = assetCategoryService.getAssetCategory(id);
        assertThat(updatedCategory.name()).isEqualTo("수정된 카테고리");
        assertThat(updatedCategory.code()).isEqualTo("UC1");
    }

    @Test
    @DisplayName("중복된 코드로 에셋 카테고리 생성 시 예외가 발생한다")
    void createAssetCategory_WithDuplicateCode_ThrowsCustomException() {
        // given
        Long id = assetCategoryService.createAssetCategory(createRequest);

        AssetCategoryCreateRequest duplicateRequest = new AssetCategoryCreateRequest(
                "다른 카테고리",
                "TC1",
                null,
                null
        );

        // when & then
        assertThrows(CustomException.class, () -> assetCategoryService.createAssetCategory(duplicateRequest));
    }

    @Test
    @DisplayName("에셋 카테고리 삭제 시 해당 카테고리가 삭제된다")
    void deleteAssetCategory_RemovesCategory() {
        // given
        Long id = assetCategoryService.createAssetCategory(createRequest);

        // when
        assetCategoryService.deleteAssetCategory(id);

        // then
        assertThrows(CustomException.class, () -> assetCategoryService.getAssetCategory(id));
    }
}
