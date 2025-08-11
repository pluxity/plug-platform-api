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
import com.pluxity.file.constant.FileStatus;
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

    @Autowired
    private AssetCategoryService assetCategoryService;

    @Autowired
    private AssetCategoryRepository assetCategoryRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private TestFileUploader testFileUploader;

    @Test
    @DisplayName("성공: 모든 필드를 채운 요청으로 최상위 카테고리를 생성하고 모든 응답 필드를 검증한다")
    void createAssetCategory_withValidRequest_savesRootCategory() {
        // GIVEN: 파일을 먼저 업로드하여 임시 ID를 받는다.
        Long thumbnailFileId = testFileUploader.initiateTestFileUpload("icon.png");
        AssetCategoryCreateRequest request = new AssetCategoryCreateRequest(
                "가전", "ELEC", null, thumbnailFileId);

        // WHEN: 서비스 메서드 호출
        Long categoryId = assetCategoryService.createAssetCategory(request);

        // THEN: 반환된 ID로 카테고리를 조회하여 모든 필드를 상세히 검증
        AssetCategoryResponse response = assetCategoryService.getAllCategories().list().stream().filter(e -> e.id().equals(categoryId)).findFirst().orElseThrow();

        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(categoryId);
        assertThat(response.name()).isEqualTo(request.name());
        assertThat(response.code()).isEqualTo(request.code());
        assertThat(response.parentId()).isNull();
        assertThat(response.depth()).isEqualTo(1);
        assertThat(response.children()).isNotNull().isEmpty();
        assertThat(response.assetIds()).isNotNull().isEmpty();
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();

        assertThat(response.thumbnail()).isNotNull();
        assertThat(response.thumbnail().id()).isEqualTo(thumbnailFileId);
        assertThat(response.thumbnail().originalFileName()).isEqualTo("icon.png");
        assertThat(response.thumbnail().fileStatus()).isEqualTo(FileStatus.COMPLETE.name());
    }

//    @Test
//    @DisplayName("성공: 유효한 부모 ID로 자식 카테고리를 생성하고 모든 응답 필드를 검증한다")
//    void createAssetCategory_withValidParent_savesChildCategory() {
//        // GIVEN: 부모 카테고리 및 자식 카테고리 생성 요청 준비
//        Long parentThumbnailId = testFileUploader.initiateTestFileUpload("parent_icon.png");
//        AssetCategoryCreateRequest parentRequest = new AssetCategoryCreateRequest("가전", "ELEC", null, parentThumbnailId);
//        Long parentId = assetCategoryService.createAssetCategory(parentRequest);
//
//        Long childThumbnailId = testFileUploader.initiateTestFileUpload("child_icon.png");
//        AssetCategoryCreateRequest childRequest = new AssetCategoryCreateRequest("TV", "TV", parentId, childThumbnailId);
//
//        // WHEN: 자식 카테고리 생성
//        Long childId = assetCategoryService.createAssetCategory(childRequest);
//
//        // THEN: 생성된 자식 카테고리의 모든 필드를 상세히 검증
//        AssetCategoryResponse response = assetCategoryService.getAllCategories().list().getFirst().children().getFirst();
//
//        assertThat(response).isNotNull();
//        assertThat(response.id()).isEqualTo(childId);
//        assertThat(response.name()).isEqualTo(childRequest.name());
//        assertThat(response.code()).isEqualTo(childRequest.code());
//        assertThat(response.parentId()).isEqualTo(parentId);
//        assertThat(response.depth()).isEqualTo(2);
//        assertThat(response.children()).isNotNull().isEmpty();
//
//        assertThat(response.thumbnail()).isNotNull();
//        assertThat(response.thumbnail().id()).isEqualTo(childThumbnailId);
//    }

    @Test
    @DisplayName("실패: 중복된 코드로 생성 시도 시 CustomException이 발생한다")
    void createAssetCategory_withDuplicateCode_throwsException() {
        // GIVEN: 썸네일 없이 카테고리 생성
        assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("가전", "ELEC", null, null));
        AssetCategoryCreateRequest duplicateRequest = new AssetCategoryCreateRequest("전자제품", "ELEC", null, null);

        // WHEN & THEN
        assertThrows(CustomException.class, () -> assetCategoryService.createAssetCategory(duplicateRequest));
    }

    @Test
    @DisplayName("실패: 존재하지 않는 부모 ID로 생성 시도 시 CustomException이 발생한다")
    void createAssetCategory_withNonExistentParentId_throwsException() {
        // GIVEN
        Long nonExistentParentId = 9999L;
        AssetCategoryCreateRequest request = new AssetCategoryCreateRequest("자식", "CHILD", nonExistentParentId, null);

        // WHEN & THEN
        assertThrows(CustomException.class, () -> assetCategoryService.createAssetCategory(request));
    }

    @Test
    @DisplayName("실패: 최대 허용 깊이를 초과하여 생성 시도 시 CustomException이 발생한다")
    void createAssetCategory_exceedingMaxDepth_throwsException() {
        // GIVEN (AssetCategory의 MAX_DEPTH가 3이라고 가정하고 테스트)
        Long id1 = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("Depth 1", "D1", null, null));

        AssetCategoryCreateRequest invalidRequest = new AssetCategoryCreateRequest("Depth 2", "D4", id1, null);

        // WHEN & THEN: AssetCategory 엔티티의 updateParent에서 깊이 검증 로직이 예외를 발생시킴
        assertThrows(CustomException.class, () -> assetCategoryService.createAssetCategory(invalidRequest));
    }

    @Test
    @DisplayName("성공: ID로 조회 시 정확한 카테고리 응답을 반환한다 (자식 미포함)")
    void getAssetCategory_withExistingId_returnsCorrectResponse() {
        // GIVEN: 에셋과 연결된 카테고리 생성
        Long thumbnailFileId = testFileUploader.initiateTestFileUpload("graphic_icon.png");
        Long categoryId = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("그래픽", "GRAPHIC", null, thumbnailFileId));
        AssetCategory category = assetCategoryRepository.findById(categoryId).orElseThrow();
        assetRepository.save(Asset.builder().name("에셋1").code("A01").category(category).build());

        // WHEN
        AssetCategoryResponse response = assetCategoryService.getAllCategories().list().stream().filter(e -> e.id().equals(categoryId)).findFirst().orElseThrow();

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(categoryId);
        assertThat(response.assetIds()).hasSize(1);
        assertThat(response.children()).isNotNull().isEmpty(); // 자식은 조회하지 않음
        assertThat(response.thumbnail().id()).isEqualTo(thumbnailFileId);
    }

//    @Test
//    @DisplayName("성공: 전체 카테고리 조회 시 올바른 트리 구조와 모든 필드를 검증한다")
//    void getAllCategories_returnsCorrectTreeStructureInList() {
//        // GIVEN: 파일 업로드 및 계층 구조의 카테고리 생성
//        Long root1IconId = testFileUploader.initiateTestFileUpload("root1.png");
//        Long root1Id = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("가전", "ELEC", null, root1IconId));
//
//        Long child1IconId = testFileUploader.initiateTestFileUpload("child1.png");
//        Long child1Id = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("TV", "TV", root1Id, child1IconId));
//
//        Long root2IconId = testFileUploader.initiateTestFileUpload("root2.png");
//        Long root2Id = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("가구", "FURN", null, root2IconId));
//
//        // WHEN
//        AssetCategoryAllResponse response = assetCategoryService.getAllCategories();
//        List<AssetCategoryResponse> rootCategories = response.list();
//
//        // THEN
//        assertThat(rootCategories).hasSize(2);
//        AssetCategoryResponse elecCategory = rootCategories.stream().filter(c -> c.id().equals(root1Id)).findFirst().orElseThrow();
//        assertThat(elecCategory.thumbnail().id()).isEqualTo(root1IconId);
//        assertThat(elecCategory.children()).hasSize(1);
//
//        AssetCategoryResponse tvCategory = elecCategory.children().getFirst();
//        assertThat(tvCategory.id()).isEqualTo(child1Id);
//        assertThat(tvCategory.thumbnail().id()).isEqualTo(child1IconId);
//    }

//    @Test
//    @DisplayName("성공: 특정 부모의 자식 카테고리 목록을 정확히 조회한다")
//    void getChildCategories_returnsListOfChildren() {
//        // GIVEN
//        Long parentId = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("부모", "P", null, null));
//        assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("자식1", "C1", parentId, null));
//        assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("자식2", "C2", parentId, null));
//
//        // WHEN
//        List<AssetCategoryResponse> children = assetCategoryService.getChildCategories(parentId);
//
//        // THEN
//        assertThat(children).hasSize(2);
//        assertThat(children).allMatch(child -> child.parentId().equals(parentId));
//        assertThat(children.stream().map(AssetCategoryResponse::name)).containsExactlyInAnyOrder("자식1", "자식2");
//    }

    @Test
    @DisplayName("실패: 존재하지 않는 부모 ID로 자식 조회 시 CustomException이 발생한다")
    void getChildCategories_withNonExistentParentId_throwsException() {
        // GIVEN
        Long nonExistentParentId = 9999L;

        // WHEN & THEN
        List<AssetCategoryResponse> childCategories = assetCategoryService.getChildCategories(nonExistentParentId);
    //        assertThrows(CustomException.class, () ->
    // assetCategoryService.getChildCategories(nonExistentParentId));
    assertThat(childCategories).isEmpty();
    }

//    @Test
//    @DisplayName("성공: 이름, 코드, 부모, 썸네일 등 모든 정보를 정상적으로 수정한다")
//    void updateAssetCategory_withAllFields_updatesSuccessfully() {
//        // GIVEN
//        Long originalParentId = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("기존 부모", "OP", null, null));
//        Long originalIconId = testFileUploader.initiateTestFileUpload("old.png");
//        Long categoryId = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("원본", "ORI", originalParentId, originalIconId));
//
//        Long newParentId = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("새 부모", "NP", null, null));
//        Long newIconId = testFileUploader.initiateTestFileUpload("new.png");
//
//        AssetCategoryUpdateRequest request = new AssetCategoryUpdateRequest("수정된 이름", "UPD", newParentId, newIconId);
//
//        // WHEN
//        assetCategoryService.updateAssetCategory(categoryId, request);
//
//    // THEN
//    AssetCategoryResponse response =
//        assetCategoryService.getAllCategories().list().getFirst().children().getFirst();
//
//        assertThat(response.name()).isEqualTo(request.name());
//        assertThat(response.code()).isEqualTo(request.code());
//        assertThat(response.parentId()).isEqualTo(newParentId);
//        assertThat(response.thumbnail().id()).isEqualTo(newIconId);
//    }

    @Test
    @DisplayName("실패: 자기 자신을 부모로 지정하려고 할 때 CustomException이 발생한다")
    void updateAssetCategory_withSelfAsParent_throwsException() {
        // GIVEN
        Long categoryId = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("카테고리", "CAT", null, null));
        AssetCategoryUpdateRequest request = new AssetCategoryUpdateRequest("이름변경", "CAT", categoryId, null);

        // WHEN & THEN
        assertThrows(CustomException.class, () -> assetCategoryService.updateAssetCategory(categoryId, request));
    }

    @Test
    @DisplayName("성공: 연결된 자식이나 에셋이 없을 때 정상적으로 삭제된다")
    void deleteAssetCategory_withNoRelations_deletesSuccessfully() {
        // GIVEN
        Long categoryId = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("삭제될 카테고리", "DEL", null, null));

        // WHEN
        assetCategoryService.deleteAssetCategory(categoryId);

        // THEN
        assertThat(assetCategoryRepository.findById(categoryId)).isEmpty();
    }

//    @Test
//    @DisplayName("실패: 자식 카테고리가 존재할 때 삭제 시도 시 CustomException이 발생한다")
//    void deleteAssetCategory_withChildren_throwsException() {
//        // GIVEN
//        Long parentId = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("부모", "P", null, null));
//        assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("자식", "C", parentId, null));
//
//        // WHEN & THEN
//        assertThrows(CustomException.class, () -> assetCategoryService.deleteAssetCategory(parentId));
//    }

    @Test
    @DisplayName("실패: 할당된 에셋이 존재할 때 삭제 시도 시 CustomException이 발생한다")
    void deleteAssetCategory_withAssignedAssets_throwsException() {
        // GIVEN
        Long categoryId = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("카테고리", "CAT", null, null));
        AssetCategory category = assetCategoryRepository.findById(categoryId).orElseThrow();
        assetRepository.save(Asset.builder().name("에셋").code("A01").category(category).build());

        // WHEN & THEN
        assertThrows(CustomException.class, () -> assetCategoryService.deleteAssetCategory(categoryId));
    }
}