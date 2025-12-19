package com.pluxity.asset

import com.pluxity.asset.dto.AssetCategoryCreateRequest
import com.pluxity.asset.dto.AssetCreateRequest
import com.pluxity.asset.dto.AssetResponse
import com.pluxity.asset.dto.AssetUpdateRequest
import com.pluxity.asset.repository.AssetRepository
import com.pluxity.asset.service.AssetCategoryService
import com.pluxity.asset.service.AssetService
import com.pluxity.config.MockBeansConfig
import com.pluxity.file.constant.FileStatus
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
class AssetServiceTest
    @Autowired
    constructor(
        private val assetService: AssetService,
        private val assetRepository: AssetRepository,
        private val assetCategoryService: AssetCategoryService,
        private val testFileUploader: TestFileUploader,
    ) {
        @Test
        @DisplayName("성공: 유효한 요청으로 에셋을 생성하고, 모든 응답 필드를 상세히 검증한다")
        fun createAsset_WithValidRequest_SavesAssetAndReturnsDetailedResponse() {
            val assetFileId = testFileUploader.initiateTestFileUpload("asset_file.glb")
            val thumbnailFileId = testFileUploader.initiateTestFileUpload("thumbnail_image.png")
            val categoryId = assetCategoryService.createAssetCategory(AssetCategoryCreateRequest("테스트 카테고리", "TCC", null, null))
            val request = AssetCreateRequest("테스트 에셋", "TES", assetFileId, thumbnailFileId, categoryId)

            val createdAssetId = assetService.createAsset(request)

            val response: AssetResponse = assetService.getAsset(createdAssetId)
            assertThat(response.id).isEqualTo(createdAssetId)
            assertThat(response.name).isEqualTo(request.name)
            assertThat(response.code).isEqualTo(request.code)
            assertThat(response.categoryId).isEqualTo(categoryId)
            assertThat(response.categoryName).isEqualTo("테스트 카테고리")
            assertThat(response.categoryCode).isEqualTo("TCC")
            checkNotNull(response.file)
            assertThat(response.file.id).isEqualTo(assetFileId)
            assertThat(response.file.originalFileName).isEqualTo("asset_file.glb")
            assertThat(response.file.fileStatus).isEqualTo(FileStatus.COMPLETE.name)
            checkNotNull(response.thumbnailFile)
            assertThat(response.thumbnailFile.id).isEqualTo(thumbnailFileId)
            assertThat(response.thumbnailFile.originalFileName).isEqualTo("thumbnail_image.png")
            assertThat(response.thumbnailFile.fileStatus).isEqualTo(FileStatus.COMPLETE.name)
        }

        @Test
        @DisplayName("성공: 전체 에셋 조회 시 상세 정보가 포함된 목록을 반환한다")
        fun getAssets_ReturnsListOfDetailedAssetResponses() {
            val categoryId = assetCategoryService.createAssetCategory(AssetCategoryCreateRequest("카테고리", "CAT", null, null))
            assetService.createAsset(AssetCreateRequest("에셋1", "AS1", testFileUploader.initiateTestFileUpload("f1.png"), null, categoryId))
            assetService.createAsset(AssetCreateRequest("에셋2", "AS2", testFileUploader.initiateTestFileUpload("f2.png"), null, categoryId))

            val responses: List<AssetResponse> = assetService.getAssets()
            assertThat(responses).hasSize(2)
            val firstAsset = responses.first { it.code == "AS1" }
            assertThat(firstAsset.name).isEqualTo("에셋1")
            assertThat(firstAsset.categoryId).isEqualTo(categoryId)
            assertThat(firstAsset.file).isNotNull
        }

        @Test
        @DisplayName("성공: 유효한 요청으로 에셋 정보를 수정하고, 모든 필드의 변경사항을 검증한다")
        fun updateAsset_WithValidRequest_UpdatesAssetAndVerifiesChanges() {
            val originalCategoryId = assetCategoryService.createAssetCategory(AssetCategoryCreateRequest("원본 카테고리", "ORI", null, null))
            val originalAssetId =
                assetService.createAsset(
                    AssetCreateRequest(
                        "원본 에셋",
                        "ORI_A",
                        testFileUploader.initiateTestFileUpload("ori_f.png"),
                        testFileUploader.initiateTestFileUpload("ori_t.png"),
                        originalCategoryId,
                    ),
                )

            val newCategoryId = assetCategoryService.createAssetCategory(AssetCategoryCreateRequest("새 카테고리", "NEW_C", null, null))
            val newFileId = testFileUploader.initiateTestFileUpload("new_f.png")
            val newThumbnailId = testFileUploader.initiateTestFileUpload("new_t.png")
            val request = AssetUpdateRequest("수정된 에셋", "UPD_A", newFileId, newThumbnailId, newCategoryId)

            assetService.updateAsset(originalAssetId, request)

            val updatedAsset = assetService.getAsset(originalAssetId)
            assertThat(updatedAsset.name).isEqualTo("수정된 에셋")
            assertThat(updatedAsset.code).isEqualTo("UPD_A")
            assertThat(updatedAsset.categoryId).isEqualTo(newCategoryId)
            assertThat(updatedAsset.categoryName).isEqualTo("새 카테고리")
            checkNotNull(updatedAsset.file)
            checkNotNull(updatedAsset.thumbnailFile)
            assertThat(updatedAsset.file.id).isEqualTo(newFileId)
            assertThat(updatedAsset.thumbnailFile.id).isEqualTo(newThumbnailId)
        }

        @Test
        @DisplayName("성공: 에셋을 삭제하면 DB에서 조회되지 않는다")
        fun deleteAsset_RemovesAsset() {
            val assetId =
                assetService.createAsset(
                    AssetCreateRequest("삭제될 에셋", "DEL", testFileUploader.initiateTestFileUpload("del.png"), null, null),
                )
            assertThat(assetRepository.findById(assetId)).isPresent
            assetService.deleteAsset(assetId)
            assertThrows<CustomException> { assetService.getAsset(assetId) }
            assertThat(assetRepository.findById(assetId)).isEmpty
        }

        @Test
        @DisplayName("성공: 에셋에 카테고리를 할당하면 정보가 업데이트된다")
        fun assignCategory_UpdatesAssetCategory() {
            val assetId =
                assetService.createAsset(
                    AssetCreateRequest("카테고리 없는 에셋", "NO_CAT", testFileUploader.initiateTestFileUpload("file.png"), null, null),
                )
            assertThat(assetService.getAsset(assetId).categoryId).isNull()

            val categoryId = assetCategoryService.createAssetCategory(AssetCategoryCreateRequest("할당될 카테고리", "ASSIGN", null, null))
            assetService.assignCategory(assetId, categoryId)
            val updatedAsset = assetService.getAsset(assetId)
            assertThat(updatedAsset.categoryId).isEqualTo(categoryId)
            assertThat(updatedAsset.categoryName).isEqualTo("할당될 카테고리")
            assertThat(updatedAsset.categoryCode).isEqualTo("ASSIGN")
        }

        @Test
        @DisplayName("성공: 에셋의 카테고리를 제거하면 null로 변경된다")
        fun removeCategory_SetsAssetCategoryToNull() {
            val categoryId = assetCategoryService.createAssetCategory(AssetCategoryCreateRequest("제거될 카테고리", "REM", null, null))
            val assetId =
                assetService.createAsset(
                    AssetCreateRequest("카테고리 있는 에셋", "HAS_CAT", testFileUploader.initiateTestFileUpload("file.png"), null, categoryId),
                )
            assertThat(assetService.getAsset(assetId).categoryId).isNotNull()

            assetService.removeCategory(assetId)
            val updatedAsset = assetService.getAsset(assetId)
            assertThat(updatedAsset.categoryId).isNull()
            assertThat(updatedAsset.categoryName).isNull()
            assertThat(updatedAsset.categoryCode).isNull()
        }

        @Test
        @DisplayName("실패: 중복된 코드로 에셋 생성 시 예외가 발생한다")
        fun createAsset_WithDuplicateCode_ThrowsCustomException() {
            assetService.createAsset(AssetCreateRequest("첫 에셋", "DUP_CODE", testFileUploader.initiateTestFileUpload("f1.png"), null, null))
            val duplicateRequest = AssetCreateRequest("두 번째 에셋", "DUP_CODE", testFileUploader.initiateTestFileUpload("f2.png"), null, null)
            assertThrows<CustomException> { assetService.createAsset(duplicateRequest) }
        }

        @Test
        @DisplayName("실패: 유효하지 않은 파일 ID로 에셋 생성 시 예외가 발생한다")
        fun createAsset_WithInvalidFileId_ThrowsCustomException() {
            val invalidFileId = 9999L
            val request = AssetCreateRequest("에셋", "CODE", invalidFileId, null, null)
            assertThrows<CustomException> { assetService.createAsset(request) }
        }

        @Test
        @DisplayName("실패: 유효하지 않은 카테고리 ID로 에셋 생성 시 예외가 발생한다")
        fun createAsset_WithInvalidCategoryId_ThrowsCustomException() {
            val invalidCategoryId = 9999L
            val request = AssetCreateRequest("에셋", "CODE", testFileUploader.initiateTestFileUpload("file.png"), null, invalidCategoryId)
            assertThrows<CustomException> { assetService.createAsset(request) }
        }

        @Test
        @DisplayName("실패: 존재하지 않는 에셋 업데이트 시 예외가 발생한다")
        fun updateAsset_WithNonExistingId_ThrowsCustomException() {
            val nonExistingId = 9999L
            val request = AssetUpdateRequest("수정", "UPD", null, null, null)
            assertThrows<CustomException> { assetService.updateAsset(nonExistingId, request) }
        }

        @Test
        @DisplayName("실패: 존재하지 않는 에셋 삭제 시 예외가 발생한다")
        fun deleteAsset_WithNonExistingId_ThrowsCustomException() {
            val nonExistingId = 9999L
            assertThrows<CustomException> { assetService.deleteAsset(nonExistingId) }
        }

        @Test
        @DisplayName("실패: 에셋에 이미 카테고리가 없을 때 제거 시도 시 예외가 발생한다")
        fun removeCategory_FromAssetWithNoCategory_ThrowsCustomException() {
            val assetId =
                assetService.createAsset(
                    AssetCreateRequest("카테고리 없는 에셋", "NO_CAT", testFileUploader.initiateTestFileUpload("file.png"), null, null),
                )
            assertThrows<CustomException> { assetService.removeCategory(assetId) }
        }

        @Test
        @DisplayName("실패: 중복된 이름으로 에셋 생성 시 예외가 발생한다")
        fun createAsset_WithDuplicateName_ThrowsCustomException() {
            assetService.createAsset(AssetCreateRequest("중복된 이름", "CODE1", testFileUploader.initiateTestFileUpload("f1.png"), null, null))
            val duplicateRequest = AssetCreateRequest("중복된 이름", "CODE2", testFileUploader.initiateTestFileUpload("f2.png"), null, null)
            assertThrows<CustomException> { assetService.createAsset(duplicateRequest) }
        }

        @Test
        @DisplayName("성공: 카테고리 없이 에셋을 생성할 수 있다")
        fun createAsset_withNullCategoryId_succeeds() {
            val request = AssetCreateRequest("카테고리 없는 에셋", "NO_CAT", testFileUploader.initiateTestFileUpload("file.png"), null, null)
            val createdAssetId = assetService.createAsset(request)
            val response = assetService.getAsset(createdAssetId)
            assertThat(response).isNotNull
            assertThat(response.categoryId).isNull()
            assertThat(response.categoryName).isNull()
        }

        @Test
        @DisplayName("성공: 에셋 정보 수정 시 이름만 변경해도 정상적으로 반영된다")
        fun updateAsset_onlyWithName_updatesSuccessfully() {
            val assetId =
                assetService.createAsset(
                    AssetCreateRequest("원본 이름", "CODE", testFileUploader.initiateTestFileUpload("file.png"), null, null),
                )
            val request = AssetUpdateRequest("새로운 이름", "CODE", null, null, null)
            assetService.updateAsset(assetId, request)
            val updatedAsset = assetService.getAsset(assetId)
            assertThat(updatedAsset.name).isEqualTo("새로운 이름")
            assertThat(updatedAsset.code).isEqualTo("CODE")
        }

        @Test
        @DisplayName("성공: 에셋 정보 수정 시 카테고리를 null 로 받더라도 카테고리는 변경되지 않는다.")
        fun updateAsset_toNullCategory_updatesSuccessfully() {
            val categoryId = assetCategoryService.createAssetCategory(AssetCategoryCreateRequest("카테고리", "CAT", null, null))
            val assetId =
                assetService.createAsset(
                    AssetCreateRequest("에셋", "CODE", testFileUploader.initiateTestFileUpload("file.png"), null, categoryId),
                )
            assertThat(assetService.getAsset(assetId).categoryId).isNotNull()
            val request = AssetUpdateRequest("에셋", "CODE", null, null, null)
            assetService.updateAsset(assetId, request)
            val updatedAsset = assetService.getAsset(assetId)
            assertThat(updatedAsset.categoryId).isEqualTo(categoryId)
        }

        @Test
        @DisplayName("실패: 에셋 업데이트 시 다른 에셋과 이름이 중복되면 예외가 발생한다")
        fun updateAsset_withDuplicateName_throwsCustomException() {
            assetService.createAsset(AssetCreateRequest("에셋1", "CODE1", testFileUploader.initiateTestFileUpload("f1.png"), null, null))
            val assetId2 =
                assetService.createAsset(
                    AssetCreateRequest("에셋2", "CODE2", testFileUploader.initiateTestFileUpload("f2.png"), null, null),
                )
            val request = AssetUpdateRequest("에셋1", "CODE2", null, null, null)
            assertThrows<CustomException> { assetService.updateAsset(assetId2, request) }
        }

        @Test
        @DisplayName("성공: 에셋이 없는 경우 전체 조회 시 빈 리스트를 반환한다")
        fun getAssets_whenNoAssetsExist_returnsEmptyList() {
            assetRepository.deleteAll()
            val responses = assetService.getAssets()
            assertThat(responses).isNotNull.isEmpty()
        }

        @Test
        @DisplayName("성공: 특정 카테고리에 에셋이 없는 경우 조회 시 빈 리스트를 반환한다")
        fun getAssetsByCategory_whenNoAssetsInCategory_returnsEmptyList() {
            val categoryId = assetCategoryService.createAssetCategory(AssetCategoryCreateRequest("빈 카테고리", "EMPTY", null, null))
            val responses = assetService.getAssetsByCategory(categoryId)
            assertThat(responses).isNotNull.isEmpty()
        }
    }
