package com.pluxity.asset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pluxity.asset.dto.AssetCategoryAllResponse;
import com.pluxity.asset.dto.AssetCategoryCreateRequest;
import com.pluxity.asset.dto.AssetCategoryResponse;
import com.pluxity.asset.dto.AssetCategoryUpdateRequest;
import com.pluxity.asset.entity.Asset;
import com.pluxity.asset.entity.AssetCategory;
import com.pluxity.asset.repository.AssetCategoryRepository;
import com.pluxity.asset.repository.AssetRepository;
import com.pluxity.asset.service.AssetCategoryService;
import com.pluxity.global.exception.CustomException;
import com.pluxity.util.TestFileUploader;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AssetCategoryServiceTest {

    @Autowired private AssetCategoryService assetCategoryService;
    @Autowired private AssetCategoryRepository assetCategoryRepository;
    @Autowired private AssetRepository assetRepository;
    @Autowired private TestFileUploader testFileUploader;

    // --- Create Test ---

    @Test
    @DisplayName("성공: 유효한 요청으로 최상위 카테고리(depth=1)를 생성하고 모든 응답 필드를 검증한다")
    void createAssetCategory_withValidRequest_savesRootCategory() {
        // GIVEN
        Long thumbnailFileId = testFileUploader.initiateTestFileUpload("icon.png");
        AssetCategoryCreateRequest request = new AssetCategoryCreateRequest("가전", "ELEC", null, thumbnailFileId);

        // WHEN
        Long categoryId = assetCategoryService.createAssetCategory(request);

        // THEN: DB에서 직접 조회하여 모든 필드를 상세히 검증
        AssetCategory savedCategory = assetCategoryRepository.findById(categoryId).orElseThrow();
        assertThat(savedCategory.getName()).isEqualTo("가전");
        assertThat(savedCategory.getCode()).isEqualTo("ELEC");
        assertThat(savedCategory.getParent()).isNull();
        assertThat(savedCategory.getDepth()).isEqualTo(1); // depth는 항상 1이어야 함
        assertThat(savedCategory.getIconFileId()).isEqualTo(thumbnailFileId);
        assertThat(savedCategory.getAssets()).isEmpty();
        assertThat(savedCategory.getChildren()).isEmpty(); // 자식은 항상 비어있어야 함
    }

    @Test
    @DisplayName("실패: 부모 ID를 지정하여 자식 카테고리(depth>1) 생성을 시도하면 예외가 발생한다")
    void createAssetCategory_withParentId_throwsException() {
        // GIVEN: 부모 카테고리 생성
        Long parentId = createAndSaveCategory("가전", "ELEC", null);
        AssetCategoryCreateRequest childRequest = new AssetCategoryCreateRequest("TV", "TV", parentId, null);

        // WHEN & THEN
        assertThrows(CustomException.class, () -> assetCategoryService.createAssetCategory(childRequest));
    }

    @Test
    @DisplayName("실패: 중복된 코드로 생성 시도 시 CustomException이 발생한다")
    void createAssetCategory_withDuplicateCode_throwsException() {
        // GIVEN
        createAndSaveCategory("가전", "ELEC", null);
        AssetCategoryCreateRequest duplicateRequest = new AssetCategoryCreateRequest("전자제품", "ELEC", null, null);

        // WHEN & THEN
        assertThrows(CustomException.class, () -> assetCategoryService.createAssetCategory(duplicateRequest));
    }

    @Test
    @DisplayName("실패: 존재하지 않는 부모 ID로 생성 시도 시 CustomException이 발생한다")
    void createAssetCategory_withNonExistentParentId_throwsException() {
        // GIVEN
        AssetCategoryCreateRequest request = new AssetCategoryCreateRequest("자식", "CHILD", 9999L, null);
        // WHEN & THEN
        assertThrows(CustomException.class, () -> assetCategoryService.createAssetCategory(request));
    }


    // --- Read Test ---

    @Test
    @DisplayName("성공: 전체 카테고리 조회 시 모든 카테고리가 depth=1이며 자식이 없는지 검증한다")
    void getAllCategories_returnsFlatListOfRootCategories() {
        // GIVEN: 여러 개의 최상위 카테고리 생성
        Long root1IconId = testFileUploader.initiateTestFileUpload("root1.png");
        createAndSaveCategory("가전", "ELEC", null, root1IconId);
        createAndSaveCategory("가구", "FURN", null);

        // WHEN
        AssetCategoryAllResponse response = assetCategoryService.getAllCategories();
        List<AssetCategoryResponse> rootCategories = response.list();

        // THEN: 생성된 모든 카테고리가 최상위 레벨에 존재
        assertThat(rootCategories).hasSize(2);
        // 모든 카테고리의 depth는 1이고, parentId는 null이며, children은 비어있어야 함
        assertThat(rootCategories).allSatisfy(category -> {
            assertThat(category.depth()).isEqualTo(1);
            assertThat(category.parentId()).isNull();
            assertThat(category.children()).isEmpty();
        });
    }

    // --- Update Test ---

    @Test
    @DisplayName("성공: 카테고리의 이름, 코드, 썸네일 정보를 정상적으로 수정한다 (부모 ID는 null)")
    void updateAssetCategory_withoutParentChange_updatesSuccessfully() {
        // GIVEN
        Long categoryId = createAndSaveCategory("원본", "ORI", null, null);
        Long newIconId = testFileUploader.initiateTestFileUpload("new.png");
        AssetCategoryUpdateRequest request = new AssetCategoryUpdateRequest("수정된 이름", "UPD", null, newIconId);

        // WHEN
        assetCategoryService.updateAssetCategory(categoryId, request);

        // THEN
        AssetCategory updatedCategory = assetCategoryRepository.findById(categoryId).orElseThrow();
        assertThat(updatedCategory.getName()).isEqualTo("수정된 이름");
        assertThat(updatedCategory.getCode()).isEqualTo("UPD");
        assertThat(updatedCategory.getIconFileId()).isEqualTo(newIconId);
        assertThat(updatedCategory.getParent()).isNull(); // 부모는 변경되지 않음
        assertThat(updatedCategory.getDepth()).isEqualTo(1); // 깊이는 변경되지 않음
    }


    @Test
    @DisplayName("실패: 카테고리 수정 시 부모를 지정하려고 하면 예외가 발생한다")
    void updateAssetCategory_withParentId_throwsException() {
        // GIVEN
        Long categoryId = createAndSaveCategory("카테고리", "CAT", null);
        Long newParentId = createAndSaveCategory("새 부모", "NEW_P", null);
        AssetCategoryUpdateRequest request = new AssetCategoryUpdateRequest("이름변경", "CAT_UPDATED", newParentId, null);

        // WHEN & THEN
        assertThrows(CustomException.class, () -> assetCategoryService.updateAssetCategory(categoryId, request));
    }


    // --- Delete Test ---

    @Test
    @DisplayName("성공: 할당된 에셋이 없을 때 정상적으로 삭제된다")
    void deleteAssetCategory_withNoRelations_deletesSuccessfully() {
        // GIVEN
        Long categoryId = createAndSaveCategory("삭제될 카테고리", "DEL", null);

        // WHEN
        assetCategoryService.deleteAssetCategory(categoryId);

        // THEN
        assertThat(assetCategoryRepository.findById(categoryId)).isEmpty();
    }

    @Test
    @DisplayName("실패: 할당된 에셋이 존재할 때 삭제 시도 시 CustomException이 발생한다")
    void deleteAssetCategory_withAssignedAssets_throwsException() {
        // GIVEN
        Long categoryId = createAndSaveCategory("카테고리", "CAT", null);
        AssetCategory category = assetCategoryRepository.findById(categoryId).orElseThrow();
        assetRepository.save(Asset.builder().name("에셋").code("A01").category(category).build());

        // WHEN & THEN
        assertThrows(CustomException.class, () -> assetCategoryService.deleteAssetCategory(categoryId));
    }

    // --- Helper Methods ---
    private Long createAndSaveCategory(String name, String code, Long parentId) {
        return createAndSaveCategory(name, code, parentId, null);
    }

    private Long createAndSaveCategory(String name, String code, Long parentId, Long thumbnailId) {
        AssetCategoryCreateRequest request = new AssetCategoryCreateRequest(name, code, parentId, thumbnailId);
        return assetCategoryService.createAssetCategory(request);
    }
}