package com.pluxity.asset

import com.pluxity.asset.dto.AssetCategoryCreateRequest
import com.pluxity.asset.dto.AssetCreateRequest
import com.pluxity.asset.dto.AssetResponse
import com.pluxity.asset.dto.AssetUpdateRequest
import com.pluxity.asset.entity.Asset
import com.pluxity.asset.repository.AssetRepository
import com.pluxity.asset.service.AssetCategoryService
import com.pluxity.asset.service.AssetService
import com.pluxity.config.MockBeansConfig
import com.pluxity.file.constant.FileStatus
import com.pluxity.file.dto.FileResponse
import com.pluxity.global.exception.CustomException
import com.pluxity.util.TestFileUploader
import org.assertj.core.api.Assertions
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
internal class AssetServiceTest {
    @Autowired
    private val assetService: AssetService? = null

    @Autowired
    private val assetRepository: AssetRepository? = null

    @Autowired
    private val assetCategoryService: AssetCategoryService? = null

    @Autowired
    private val testFileUploader: TestFileUploader? = null

    @Test
    @DisplayName("성공: 유효한 요청으로 에셋을 생성하고, 모든 응답 필드를 상세히 검증한다")
    fun createAsset_WithValidRequest_SavesAssetAndReturnsDetailedResponse() {
        // GIVEN: 에셋 생성에 필요한 모든 데이터 준비
        val assetFileId = testFileUploader!!.initiateTestFileUpload("asset_file.glb")
        val thumbnailFileId = testFileUploader.initiateTestFileUpload("thumbnail_image.png")
        val categoryId =
            assetCategoryService!!.createAssetCategory(
                AssetCategoryCreateRequest("테스트 카테고리", "TCC", null, null)
            )
        val request =
            AssetCreateRequest("테스트 에셋", "TES", assetFileId, thumbnailFileId, categoryId)

        // WHEN: 에셋 생성
        val createdAssetId = assetService!!.createAsset(request)

        // THEN: 생성된 에셋의 모든 필드 검증
        val response = assetService.getAsset(createdAssetId!!)

        Assertions.assertThat(response.id).isEqualTo(createdAssetId)
        Assertions.assertThat(response.name).isEqualTo(request.name)
        Assertions.assertThat(response.code).isEqualTo(request.code)

        // 카테고리 정보 검증
        Assertions.assertThat(response.categoryId).isEqualTo(categoryId)
        Assertions.assertThat(response.categoryName).isEqualTo("테스트 카테고리")
        Assertions.assertThat(response.categoryCode).isEqualTo("TCC")

        // 파일 정보 검증 (finalizeUpload가 호출되므로 COMPLETE 상태여야 함)
        Assertions.assertThat<FileResponse?>(response.file).isNotNull()
        Assertions.assertThat(response.file!!.id).isEqualTo(assetFileId)
        Assertions.assertThat(response.file.originalFileName).isEqualTo("asset_file.glb")
        Assertions.assertThat(response.file.fileStatus).isEqualTo(FileStatus.COMPLETE.name)

        // 썸네일 파일 정보 검증
        Assertions.assertThat<FileResponse?>(response.thumbnailFile).isNotNull()
        Assertions.assertThat(response.thumbnailFile!!.id).isEqualTo(thumbnailFileId)
        Assertions.assertThat(response.thumbnailFile.originalFileName).isEqualTo("thumbnail_image.png")
        Assertions.assertThat(response.thumbnailFile.fileStatus).isEqualTo(FileStatus.COMPLETE.name)
    }

    @Test
    @DisplayName("성공: 전체 에셋 조회 시 상세 정보가 포함된 목록을 반환한다")
    fun getAssets_ReturnsListOfDetailedAssetResponses() {
        // GIVEN: 2개의 서로 다른 에셋 생성
        val categoryId =
            assetCategoryService!!.createAssetCategory(
                AssetCategoryCreateRequest("카테고리", "CAT", null, null)
            )
        assetService!!.createAsset(
            AssetCreateRequest(
                "에셋1", "AS1", testFileUploader!!.initiateTestFileUpload("f1.png"), null, categoryId
            )
        )
        assetService.createAsset(
            AssetCreateRequest(
                "에셋2", "AS2", testFileUploader.initiateTestFileUpload("f2.png"), null, categoryId
            )
        )

        // WHEN: 전체 에셋 조회
        val responses: MutableList<AssetResponse> = assetService.assets

        // THEN: 목록 및 포함된 내용 검증
        Assertions.assertThat<AssetResponse?>(responses).hasSize(2)

        val firstAsset =
            responses.stream().filter { a: AssetResponse? -> a!!.code == "AS1" }.findFirst().orElseThrow()
        Assertions.assertThat(firstAsset.name).isEqualTo("에셋1")
        Assertions.assertThat(firstAsset.categoryId).isEqualTo(categoryId)
        Assertions.assertThat<FileResponse?>(firstAsset.file).isNotNull()
    }

    @Test
    @DisplayName("성공: 유효한 요청으로 에셋 정보를 수정하고, 모든 필드의 변경사항을 검증한다")
    fun updateAsset_WithValidRequest_UpdatesAssetAndVerifiesChanges() {
        // GIVEN: 원본 에셋 생성
        val originalCategoryId =
            assetCategoryService!!.createAssetCategory(
                AssetCategoryCreateRequest("원본 카테고리", "ORI", null, null)
            )
        val originalAssetId =
            assetService!!.createAsset(
                AssetCreateRequest(
                    "원본 에셋",
                    "ORI_A",
                    testFileUploader!!.initiateTestFileUpload("ori_f.png"),
                    testFileUploader.initiateTestFileUpload("ori_t.png"),
                    originalCategoryId
                )
            )

        // GIVEN: 수정을 위한 새로운 데이터 준비
        val newCategoryId =
            assetCategoryService.createAssetCategory(
                AssetCategoryCreateRequest("새 카테고리", "NEW_C", null, null)
            )
        val newFileId = testFileUploader.initiateTestFileUpload("new_f.png")
        val newThumbnailId = testFileUploader.initiateTestFileUpload("new_t.png")
        val updateRequest =
            AssetUpdateRequest("수정된 에셋", "UPD_A", newFileId, newThumbnailId, newCategoryId)

        // WHEN: 에셋 정보 수정
        assetService.updateAsset(originalAssetId!!, updateRequest)

        // THEN: 수정된 에셋의 모든 필드 검증
        val updatedAsset = assetService.getAsset(originalAssetId)
        Assertions.assertThat(updatedAsset.name).isEqualTo("수정된 에셋")
        Assertions.assertThat(updatedAsset.code).isEqualTo("UPD_A")
        Assertions.assertThat(updatedAsset.categoryId).isEqualTo(newCategoryId)
        Assertions.assertThat(updatedAsset.categoryName).isEqualTo("새 카테고리")
        Assertions.assertThat(updatedAsset.file!!.id).isEqualTo(newFileId)
        Assertions.assertThat(updatedAsset.thumbnailFile!!.id).isEqualTo(newThumbnailId)
    }

    @Test
    @DisplayName("성공: 에셋을 삭제하면 DB에서 조회되지 않는다")
    fun deleteAsset_RemovesAsset() {
        // GIVEN: 삭제할 에셋 생성
        val assetId =
            assetService!!.createAsset(
                AssetCreateRequest(
                    "삭제될 에셋", "DEL", testFileUploader!!.initiateTestFileUpload("del.png"), null, null
                )
            )
        Assertions.assertThat<Asset?>(assetRepository!!.findById(assetId!!)).isPresent()

        // WHEN: 에셋 삭제
        assetService.deleteAsset(assetId)

        // THEN: 해당 ID로 조회 시 예외 발생
        org.junit.jupiter.api.Assertions.assertThrows<CustomException?>(CustomException::class.java, Executable { assetService.getAsset(assetId) })
        Assertions.assertThat<Asset?>(assetRepository.findById(assetId)).isEmpty()
    }

    @Test
    @DisplayName("성공: 에셋에 카테고리를 할당하면 정보가 업데이트된다")
    fun assignCategory_UpdatesAssetCategory() {
        // GIVEN: 카테고리가 없는 에셋 생성
        val assetId =
            assetService!!.createAsset(
                AssetCreateRequest(
                    "카테고리 없는 에셋",
                    "NO_CAT",
                    testFileUploader!!.initiateTestFileUpload("file.png"),
                    null,
                    null
                )
            )
        Assertions.assertThat(assetService.getAsset(assetId!!).categoryId).isNull()

        // GIVEN: 할당할 카테고리 생성
        val categoryId =
            assetCategoryService!!.createAssetCategory(
                AssetCategoryCreateRequest("할당될 카테고리", "ASSIGN", null, null)
            )

        // WHEN: 카테고리 할당
        assetService.assignCategory(assetId, categoryId)

        // THEN: 에셋의 카테고리 정보가 업데이트되었는지 검증
        val updatedAsset = assetService.getAsset(assetId)
        Assertions.assertThat(updatedAsset.categoryId).isEqualTo(categoryId)
        Assertions.assertThat(updatedAsset.categoryName).isEqualTo("할당될 카테고리")
        Assertions.assertThat(updatedAsset.categoryCode).isEqualTo("ASSIGN")
    }

    @Test
    @DisplayName("성공: 에셋의 카테고리를 제거하면 null로 변경된다")
    fun removeCategory_SetsAssetCategoryToNull() {
        // GIVEN: 카테고리가 있는 에셋 생성
        val categoryId =
            assetCategoryService!!.createAssetCategory(
                AssetCategoryCreateRequest("제거될 카테고리", "REM", null, null)
            )
        val assetId =
            assetService!!.createAsset(
                AssetCreateRequest(
                    "카테고리 있는 에셋",
                    "HAS_CAT",
                    testFileUploader!!.initiateTestFileUpload("file.png"),
                    null,
                    categoryId
                )
            )
        Assertions.assertThat(assetService.getAsset(assetId!!).categoryId).isNotNull()

        // WHEN: 카테고리 제거
        assetService.removeCategory(assetId)

        // THEN: 에셋의 카테고리 정보가 null로 변경되었는지 검증
        val updatedAsset = assetService.getAsset(assetId)
        Assertions.assertThat(updatedAsset.categoryId).isNull()
        Assertions.assertThat(updatedAsset.categoryName).isNull()
        Assertions.assertThat(updatedAsset.categoryCode).isNull()
    }

    // --- 예외 케이스 테스트 ---
    @Test
    @DisplayName("실패: 중복된 코드로 에셋 생성 시 예외가 발생한다")
    fun createAsset_WithDuplicateCode_ThrowsCustomException() {
        // GIVEN: 기준 에셋 생성
        assetService!!.createAsset(
            AssetCreateRequest(
                "첫 에셋", "DUP_CODE", testFileUploader!!.initiateTestFileUpload("f1.png"), null, null
            )
        )

        // WHEN & THEN: 동일한 코드로 두 번째 에셋 생성 시도
        val duplicateRequest =
            AssetCreateRequest(
                "두 번째 에셋", "DUP_CODE", testFileUploader.initiateTestFileUpload("f2.png"), null, null
            )
        org.junit.jupiter.api.Assertions.assertThrows<CustomException?>(CustomException::class.java, Executable { assetService.createAsset(duplicateRequest) })
    }

    @Test
    @DisplayName("실패: 유효하지 않은 파일 ID로 에셋 생성 시 예외가 발생한다")
    fun createAsset_WithInvalidFileId_ThrowsCustomException() {
        // GIVEN
        val invalidFileId = 9999L
        val request = AssetCreateRequest("에셋", "CODE", invalidFileId, null, null)

        // WHEN & THEN
        org.junit.jupiter.api.Assertions.assertThrows<CustomException?>(CustomException::class.java, Executable { assetService!!.createAsset(request) })
    }

    @Test
    @DisplayName("실패: 유효하지 않은 카테고리 ID로 에셋 생성 시 예외가 발생한다")
    fun createAsset_WithInvalidCategoryId_ThrowsCustomException() {
        // GIVEN
        val invalidCategoryId = 9999L
        val request =
            AssetCreateRequest(
                "에셋",
                "CODE",
                testFileUploader!!.initiateTestFileUpload("file.png"),
                null,
                invalidCategoryId
            )

        // WHEN & THEN
        org.junit.jupiter.api.Assertions.assertThrows<CustomException?>(CustomException::class.java, Executable { assetService!!.createAsset(request) })
    }

    @Test
    @DisplayName("실패: 존재하지 않는 에셋 업데이트 시 예외가 발생한다")
    fun updateAsset_WithNonExistingId_ThrowsCustomException() {
        // GIVEN
        val nonExistingId = 9999L
        val request = AssetUpdateRequest("수정", "UPD", null, null, null)

        // WHEN & THEN
        org.junit.jupiter.api.Assertions.assertThrows<CustomException?>(CustomException::class.java, Executable { assetService!!.updateAsset(nonExistingId, request) })
    }

    @Test
    @DisplayName("실패: 존재하지 않는 에셋 삭제 시 예외가 발생한다")
    fun deleteAsset_WithNonExistingId_ThrowsCustomException() {
        // GIVEN
        val nonExistingId = 9999L

        // WHEN & THEN
        org.junit.jupiter.api.Assertions.assertThrows<CustomException?>(CustomException::class.java, Executable { assetService!!.deleteAsset(nonExistingId) })
    }

    @Test
    @DisplayName("실패: 에셋에 이미 카테고리가 없을 때 제거 시도 시 예외가 발생한다")
    fun removeCategory_FromAssetWithNoCategory_ThrowsCustomException() {
        // GIVEN: 카테고리가 없는 에셋 생성
        val assetId =
            assetService!!.createAsset(
                AssetCreateRequest(
                    "카테고리 없는 에셋",
                    "NO_CAT",
                    testFileUploader!!.initiateTestFileUpload("file.png"),
                    null,
                    null
                )
            )

        // WHEN & THEN
        org.junit.jupiter.api.Assertions.assertThrows<CustomException?>(CustomException::class.java, Executable { assetService.removeCategory(assetId!!) })
    }

    @Test
    @DisplayName("실패: 중복된 이름으로 에셋 생성 시 예외가 발생한다")
    fun createAsset_WithDuplicateName_ThrowsCustomException() {
        // GIVEN: 기준 에셋 생성
        assetService!!.createAsset(
            AssetCreateRequest(
                "중복된 이름", "CODE1", testFileUploader!!.initiateTestFileUpload("f1.png"), null, null
            )
        )

        // WHEN & THEN: 동일한 이름으로 두 번째 에셋 생성 시도
        val duplicateRequest =
            AssetCreateRequest(
                "중복된 이름", "CODE2", testFileUploader.initiateTestFileUpload("f2.png"), null, null
            )
        org.junit.jupiter.api.Assertions.assertThrows<CustomException?>(CustomException::class.java, Executable { assetService.createAsset(duplicateRequest) })
    }

    @Test
    @DisplayName("성공: 카테고리 없이 에셋을 생성할 수 있다")
    fun createAsset_withNullCategoryId_succeeds() {
        // GIVEN
        val request =
            AssetCreateRequest(
                "카테고리 없는 에셋",
                "NO_CAT",
                testFileUploader!!.initiateTestFileUpload("file.png"),
                null,
                null
            )

        // WHEN
        val createdAssetId = assetService!!.createAsset(request)

        // THEN
        val response = assetService.getAsset(createdAssetId!!)
        Assertions.assertThat<AssetResponse?>(response).isNotNull()
        Assertions.assertThat(response.categoryId).isNull()
        Assertions.assertThat(response.categoryName).isNull()
    }

    @Test
    @DisplayName("성공: 에셋 정보 수정 시 이름만 변경해도 정상적으로 반영된다")
    fun updateAsset_onlyWithName_updatesSuccessfully() {
        // GIVEN
        val assetId =
            assetService!!.createAsset(
                AssetCreateRequest(
                    "원본 이름", "CODE", testFileUploader!!.initiateTestFileUpload("file.png"), null, null
                )
            )

        // WHEN: 이름만 포함된 요청으로 업데이트
        val request = AssetUpdateRequest("새로운 이름", "CODE", null, null, null)
        assetService.updateAsset(assetId!!, request)

        // THEN
        val updatedAsset = assetService.getAsset(assetId)
        Assertions.assertThat(updatedAsset.name).isEqualTo("새로운 이름")
        Assertions.assertThat(updatedAsset.code).isEqualTo("CODE") // 코드는 그대로 유지
    }

    @Test
    @DisplayName("성공: 에셋 정보 수정 시 카테고리를 null 로 받더라도 카테고리는 변경되지 않는다.")
    fun updateAsset_toNullCategory_updatesSuccessfully() {
        // GIVEN
        val categoryId =
            assetCategoryService!!.createAssetCategory(
                AssetCategoryCreateRequest("카테고리", "CAT", null, null)
            )
        val assetId =
            assetService!!.createAsset(
                AssetCreateRequest(
                    "에셋",
                    "CODE",
                    testFileUploader!!.initiateTestFileUpload("file.png"),
                    null,
                    categoryId
                )
            )
        Assertions.assertThat(assetService.getAsset(assetId!!).categoryId).isNotNull()

        // WHEN: categoryId를 null로 하여 업데이트
        val request = AssetUpdateRequest("에셋", "CODE", null, null, null)
        assetService.updateAsset(assetId, request)

        // THEN
        val updatedAsset = assetService.getAsset(assetId)
        Assertions.assertThat(updatedAsset.categoryId).isEqualTo(categoryId)
    }

    @Test
    @DisplayName("실패: 에셋 업데이트 시 다른 에셋과 이름이 중복되면 예외가 발생한다")
    fun updateAsset_withDuplicateName_throwsCustomException() {
        // GIVEN: 두 개의 에셋 생성
        assetService!!.createAsset(
            AssetCreateRequest(
                "에셋1", "CODE1", testFileUploader!!.initiateTestFileUpload("f1.png"), null, null
            )
        )
        val assetId2 =
            assetService.createAsset(
                AssetCreateRequest(
                    "에셋2", "CODE2", testFileUploader.initiateTestFileUpload("f2.png"), null, null
                )
            )

        // WHEN & THEN: 두 번째 에셋의 이름을 첫 번째 에셋의 이름으로 변경 시도
        val request = AssetUpdateRequest("에셋1", "CODE2", null, null, null)
        org.junit.jupiter.api.Assertions.assertThrows<CustomException?>(CustomException::class.java, Executable { assetService.updateAsset(assetId2!!, request) })
    }

    @Test
    @DisplayName("성공: 에셋이 없는 경우 전체 조회 시 빈 리스트를 반환한다")
    fun getAssets_whenNoAssetsExist_returnsEmptyList() {
        // GIVEN: 에셋이 없는 상태
        assetRepository!!.deleteAll()

        // WHEN
        val responses: MutableList<AssetResponse> = assetService!!.assets

        // THEN
        Assertions.assertThat<AssetResponse?>(responses).isNotNull().isEmpty()
    }

    @Test
    @DisplayName("성공: 특정 카테고리에 에셋이 없는 경우 조회 시 빈 리스트를 반환한다")
    fun getAssetsByCategory_whenNoAssetsInCategory_returnsEmptyList() {
        // GIVEN: 에셋이 없는 카테고리 생성
        val categoryId =
            assetCategoryService!!.createAssetCategory(
                AssetCategoryCreateRequest("빈 카테고리", "EMPTY", null, null)
            )

        // WHEN
        val responses: MutableList<AssetResponse> = assetService!!.getAssetsByCategory(categoryId)

        // THEN
        Assertions.assertThat<AssetResponse?>(responses).isNotNull().isEmpty()
    }

    @Test
    @DisplayName("성공: 이미 카테고리가 있는 에셋에 다른 카테고리를 할당하면 교체된다")
    fun assignCategory_toAssetWithExistingCategory_replacesCategory() {
        // GIVEN
        val originalCategoryId =
            assetCategoryService!!.createAssetCategory(
                AssetCategoryCreateRequest("원본 카테고리", "ORI_C", null, null)
            )
        val assetId =
            assetService!!.createAsset(
                AssetCreateRequest(
                    "에셋",
                    "CODE",
                    testFileUploader!!.initiateTestFileUpload("file.png"),
                    null,
                    originalCategoryId
                )
            )

        val newCategoryId =
            assetCategoryService.createAssetCategory(
                AssetCategoryCreateRequest("새 카테고리", "NEW_C", null, null)
            )

        // WHEN
        assetService.assignCategory(assetId!!, newCategoryId)

        // THEN
        val updatedAsset = assetService.getAsset(assetId)
        Assertions.assertThat(updatedAsset.categoryId).isEqualTo(newCategoryId)
        Assertions.assertThat(updatedAsset.categoryName).isEqualTo("새 카테고리")
    }
}
