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
import org.assertj.core.api.Assertions
import org.assertj.core.api.ThrowingConsumer
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.function.Executable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Import(MockBeansConfig::class)
@Transactional
internal class AssetCategoryServiceTest {
    @Autowired
    private val assetCategoryService: AssetCategoryService? = null

    @Autowired
    private val assetCategoryRepository: AssetCategoryRepository? = null

    @Autowired
    private val assetRepository: AssetRepository? = null

    @Autowired
    private val testFileUploader: TestFileUploader? = null

    // --- Create Test ---
    @Test
    @DisplayName("성공: 유효한 요청으로 최상위 카테고리(depth=1)를 생성하고 모든 응답 필드를 검증한다")
    fun createAssetCategory_withValidRequest_savesRootCategory() {
        // GIVEN
        val thumbnailFileId = testFileUploader!!.initiateTestFileUpload("icon.png")
        val request =
            AssetCategoryCreateRequest("가전", "ELEC", null, thumbnailFileId)

        // WHEN
        val categoryId = assetCategoryService!!.createAssetCategory(request)

        // THEN: DB에서 직접 조회하여 모든 필드를 상세히 검증
        val savedCategory: AssetCategory = assetCategoryRepository!!.findById(categoryId!!).orElseThrow()!!
        Assertions.assertThat(savedCategory.getName()).isEqualTo("가전")
        assertThat(savedCategory.getCode()).isEqualTo("ELEC")
        Assertions.assertThat<AssetCategory?>(savedCategory.getParent()).isNull()
        Assertions.assertThat(savedCategory.getDepth()).isEqualTo(1) // depth는 항상 1이어야 함
        assertThat(savedCategory.getIconFileId()).isEqualTo(thumbnailFileId)
        assertThat(savedCategory.getAssets()).isEmpty()
        Assertions.assertThat<AssetCategory?>(savedCategory.getChildren()).isEmpty() // 자식은 항상 비어있어야 함
    }

    @Test
    @DisplayName("실패: 부모 ID를 지정하여 자식 카테고리(depth>1) 생성을 시도하면 예외가 발생한다")
    fun createAssetCategory_withParentId_throwsException() {
        // GIVEN: 부모 카테고리 생성
        val parentId = createAndSaveCategory("가전", "ELEC", null)
        val childRequest =
            AssetCategoryCreateRequest("TV", "TV", parentId, null)

        // WHEN & THEN
        org.junit.jupiter.api.Assertions.assertThrows<CustomException?>(
            CustomException::class.java, Executable { assetCategoryService!!.createAssetCategory(childRequest) })
    }

    @Test
    @DisplayName("실패: 중복된 코드로 생성 시도 시 CustomException이 발생한다")
    fun createAssetCategory_withDuplicateCode_throwsException() {
        // GIVEN
        createAndSaveCategory("가전", "ELEC", null)
        val duplicateRequest =
            AssetCategoryCreateRequest("전자제품", "ELEC", null, null)

        // WHEN & THEN
        org.junit.jupiter.api.Assertions.assertThrows<CustomException?>(
            CustomException::class.java, Executable { assetCategoryService!!.createAssetCategory(duplicateRequest) })
    }

    @Test
    @DisplayName("실패: 존재하지 않는 부모 ID로 생성 시도 시 CustomException이 발생한다")
    fun createAssetCategory_withNonExistentParentId_throwsException() {
        // GIVEN
        val request = AssetCategoryCreateRequest("자식", "CHILD", 9999L, null)
        // WHEN & THEN
        org.junit.jupiter.api.Assertions.assertThrows<CustomException?>(CustomException::class.java, Executable { assetCategoryService!!.createAssetCategory(request) })
    }

    // --- Read Test ---
    @Test
    @DisplayName("성공: 전체 카테고리 조회 시 모든 카테고리가 depth=1이며 자식이 없는지 검증한다")
    fun getAllCategories_returnsFlatListOfRootCategories() {
        // GIVEN: 여러 개의 최상위 카테고리 생성
        val root1IconId = testFileUploader!!.initiateTestFileUpload("root1.png")
        createAndSaveCategory("가전", "ELEC", null, root1IconId)
        createAndSaveCategory("가구", "FURN", null)

        // WHEN
        val rootCategories = assetCategoryService!!.allCategories

        // THEN: 생성된 모든 카테고리가 최상위 레벨에 존재
        Assertions.assertThat<AssetCategoryResponse?>(rootCategories).hasSize(2)
        // 모든 카테고리의 depth는 1이고, parentId는 null이며, children은 비어있어야 함
        Assertions.assertThat<AssetCategoryResponse?>(rootCategories)
            .allSatisfy(
                ThrowingConsumer { category: AssetCategoryResponse? ->
                    Assertions.assertThat(category!!.depth).isEqualTo(1)
                    Assertions.assertThat(category.parentId).isNull()
                    Assertions.assertThat<AssetCategoryResponse?>(category.children).isEmpty()
                })
    }

    // --- Update Test ---
    @Test
    @DisplayName("성공: 카테고리의 이름, 코드, 썸네일 정보를 정상적으로 수정한다 (부모 ID는 null)")
    fun updateAssetCategory_withoutParentChange_updatesSuccessfully() {
        // GIVEN
        val categoryId = createAndSaveCategory("원본", "ORI", null, null)
        val newIconId = testFileUploader!!.initiateTestFileUpload("new.png")
        val request =
            AssetCategoryUpdateRequest("수정된 이름", "UPD", null, newIconId)

        // WHEN
        assetCategoryService!!.updateAssetCategory(categoryId, request)

        // THEN
        val updatedCategory: AssetCategory = assetCategoryRepository!!.findById(categoryId).orElseThrow()!!
        Assertions.assertThat(updatedCategory.getName()).isEqualTo("수정된 이름")
        assertThat(updatedCategory.getCode()).isEqualTo("UPD")
        assertThat(updatedCategory.getIconFileId()).isEqualTo(newIconId)
        Assertions.assertThat<AssetCategory?>(updatedCategory.getParent()).isNull() // 부모는 변경되지 않음
        Assertions.assertThat(updatedCategory.getDepth()).isEqualTo(1) // 깊이는 변경되지 않음
    }

    @Test
    @DisplayName("실패: 카테고리 수정 시 부모를 지정하려고 하면 예외가 발생한다")
    fun updateAssetCategory_withParentId_throwsException() {
        // GIVEN
        val categoryId = createAndSaveCategory("카테고리", "CAT", null)
        val newParentId = createAndSaveCategory("새 부모", "NEW_P", null)
        val request =
            AssetCategoryUpdateRequest("이름변경", "CAT_UPDATED", newParentId, null)

        // WHEN & THEN
        org.junit.jupiter.api.Assertions.assertThrows<CustomException?>(
            CustomException::class.java, Executable { assetCategoryService!!.updateAssetCategory(categoryId, request) })
    }

    // --- Delete Test ---
    @Test
    @DisplayName("성공: 할당된 에셋이 없을 때 정상적으로 삭제된다")
    fun deleteAssetCategory_withNoRelations_deletesSuccessfully() {
        // GIVEN
        val categoryId = createAndSaveCategory("삭제될 카테고리", "DEL", null)

        // WHEN
        assetCategoryService!!.deleteAssetCategory(categoryId)

        // THEN
        Assertions.assertThat<AssetCategory?>(assetCategoryRepository!!.findById(categoryId)).isEmpty()
    }

    @Test
    @DisplayName("실패: 할당된 에셋이 존재할 때 삭제 시도 시 CustomException이 발생한다")
    fun deleteAssetCategory_withAssignedAssets_throwsException() {
        // GIVEN
        val categoryId = createAndSaveCategory("카테고리", "CAT", null)
        val category: AssetCategory = assetCategoryRepository!!.findById(categoryId).orElseThrow()!!
        assetRepository!!.save<Asset?>(Asset.builder().name("에셋").code("A01").category(category).build())

        // WHEN & THEN
        org.junit.jupiter.api.Assertions.assertThrows<CustomException?>(CustomException::class.java, Executable { assetCategoryService!!.deleteAssetCategory(categoryId) })
    }

    // --- Helper Methods ---
    private fun createAndSaveCategory(name: String?, code: String?, parentId: Long?, thumbnailId: Long? = null): Long {
        val request =
            AssetCategoryCreateRequest(name, code, parentId, thumbnailId)
        return assetCategoryService!!.createAssetCategory(request)!!
    }
}
