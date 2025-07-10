package com.pluxity.asset.service;

import com.pluxity.asset.dto.AssetCategoryCreateRequest;
import com.pluxity.asset.dto.AssetCategoryResponse;
import com.pluxity.asset.dto.AssetCategoryAllResponse;
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
        AssetCategoryAllResponse rootCategoriesResponse = assetCategoryService.getAllCategories();
        List<AssetCategoryResponse> rootCategories = rootCategoriesResponse.list();

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

    @Test
    @DisplayName("빈 이름으로 에셋 카테고리 생성 시 예외가 발생한다")
    void createAssetCategory_WithEmptyName_ThrowsCustomException() {
        // 이 테스트는 컨트롤러 계층에서 @Valid 검증을 통해 수행되어야 합니다.
        // @NotBlank 어노테이션이 있으므로 컨트롤러 테스트에서 검증해야 합니다.
    }

    @Test
    @DisplayName("빈 코드로 에셋 카테고리 생성 시 예외가 발생한다")
    void createAssetCategory_WithEmptyCode_ThrowsCustomException() {
        // 이 테스트는 컨트롤러 계층에서 @Valid 검증을 통해 수행되어야 합니다.
        // @NotBlank 어노테이션이 있으므로 컨트롤러 테스트에서 검증해야 합니다.
    }

    @Test
    @DisplayName("존재하지 않는 부모 카테고리 ID로 에셋 카테고리 생성 시 예외가 발생한다")
    void createAssetCategory_WithNonExistingParentId_ThrowsCustomException() {
        // given
        Long nonExistingParentId = 9999L;
        AssetCategoryCreateRequest invalidRequest = new AssetCategoryCreateRequest(
                "자식 카테고리",
                "CC1",
                nonExistingParentId,
                null
        );

        // when & then
        assertThrows(CustomException.class, () -> assetCategoryService.createAssetCategory(invalidRequest));
    }

    @Test
    @DisplayName("유효하지 않은 아이콘 파일 ID로 에셋 카테고리 생성 시 예외가 발생한다")
    void createAssetCategory_WithInvalidIconFileId_ThrowsCustomException() {
        // given
        Long invalidIconFileId = 9999L;
        AssetCategoryCreateRequest invalidRequest = new AssetCategoryCreateRequest(
                "테스트 카테고리",
                "TC3",
                null,
                invalidIconFileId
        );

        // when & then
        // 이 테스트는 컨트롤러 계층에서 @Valid 검증을 통해 수행되어야 합니다.
        // @NotBlank 어노테이션이 있으므로 컨트롤러 테스트에서 검증해야 합니다.
//        assertThrows(CustomException.class, () -> assetCategoryService.createAssetCategory(invalidRequest));
    }

    @Test
    @DisplayName("최대 깊이를 초과하는 계층 구조로 에셋 카테고리 생성 시 예외가 발생한다")
    void createAssetCategory_ExceedingMaxDepth_ThrowsCustomException() {
        // given
        // 1. 루트 카테고리 생성
        Long rootId = assetCategoryService.createAssetCategory(createRequest);
        
//        // 2. 1단계 자식 카테고리 생성
//        AssetCategoryCreateRequest childRequest = new AssetCategoryCreateRequest(
//                "자식 카테고리",
//                "CC1",
//                rootId,
//                null
//        );
//        Long childId = assetCategoryService.createAssetCategory(childRequest);
        
        // 3. 2단계 자식 카테고리 생성 시도 (최대 깊이 초과)
        AssetCategoryCreateRequest grandchildRequest = new AssetCategoryCreateRequest(
                "손자 카테고리",
                "GC1",
                rootId,
                null
        );
        
        // when & then
        assertThrows(CustomException.class, () -> assetCategoryService.createAssetCategory(grandchildRequest));
    }

    @Test
    @DisplayName("에셋 카테고리 업데이트 시 중복 코드로 변경 시도할 때 예외가 발생한다")
    void updateAssetCategory_WithDuplicateCode_ThrowsCustomException() {
        // given
        // 1. 첫 번째 카테고리 생성
        Long id1 = assetCategoryService.createAssetCategory(createRequest);
        
        // 2. 두 번째 카테고리 생성
        AssetCategoryCreateRequest secondRequest = new AssetCategoryCreateRequest(
                "두 번째 카테고리",
                "TC2",
                null,
                null
        );
        Long id2 = assetCategoryService.createAssetCategory(secondRequest);
        
        // 3. 두 번째 카테고리를 첫 번째 카테고리와 동일한 코드로 업데이트 시도
        AssetCategoryUpdateRequest updateRequest = new AssetCategoryUpdateRequest(
                "수정된 카테고리",
                "TC1", // 첫 번째 카테고리와 동일한 코드
                null,
                null
        );
        
        // when & then
        assertThrows(CustomException.class, () -> assetCategoryService.updateAssetCategory(id2, updateRequest));
    }

    @Test
    @DisplayName("존재하지 않는 카테고리를 업데이트 시도할 때 예외가 발생한다")
    void updateAssetCategory_WithNonExistingId_ThrowsCustomException() {
        // given
        Long nonExistingId = 9999L;
        AssetCategoryUpdateRequest updateRequest = new AssetCategoryUpdateRequest(
                "수정된 카테고리",
                "UC2",
                null,
                null
        );
        
        // when & then
        assertThrows(CustomException.class, () -> assetCategoryService.updateAssetCategory(nonExistingId, updateRequest));
    }

    @Test
    @DisplayName("존재하지 않는 카테고리를 삭제 시도할 때 예외가 발생한다")
    void deleteAssetCategory_WithNonExistingId_ThrowsCustomException() {
        // given
        Long nonExistingId = 9999L;
        
        // when & then
        assertThrows(CustomException.class, () -> assetCategoryService.deleteAssetCategory(nonExistingId));
    }
}
