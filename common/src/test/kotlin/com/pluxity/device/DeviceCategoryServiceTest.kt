package com.pluxity.device

import com.pluxity.config.MockBeansConfig
import com.pluxity.device.dto.DeviceCategoryRequest
import com.pluxity.device.dto.DeviceCategoryResponse
import com.pluxity.device.dto.DeviceCategoryUpdateRequest
import com.pluxity.device.entity.Device
import com.pluxity.device.entity.DeviceCompanyType
import com.pluxity.device.entity.DeviceType
import com.pluxity.device.repository.DeviceCategoryRepository
import com.pluxity.device.repository.DeviceRepository
import com.pluxity.device.service.DeviceCategoryService
import com.pluxity.file.constant.FileStatus
import com.pluxity.file.dto.FileResponse
import com.pluxity.global.exception.CustomException
import com.pluxity.util.TestFileUploader
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@SpringBootTest
@Import(MockBeansConfig::class)
@Transactional
internal class DeviceCategoryServiceTest
    @Autowired
    constructor(
        private val deviceCategoryService: DeviceCategoryService,
        private val deviceCategoryRepository: DeviceCategoryRepository,
        private val testFileUploader: TestFileUploader,
        private val deviceRepository: DeviceRepository,
    ) {
        @Test
        @DisplayName("성공: 유효한 요청으로 최상위 카테고리 생성 시 모든 필드가 정상적으로 저장된다")
        fun create_withValidRequestForRootCategory_savesCategory() {
            // GIVEN
            val iconFileId = testFileUploader.initiateTestFileUpload("root_icon.png")
            val request = DeviceCategoryRequest("루트 카테고리", null, iconFileId)

            // WHEN
            val createdId = deviceCategoryService.create(request)

            // THEN
            val response = deviceCategoryService.getDeviceCategory(createdId)

            Assertions.assertThat(response.id).isEqualTo(createdId)
            Assertions.assertThat(response.name).isEqualTo("루트 카테고리")
            Assertions.assertThat(response.parentId).isNull()
            Assertions.assertThat(response.depth).isEqualTo(1)
            Assertions.assertThat<DeviceCategoryResponse>(response.children).isNotNull().isEmpty()
            Assertions.assertThat<FileResponse>(response.thumbnailFile).isNotNull()
            Assertions.assertThat(response.thumbnailFile.id).isEqualTo(iconFileId)
            Assertions.assertThat(response.thumbnailFile.originalFileName).isEqualTo("root_icon.png")
            Assertions.assertThat(response.thumbnailFile.fileStatus).isEqualTo(FileStatus.COMPLETE.name)
        }

        @Test
        @DisplayName("성공: 유효한 요청으로 하위 카테고리를 생성하고 계층 구조를 검증한다")
        fun create_withValidRequestForChildCategory_savesCategoryWithHierarchy() {
            // GIVEN
            val parentId = deviceCategoryService.create(DeviceCategoryRequest("부모", null, null))
            val childIconId = testFileUploader.initiateTestFileUpload("child_icon.png")
            val childRequest = DeviceCategoryRequest("자식", parentId, childIconId)

            // WHEN
            val childId = deviceCategoryService.create(childRequest)

            // THEN
            val response = deviceCategoryService.getDeviceCategory(childId)
            Assertions.assertThat(response.id).isEqualTo(childId)
            Assertions.assertThat(response.name).isEqualTo("자식")
            Assertions.assertThat(response.parentId).isEqualTo(parentId)
            Assertions.assertThat(response.depth).isEqualTo(2)
        }

        @Test
        @DisplayName("성공: 유효한 요청으로 카테고리 정보를 수정하고 모든 필드의 변경사항을 검증한다")
        fun update_withValidRequest_updatesCategory() {
            // GIVEN
            val originalParentId =
                deviceCategoryService.create(DeviceCategoryRequest("원본 부모", null, null))
            val categoryId =
                deviceCategoryService.create(
                    DeviceCategoryRequest(
                        "원본 이름",
                        originalParentId,
                        testFileUploader.initiateTestFileUpload("old.png"),
                    ),
                )

            val newParentId = deviceCategoryService.create(DeviceCategoryRequest("새 부모", null, null))
            val newIconId = testFileUploader.initiateTestFileUpload("new.png")
            val updateRequest =
                DeviceCategoryUpdateRequest("수정된 이름", newParentId, newIconId)

            // WHEN
            deviceCategoryService.update(categoryId, updateRequest)

            // THEN
            val response = deviceCategoryService.getDeviceCategory(categoryId)
            Assertions.assertThat(response.name).isEqualTo("수정된 이름")
            Assertions.assertThat(response.parentId).isEqualTo(newParentId)
            Assertions.assertThat(response.depth).isEqualTo(2)
            Assertions.assertThat(response.thumbnailFile.id).isEqualTo(newIconId)
        }

        @Test
        @DisplayName("성공: 전체 카테고리 조회 시 올바른 트리 구조를 반환한다")
        fun getDeviceCategories_withHierarchy_returnsTreeStructure() {
            // GIVEN
            val root1Id = deviceCategoryService.create(DeviceCategoryRequest("루트1", null, null))
            val child1Id = deviceCategoryService.create(DeviceCategoryRequest("자식1", root1Id, null))
            val root2Id =
                deviceCategoryService.create(
                    DeviceCategoryRequest(
                        "루트2",
                        null,
                        testFileUploader.initiateTestFileUpload("r2.png"),
                    ),
                )

            // WHEN
            val response: List<DeviceCategoryResponse> = deviceCategoryService.getDeviceCategories()

            // THEN
            Assertions.assertThat(response).hasSize(2)
            val root1 = response.first { it.id == root1Id }
            Assertions.assertThat(root1.children).hasSize(1)
            Assertions.assertThat(root1.children.first().id).isEqualTo(child1Id)
        }

        @Test
        @DisplayName("성공: 특정 부모의 직속 자식 목록을 정확히 조회한다")
        fun getChildDeviceCategories_returnsListOfDirectChildren() {
            // GIVEN
            val parentId = deviceCategoryService.create(DeviceCategoryRequest("부모", null, null))
            val child1Id = deviceCategoryService.create(DeviceCategoryRequest("자식1", parentId, null))
            val child2Id = deviceCategoryService.create(DeviceCategoryRequest("자식2", parentId, null))

            // WHEN
            val children: List<DeviceCategoryResponse> =
                deviceCategoryService.getChildDeviceCategories(parentId)

            // THEN
            Assertions.assertThat(children).hasSize(2)
            Assertions
                .assertThat(children.map { it.id })
                .containsExactlyInAnyOrder(child1Id, child2Id)
            Assertions.assertThat(children.first().children).isEmpty()
        }

        @Test
        @DisplayName("성공: 연관관계가 없는 카테고리는 정상적으로 삭제된다")
        fun delete_withEmptyCategory_deletesSuccessfully() {
            // GIVEN
            val categoryId =
                deviceCategoryService.create(DeviceCategoryRequest("삭제될 카테고리", null, null))

            // WHEN
            deviceCategoryService.delete(categoryId)

            // THEN
            Assertions.assertThat(deviceCategoryRepository.existsById(categoryId)).isFalse()
        }

        @Test
        @DisplayName("실패: 자식 카테고리가 있는 카테고리 삭제 시 예외가 발생한다")
        fun delete_withChildCategories_throwsCustomException() {
            // GIVEN
            val parentId = deviceCategoryService.create(DeviceCategoryRequest("부모", null, null))
            deviceCategoryService.create(DeviceCategoryRequest("자식", parentId, null))

            // WHEN & THEN
            org.junit.jupiter.api.Assertions.assertThrows<CustomException?>(
                CustomException::class.java,
                { deviceCategoryService.delete(parentId) },
            )
        }

        @Test
        @DisplayName("실패: 연결된 디바이스가 있는 카테고리 삭제 시 예외가 발생한다")
        fun delete_withAssociatedDevices_throwsCustomException() {
            // GIVEN
            val categoryId =
                deviceCategoryService.create(DeviceCategoryRequest("디바이스 있는 카테고리", null, null))
            val category = deviceCategoryService.findById(categoryId)
            val device =
                Device(
                    UUID.randomUUID().toString(),
                    "name",
                    null,
                    category,
                    DeviceType.TEMP_HUM,
                    DeviceCompanyType.DAWONDNS,
                )

            device.changeCategory(category)
            deviceRepository.save<Device?>(device)

            // WHEN & THEN
            assertThrows<CustomException> {
                deviceCategoryService.delete(categoryId)
            }
        }

        @Test
        @DisplayName("실패: 유효하지 않은 부모 ID로 카테고리 생성 시 예외가 발생한다")
        fun create_withInvalidParentId_throwsCustomException() {
            val request = DeviceCategoryRequest("잘못된 자식", 9999L, null)
            assertThrows<CustomException> {
                deviceCategoryService.create(request)
            }
        }

        @Test
        @DisplayName("실패: 최대 허용 깊이(2)를 초과하여 카테고리 생성 시 예외가 발생한다")
        fun create_exceedsMaxDepth_throwsCustomException() {
            // GIVEN
            val id1 = deviceCategoryService.create(DeviceCategoryRequest("Depth 1", null, null))
            val id2 = deviceCategoryService.create(DeviceCategoryRequest("Depth 2", id1, null))
            val invalidRequest = DeviceCategoryRequest("Depth 3", id2, null)

            // WHEN & THEN
            assertThrows<CustomException> {
                deviceCategoryService.create(invalidRequest)
            }
        }

        @Test
        @DisplayName("실패: 자기 자신을 부모로 지정하여 수정 시 예외가 발생한다")
        fun update_setSelfAsParent_throwsException() {
            // GIVEN
            val categoryId = deviceCategoryService.create(DeviceCategoryRequest("카테고리", null, null))
            val request = DeviceCategoryUpdateRequest("이름변경", categoryId, null)

            // WHEN & THEN
            assertThrows<CustomException> {
                deviceCategoryService.update(categoryId, request)
            }
        }

        @Test
        @DisplayName("실패: 자신의 자식을 부모로 지정하여 수정 시 예외가 발생한다")
        fun update_setChildAsParent_throwsException() {
            // GIVEN
            val parentId = deviceCategoryService.create(DeviceCategoryRequest("부모", null, null))
            val childId = deviceCategoryService.create(DeviceCategoryRequest("자식", parentId, null))
            val request = DeviceCategoryUpdateRequest("순환참조", childId, null)

            // WHEN & THEN
            assertThrows<CustomException> {
                deviceCategoryService.update(parentId, request)
            }
        }

        @Test
        @DisplayName("성공: 아이콘 파일 없이 카테고리를 생성할 수 있다")
        fun create_withoutIconFile_succeeds() {
            // GIVEN
            val request = DeviceCategoryRequest("아이콘 없는 카테고리", null, null)

            // WHEN
            val createdId = deviceCategoryService.create(request)

            // THEN
            val response = deviceCategoryService.getDeviceCategory(createdId)
            Assertions.assertThat<DeviceCategoryResponse?>(response).isNotNull()
            Assertions.assertThat(response.name).isEqualTo("아이콘 없는 카테고리")
            Assertions.assertThat<FileResponse>(response.thumbnailFile).isNotNull()
            Assertions.assertThat(response.thumbnailFile.id).isNull()
        }

        @Test
        @DisplayName("성공: 카테고리 수정 시 아이콘 파일을 제거(null)할 수 있다")
        fun update_toNullIconFile_updatesSuccessfully() {
            // GIVEN
            val iconFileId = testFileUploader.initiateTestFileUpload("icon.png")
            val categoryId =
                deviceCategoryService.create(DeviceCategoryRequest("아이콘 있는 카테고리", null, iconFileId))
            Assertions
                .assertThat(deviceCategoryService.getDeviceCategory(categoryId).thumbnailFile.id)
                .isNotNull()

            // WHEN: thumbnailFileId를 null로 하여 업데이트
            val request =
                DeviceCategoryUpdateRequest("아이콘 제거된 카테고리", null, null)
            deviceCategoryService.update(categoryId, request)

            // THEN
            val updatedCategory = deviceCategoryService.getDeviceCategory(categoryId)
            Assertions.assertThat(updatedCategory.name).isEqualTo("아이콘 제거된 카테고리")
            Assertions.assertThat(updatedCategory.thumbnailFile.id).isNull()
        }

        @Test
        @DisplayName("성공: 자식 카테고리를 최상위(루트) 카테고리로 변경할 수 있다")
        fun update_changeChildToRootCategory_succeeds() {
            // GIVEN
            val parentId = deviceCategoryService.create(DeviceCategoryRequest("부모", null, null))
            val childId = deviceCategoryService.create(DeviceCategoryRequest("자식", parentId, null))
            Assertions.assertThat(deviceCategoryService.getDeviceCategory(childId).depth).isEqualTo(2)

            // WHEN: 부모 ID를 null로 하여 업데이트 (최상위로 변경)
            val request = DeviceCategoryUpdateRequest("이제 루트", null, null)
            deviceCategoryService.update(childId, request)

            // THEN
            val response = deviceCategoryService.getDeviceCategory(childId)
            Assertions.assertThat(response.parentId).isNull()
            Assertions.assertThat(response.depth).isEqualTo(1)
        }

        @Test
        @DisplayName("성공: 카테고리가 하나도 없을 때 전체 트리 조회 시 빈 리스트를 반환한다")
        fun getDeviceCategories_whenNoCategoriesExist_returnsEmptyList() {
            // GIVEN: 모든 카테고리 삭제
            deviceCategoryRepository.deleteAll()

            // WHEN
            val response: List<DeviceCategoryResponse> = deviceCategoryService.getDeviceCategories()

            // THEN
            Assertions.assertThat<DeviceCategoryResponse?>(response).isNotNull()
            Assertions.assertThat<DeviceCategoryResponse?>(response).isEmpty()
        }

        @Test
        @DisplayName("성공: 루트 카테고리가 하나도 없을 때 조회 시 빈 리스트를 반환한다")
        fun getRootDeviceCategoryResponses_whenNoRootCategories_returnsEmptyList() {
            // GIVEN
            deviceCategoryRepository.deleteAll()

            // WHEN
            val responses: List<DeviceCategoryResponse> = deviceCategoryService.getDeviceCategories()

            // THEN
            Assertions.assertThat(responses).isNotNull().isEmpty()
        }

        @Test
        @DisplayName("성공: 자식이 없는 카테고리에서 자식 목록 조회 시 빈 리스트를 반환한다")
        fun getChildDeviceCategories_forCategoryWithNoChildren_returnsEmptyList() {
            // GIVEN
            val parentId = deviceCategoryService.create(DeviceCategoryRequest("자식 없는 부모", null, null))

            // WHEN
            val children: List<DeviceCategoryResponse> =
                deviceCategoryService.getChildDeviceCategories(parentId)

            // THEN
            Assertions.assertThat(children).isNotNull().isEmpty()
        }

        @Test
        @DisplayName("성공: 존재하지 않는 카테고리의 자식 목록 조회 시 빈 리스트를 반환한다")
        fun getChildDeviceCategories_withNonExistingParentId_throwsCustomException() {
            // GIVEN
            val nonExistingParentId = 9999L

            // WHEN & THEN
            val childDeviceCategories: List<DeviceCategoryResponse> =
                deviceCategoryService.getChildDeviceCategories(nonExistingParentId)

            Assertions.assertThat<DeviceCategoryResponse?>(childDeviceCategories).isNotNull().isEmpty()
        }
    }
