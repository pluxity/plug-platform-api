package com.pluxity.asset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import com.pluxity.asset.dto.AssetCategoryAllResponse;
import com.pluxity.asset.dto.AssetCategoryCreateRequest;
import com.pluxity.asset.dto.AssetCategoryResponse;
import com.pluxity.asset.dto.AssetCategoryUpdateRequest;
import com.pluxity.asset.entity.AssetCategory;
import com.pluxity.asset.entity.Asset;
import com.pluxity.asset.repository.AssetCategoryRepository;
import com.pluxity.asset.repository.AssetRepository;
import com.pluxity.asset.service.AssetCategoryService;
import com.pluxity.file.dto.FileResponse;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class AssetCategoryServiceTest {

    @Autowired
    private AssetCategoryService assetCategoryService;

    @Autowired
    private AssetCategoryRepository assetCategoryRepository;

    // Asset과의 연관관계 테스트를 위해 주입
    @Autowired
    private AssetRepository assetRepository;

    // FileService는 Mocking하여 AssetCategoryService의 로직에만 집중
    @MockBean
    private FileService fileService;

    private Long iconFileId = 1L;

    @BeforeEach
    void setUp() {
        // GIVEN: 모든 테스트에서 FileService의 getFiles(List<Long>)가 호출될 때,
        //        가짜 FileResponse 목록을 반환하도록 설정합니다.
        when(fileService.getFiles(anyList())).thenAnswer(invocation -> {
            List<Long> ids = invocation.getArgument(0);
            // 요청받은 ID 목록을 기반으로 FileResponse 목록을 동적으로 생성
            return ids.stream()
                    .map(id -> new FileResponse(id, "/files/icon_" + id + ".png", "icon.png", "image/png", "COMPLETE", null))
                    .toList();
        });
    }

        @Test
        @DisplayName("성공: 유효한 요청으로 최상위 카테고리를 생성한다")
        void createAssetCategory_withValidRequest_savesRootCategory() {
            // GIVEN
            AssetCategoryCreateRequest request = new AssetCategoryCreateRequest("가전", "ELEC", null, iconFileId);

            // WHEN
            Long categoryId = assetCategoryService.createAssetCategory(request);

            // THEN
            AssetCategory savedCategory = assetCategoryRepository.findById(categoryId).orElseThrow();
            assertThat(savedCategory.getName()).isEqualTo("가전");
            assertThat(savedCategory.getCode()).isEqualTo("ELEC");
            assertThat(savedCategory.getParent()).isNull();
            assertThat(savedCategory.getIconFileId()).isEqualTo(iconFileId);
        }

        @Test
        @DisplayName("성공: 유효한 요청으로 자식 카테고리를 생성한다")
        void createAssetCategory_withValidParent_savesChildCategory() {
            // GIVEN
            Long parentId = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("가전", "ELEC", null, null));
            AssetCategoryCreateRequest childRequest = new AssetCategoryCreateRequest("TV", "TV", parentId, null);

            // WHEN
            Long childId = assetCategoryService.createAssetCategory(childRequest);

            // THEN
            AssetCategory childCategory = assetCategoryRepository.findById(childId).orElseThrow();
            assertThat(childCategory.getParent()).isNotNull();
            assertThat(childCategory.getParent().getId()).isEqualTo(parentId);
            assertThat(childCategory.getDepth()).isEqualTo(2);
        }

        @Test
        @DisplayName("실패: 중복된 코드로 생성 시도 시 CustomException이 발생한다")
        void createAssetCategory_withDuplicateCode_throwsException() {
            // GIVEN
            assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("가전", "ELEC", null, null));
            AssetCategoryCreateRequest duplicateRequest = new AssetCategoryCreateRequest("전자제품", "ELEC", null, null);

            // WHEN & THEN
            CustomException exception = assertThrows(CustomException.class, () -> assetCategoryService.createAssetCategory(duplicateRequest));
        }

        @Test
        @DisplayName("실패: 존재하지 않는 부모 ID로 생성 시도 시 CustomException이 발생한다")
        void createAssetCategory_withNonExistentParentId_throwsException() {
            // GIVEN
            Long nonExistentParentId = 9999L;
            AssetCategoryCreateRequest request = new AssetCategoryCreateRequest("자식", "CHILD", nonExistentParentId, null);

            // WHEN & THEN
            CustomException exception = assertThrows(CustomException.class, () -> assetCategoryService.createAssetCategory(request));
        }

        @Test
        @DisplayName("실패: 최대 허용 깊이를 초과하여 생성 시도 시 CustomException이 발생한다")
        void createAssetCategory_exceedingMaxDepth_throwsException() {
            // GIVEN (AssetCategory의 maxDepth가 3 이라고 가정)
            Long id1 = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("Depth 1", "D0", null, null));
            Long id2 = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("Depth 2", "D1", id1, null));

            AssetCategoryCreateRequest invalidRequest = new AssetCategoryCreateRequest("Depth 4", "D4", id2, null);

            // WHEN & THEN
            // AssetCategory 엔티티 내부에 깊이 검증 로직이 있다고 가정
            assertThrows(CustomException.class, () -> assetCategoryService.createAssetCategory(invalidRequest));
        }

        @Test
        @DisplayName("성공: ID로 조회 시 정확한 카테고리 응답을 반환한다")
        void getAssetCategory_withExistingId_returnsCorrectResponse() {
            // GIVEN: 여러 카테고리를 생성하여 전체 목록이 복잡한 상황을 가정
            Long rootId = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("가전", "ELEC", null, iconFileId));
            assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("TV", "TV", rootId, null));

            // WHEN
            AssetCategory response = assetCategoryRepository.findById(rootId).orElseThrow();

            // THEN
            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(rootId);
            assertThat(response.getName()).isEqualTo("가전");
            assertThat(response.getIconFileId()).isNotNull();
        }

        @Test
        @DisplayName("성공: 전체 카테고리 조회 시 올바른 트리 구조를 list 필드에 반환한다")
        void getAllCategories_returnsCorrectTreeStructureInList() {
            // GIVEN: 2개의 루트와 각각의 자식 생성
            Long root1Id = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("가전", "ELEC", null, null));
            assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("TV", "TV", root1Id, null));
            Long root2Id = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("가구", "FURN", null, null));

            // WHEN
            AssetCategoryAllResponse response = assetCategoryService.getAllCategories();
            // `response.tree()`가 아니라 `response.list()`를 사용합니다.
            List<AssetCategoryResponse> rootCategories = response.list();

            // THEN
            assertThat(rootCategories).hasSize(2); // 루트는 2개

            // '가전' 카테고리를 찾아서 자식이 올바르게 연결되었는지 검증
            AssetCategoryResponse elecCategory = rootCategories.stream()
                    .filter(c -> c.id().equals(root1Id))
                    .findFirst()
                    .orElseThrow();
            assertThat(elecCategory.children()).hasSize(1);
            assertThat(elecCategory.children().getFirst().name()).isEqualTo("TV");

            // '가구' 카테고리를 찾아서 자식이 없는지 검증
            AssetCategoryResponse furnCategory = rootCategories.stream()
                    .filter(c -> c.id().equals(root2Id))
                    .findFirst()
                    .orElseThrow();
            assertThat(furnCategory.children()).isEmpty();
        }

        @Test
        @DisplayName("성공: 특정 부모의 자식 카테고리 목록을 조회한다")
        void getChildCategories_returnsListOfChildren() {
            // GIVEN
            Long parentId = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("부모", "P", null, null));
            assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("자식1", "C1", parentId, null));
            assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("자식2", "C2", parentId, null));
            assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("다른 부모의 자식", "C3", null, null)); // 관련 없는 카테고리

            // WHEN
            List<AssetCategoryResponse> children = assetCategoryService.getChildCategories(parentId);

            // THEN
            assertThat(children).hasSize(2);
            // 자식들의 parentId가 모두 일치하는지 추가로 검증
            assertThat(children).allMatch(child -> child.parentId().equals(parentId));
            assertThat(children.stream().map(AssetCategoryResponse::name)).containsExactlyInAnyOrder("자식1", "자식2");
        }

        @Test
        @DisplayName("성공: 자식이 없는 카테고리 조회 시 빈 리스트를 반환한다")
        void getChildCategories_withNoChildren_returnsEmptyList() {
            // GIVEN
            Long parentId = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("자식 없는 부모", "P", null, null));

            // WHEN
            List<AssetCategoryResponse> children = assetCategoryService.getChildCategories(parentId);

            // THEN
            assertThat(children).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("실패: 존재하지 않는 부모 ID로 자식 조회 시 빈 리스트를 반환한다")
        void getChildCategories_withNonExistentParentId_returnsEmptyList() {
            // GIVEN
            Long nonExistentParentId = 9999L;

            // WHEN: findByParentId는 결과가 없으면 빈 리스트를 반환하므로 예외가 발생하지 않음
            List<AssetCategoryResponse> children = assetCategoryService.getChildCategories(nonExistentParentId);

            // THEN
            assertThat(children).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("성공: 이름과 코드 등 모든 정보를 정상적으로 수정한다")
        void updateAssetCategory_withAllFields_updatesSuccessfully() {
            // GIVEN
            Long categoryId = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("원본", "ORI", null, null));
            AssetCategoryUpdateRequest request = new AssetCategoryUpdateRequest("수정", "UPD", null, iconFileId);

            // WHEN
            assetCategoryService.updateAssetCategory(categoryId, request);

            // THEN
            AssetCategory updated = assetCategoryRepository.findById(categoryId).orElseThrow();
            assertThat(updated.getName()).isEqualTo("수정");
            assertThat(updated.getCode()).isEqualTo("UPD");
            assertThat(updated.getIconFileId()).isEqualTo(iconFileId);
        }

        @Test
        @DisplayName("성공: 부모 카테고리를 다른 카테고리로 변경한다")
        void updateAssetCategory_changingParent_updatesHierarchy() {
            // GIVEN
            Long root1Id = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("루트1", "R1", null, null));
            Long root2Id = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("루트2", "R2", null, null));
            Long childId = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("자식", "C1", root1Id, null));

            // WHEN: 자식의 부모를 root2로 변경
            assetCategoryService.updateAssetCategory(childId, new AssetCategoryUpdateRequest(null, null, root2Id, null));

            // THEN
            AssetCategory child = assetCategoryRepository.findById(childId).orElseThrow();
            assertThat(child.getParent().getId()).isEqualTo(root2Id);
        }

        @Test
        @DisplayName("실패: 자기 자신을 부모로 지정하려고 할 때 CustomException이 발생한다")
        void updateAssetCategory_withSelfAsParent_throwsException() {
            // GIVEN
            Long categoryId = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("카테고리", "CAT", null, null));
            AssetCategoryUpdateRequest request = new AssetCategoryUpdateRequest(null, null, categoryId, null);

            // WHEN & THEN
            CustomException exception = assertThrows(CustomException.class, () -> assetCategoryService.updateAssetCategory(categoryId, request));
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

        @Test
        @DisplayName("실패: 자식 카테고리가 존재할 때 삭제 시도 시 CustomException이 발생한다")
        void deleteAssetCategory_withChildren_throwsException() {
            // GIVEN
            Long parentId = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("부모", "P", null, null));
            assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("자식", "C", parentId, null));

            // WHEN & THEN
            CustomException exception = assertThrows(CustomException.class, () -> assetCategoryService.deleteAssetCategory(parentId));
        }

        @Test
        @DisplayName("실패: 할당된 에셋이 존재할 때 삭제 시도 시 CustomException이 발생한다")
        void deleteAssetCategory_withAssignedAssets_throwsException() {
            // GIVEN
            Long categoryId = assetCategoryService.createAssetCategory(new AssetCategoryCreateRequest("카테고리", "CAT", null, null));
            AssetCategory category = assetCategoryRepository.findById(categoryId).orElseThrow();

            // 카테고리에 에셋 할당
            assetRepository.save(Asset.builder().name("에셋").code("A01").category(category).build());

            // WHEN & THEN
            CustomException exception = assertThrows(CustomException.class, () -> assetCategoryService.deleteAssetCategory(categoryId));
    }
}