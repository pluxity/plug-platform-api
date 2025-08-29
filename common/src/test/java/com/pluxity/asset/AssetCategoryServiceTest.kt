package com.pluxity.asset

import com.pluxity.asset.dto.AssetCategoryCreateRequest
import com.pluxity.asset.dto.AssetCategoryResponse
import com.pluxity.asset.dto.AssetCategoryUpdateRequest
import com.pluxity.asset.entity.Asset
import com.pluxity.asset.entity.AssetCategory
import com.pluxity.asset.repository.AssetCategoryRepository
import com.pluxity.asset.repository.AssetRepository
import com.pluxity.asset.service.AssetCategoryService
import com.pluxity.config.MockBeansConfig
import com.pluxity.global.exception.CustomException
import com.pluxity.util.TestFileUploader
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Import(MockBeansConfig::class)
@Transactional
class AssetCategoryServiceTest(
    @Autowired private val assetCategoryService: AssetCategoryService,
    @Autowired private val assetCategoryRepository: AssetCategoryRepository,
    @Autowired private val assetRepository: AssetRepository,
    @Autowired private val testFileUploader: TestFileUploader,
) {
    @Test
    @DisplayName("성공: 유효한 요청으로 최상위 카테고리(depth=1)를 생성하고 모든 응답 필드를 검증한다")
    fun createAssetCategory_withValidRequest_savesRootCategory() {
        val thumbnailFileId = testFileUploader.initiateTestFileUpload("icon.png")
        val request = AssetCategoryCreateRequest("가전", "ELEC", null, thumbnailFileId)

        val categoryId = assetCategoryService.createAssetCategory(request)

        val savedCategory = assetCategoryRepository.findById(categoryId).orElseThrow()
        assertThat(savedCategory.name).isEqualTo("가전")
        assertThat(savedCategory.code).isEqualTo("ELEC")
        assertThat(savedCategory.parent).isNull()
        assertThat(savedCategory.depth).isEqualTo(1)
        assertThat(savedCategory.iconFileId).isEqualTo(thumbnailFileId)
        assertThat(savedCategory.assets).isEmpty()
        assertThat(savedCategory.children).isEmpty()
    }

    @Test
    @DisplayName("실패: 부모 ID를 지정하여 자식 카테고리(depth>1) 생성을 시도하면 예외가 발생한다")
    fun createAssetCategory_withParentId_throwsException() {
        val parentId = createAndSaveCategory("가전", "ELEC", null)
        val childRequest = AssetCategoryCreateRequest("TV", "TV", parentId, null)

        assertThrows<CustomException> { assetCategoryService.createAssetCategory(childRequest) }
    }

    @Test
    @DisplayName("실패: 중복된 코드로 생성 시도 시 CustomException이 발생한다")
    fun createAssetCategory_withDuplicateCode_throwsException() {
        createAndSaveCategory("가전", "ELEC", null)
        val duplicateRequest = AssetCategoryCreateRequest("전자제품", "ELEC", null, null)

        assertThrows<CustomException> { assetCategoryService.createAssetCategory(duplicateRequest) }
    }

    @Test
    @DisplayName("실패: 존재하지 않는 부모 ID로 생성 시도 시 CustomException이 발생한다")
    fun createAssetCategory_withNonExistentParentId_throwsException() {
        val request = AssetCategoryCreateRequest("자식", "CHILD", 9999L, null)
        assertThrows<CustomException> { assetCategoryService.createAssetCategory(request) }
    }

    @Test
    @DisplayName("성공: 전체 카테고리 조회 시 모든 카테고리가 depth=1이며 자식이 없는지 검증한다")
    fun getAllCategories_returnsFlatListOfRootCategories() {
        val root1IconId = testFileUploader.initiateTestFileUpload("root1.png")
        createAndSaveCategory("가전", "ELEC", null, root1IconId)
        createAndSaveCategory("가구", "FURN", null)

        val rootCategories: List<AssetCategoryResponse> = assetCategoryService.getAllCategories()

        assertThat(rootCategories).hasSize(2)
        assertThat(rootCategories).allSatisfy { category ->
            assertThat(category.depth).isEqualTo(1)
            assertThat(category.parentId).isNull()
            assertThat(category.children).isEmpty()
        }
    }

    @Test
    @DisplayName("성공: 카테고리의 이름, 코드, 썸네일 정보를 정상적으로 수정한다 (부모 ID는 null)")
    fun updateAssetCategory_withoutParentChange_updatesSuccessfully() {
        val categoryId = createAndSaveCategory("원본", "ORI", null, null)
        val newIconId = testFileUploader.initiateTestFileUpload("new.png")
        val request = AssetCategoryUpdateRequest("수정된 이름", "UPD", null, newIconId)

        assetCategoryService.updateAssetCategory(categoryId, request)

        val updatedCategory = assetCategoryRepository.findById(categoryId).orElseThrow()
        assertThat(updatedCategory.name).isEqualTo("수정된 이름")
        assertThat(updatedCategory.code).isEqualTo("UPD")
        assertThat(updatedCategory.iconFileId).isEqualTo(newIconId)
        assertThat(updatedCategory.parent).isNull()
        assertThat(updatedCategory.depth).isEqualTo(1)
    }

    @Test
    @DisplayName("실패: 카테고리 수정 시 부모를 지정하려고 하면 예외가 발생한다")
    fun updateAssetCategory_withParentId_throwsException() {
        val categoryId = createAndSaveCategory("카테고리", "CAT", null)
        val newParentId = createAndSaveCategory("새 부모", "NEW_P", null)
        val request = AssetCategoryUpdateRequest("이름변경", "CAT_UPDATED", newParentId, null)

        assertThrows<CustomException> { assetCategoryService.updateAssetCategory(categoryId, request) }
    }

    @Test
    @DisplayName("성공: 할당된 에셋이 없을 때 정상적으로 삭제된다")
    fun deleteAssetCategory_withNoRelations_deletesSuccessfully() {
        val categoryId = createAndSaveCategory("삭제될 카테고리", "DEL", null)
        assetCategoryService.deleteAssetCategory(categoryId)
        assertThat(assetCategoryRepository.findById(categoryId)).isEmpty
    }

    @Test
    @DisplayName("실패: 할당된 에셋이 존재할 때 삭제 시도 시 CustomException이 발생한다")
    fun deleteAssetCategory_withAssignedAssets_throwsException() {
        val categoryId = createAndSaveCategory("카테고리", "CAT", null)
        val category: AssetCategory = assetCategoryRepository.findById(categoryId).orElseThrow()
        assetRepository.save(
            Asset
                .builder()
                .name("에셋")
                .code("A01")
                .category(category)
                .build(),
        )

        assertThrows<CustomException> { assetCategoryService.deleteAssetCategory(categoryId) }
    }

    private fun createAndSaveCategory(
        name: String,
        code: String,
        parentId: Long?,
    ): Long = createAndSaveCategory(name, code, parentId, null)

    private fun createAndSaveCategory(
        name: String,
        code: String,
        parentId: Long?,
        thumbnailId: Long?,
    ): Long {
        val request = AssetCategoryCreateRequest(name, code, parentId, thumbnailId)
        return assetCategoryService.createAssetCategory(request)
    }
}
