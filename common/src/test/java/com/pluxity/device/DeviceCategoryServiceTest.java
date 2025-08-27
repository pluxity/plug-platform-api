package com.pluxity.device;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pluxity.config.MockBeansConfig;
import com.pluxity.device.dto.*;
import com.pluxity.device.entity.Device;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.device.entity.DeviceCompanyType;
import com.pluxity.device.entity.DeviceType;
import com.pluxity.device.repository.DeviceCategoryRepository;
import com.pluxity.device.repository.DeviceRepository;
import com.pluxity.device.service.DeviceCategoryService;
import com.pluxity.file.constant.FileStatus;
import com.pluxity.global.exception.CustomException;
import com.pluxity.util.TestFileUploader;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Import(MockBeansConfig.class)
@Transactional
class DeviceCategoryServiceTest {

    @Autowired private DeviceCategoryService deviceCategoryService;
    @Autowired private DeviceCategoryRepository deviceCategoryRepository;
    @Autowired private TestFileUploader testFileUploader;
    @Autowired private DeviceRepository deviceRepository;

    @Test
    @DisplayName("성공: 유효한 요청으로 최상위 카테고리 생성 시 모든 필드가 정상적으로 저장된다")
    void create_withValidRequestForRootCategory_savesCategory() {
        // GIVEN
        Long iconFileId = testFileUploader.initiateTestFileUpload("root_icon.png");
        DeviceCategoryRequest request = new DeviceCategoryRequest("루트 카테고리", null, iconFileId);

        // WHEN
        Long createdId = deviceCategoryService.create(request);

        // THEN
        DeviceCategoryResponse response = deviceCategoryService.getDeviceCategory(createdId);

        assertThat(response.id()).isEqualTo(createdId);
        assertThat(response.name()).isEqualTo("루트 카테고리");
        assertThat(response.parentId()).isNull();
        assertThat(response.depth()).isEqualTo(1);
        assertThat(response.children()).isNotNull().isEmpty();
        assertThat(response.thumbnailFile()).isNotNull();
        assertThat(response.thumbnailFile().id()).isEqualTo(iconFileId);
        assertThat(response.thumbnailFile().originalFileName()).isEqualTo("root_icon.png");
        assertThat(response.thumbnailFile().fileStatus()).isEqualTo(FileStatus.COMPLETE.name());
    }

    @Test
    @DisplayName("성공: 유효한 요청으로 하위 카테고리를 생성하고 계층 구조를 검증한다")
    void create_withValidRequestForChildCategory_savesCategoryWithHierarchy() {
        // GIVEN
        Long parentId = deviceCategoryService.create(new DeviceCategoryRequest("부모", null, null));
        Long childIconId = testFileUploader.initiateTestFileUpload("child_icon.png");
        DeviceCategoryRequest childRequest = new DeviceCategoryRequest("자식", parentId, childIconId);

        // WHEN
        Long childId = deviceCategoryService.create(childRequest);

        // THEN
        DeviceCategoryResponse response = deviceCategoryService.getDeviceCategory(childId);
        assertThat(response.id()).isEqualTo(childId);
        assertThat(response.name()).isEqualTo("자식");
        assertThat(response.parentId()).isEqualTo(parentId);
        assertThat(response.depth()).isEqualTo(2);
    }

    @Test
    @DisplayName("성공: 유효한 요청으로 카테고리 정보를 수정하고 모든 필드의 변경사항을 검증한다")
    void update_withValidRequest_updatesCategory() {
        // GIVEN
        Long originalParentId =
                deviceCategoryService.create(new DeviceCategoryRequest("원본 부모", null, null));
        Long categoryId =
                deviceCategoryService.create(
                        new DeviceCategoryRequest(
                                "원본 이름", originalParentId, testFileUploader.initiateTestFileUpload("old.png")));

        Long newParentId = deviceCategoryService.create(new DeviceCategoryRequest("새 부모", null, null));
        Long newIconId = testFileUploader.initiateTestFileUpload("new.png");
        DeviceCategoryUpdateRequest updateRequest =
                new DeviceCategoryUpdateRequest("수정된 이름", newParentId, newIconId);

        // WHEN
        deviceCategoryService.update(categoryId, updateRequest);

        // THEN
        DeviceCategoryResponse response = deviceCategoryService.getDeviceCategory(categoryId);
        assertThat(response.name()).isEqualTo("수정된 이름");
        assertThat(response.parentId()).isEqualTo(newParentId);
        assertThat(response.depth()).isEqualTo(2);
        assertThat(response.thumbnailFile().id()).isEqualTo(newIconId);
    }

    @Test
    @DisplayName("성공: 전체 카테고리 조회 시 올바른 트리 구조를 반환한다")
    void getDeviceCategories_withHierarchy_returnsTreeStructure() {
        // GIVEN
        Long root1Id = deviceCategoryService.create(new DeviceCategoryRequest("루트1", null, null));
        Long child1Id = deviceCategoryService.create(new DeviceCategoryRequest("자식1", root1Id, null));
        Long root2Id =
                deviceCategoryService.create(
                        new DeviceCategoryRequest(
                                "루트2", null, testFileUploader.initiateTestFileUpload("r2.png")));

        // WHEN
        List<DeviceCategoryResponse> response = deviceCategoryService.getDeviceCategories();

        // THEN
        assertThat(response).hasSize(2);
        DeviceCategoryResponse root1 =
                response.stream().filter(c -> c.id().equals(root1Id)).findFirst().orElseThrow();
        assertThat(root1.children()).hasSize(1);
        assertThat(root1.children().getFirst().id()).isEqualTo(child1Id);
    }

    @Test
    @DisplayName("성공: 특정 부모의 직속 자식 목록을 정확히 조회한다")
    void getChildDeviceCategories_returnsListOfDirectChildren() {
        // GIVEN
        Long parentId = deviceCategoryService.create(new DeviceCategoryRequest("부모", null, null));
        Long child1Id = deviceCategoryService.create(new DeviceCategoryRequest("자식1", parentId, null));
        Long child2Id = deviceCategoryService.create(new DeviceCategoryRequest("자식2", parentId, null));

        // WHEN
        List<DeviceCategoryResponse> children =
                deviceCategoryService.getChildDeviceCategories(parentId);

        // THEN
        assertThat(children).hasSize(2);
        assertThat(children.stream().map(DeviceCategoryResponse::id))
                .containsExactlyInAnyOrder(child1Id, child2Id);
        assertThat(children.getFirst().children()).isEmpty();
    }

    @Test
    @DisplayName("성공: 연관관계가 없는 카테고리는 정상적으로 삭제된다")
    void delete_withEmptyCategory_deletesSuccessfully() {
        // GIVEN
        Long categoryId =
                deviceCategoryService.create(new DeviceCategoryRequest("삭제될 카테고리", null, null));

        // WHEN
        deviceCategoryService.delete(categoryId);

        // THEN
        assertThat(deviceCategoryRepository.existsById(categoryId)).isFalse();
    }

    @Test
    @DisplayName("실패: 자식 카테고리가 있는 카테고리 삭제 시 예외가 발생한다")
    void delete_withChildCategories_throwsCustomException() {
        // GIVEN
        Long parentId = deviceCategoryService.create(new DeviceCategoryRequest("부모", null, null));
        deviceCategoryService.create(new DeviceCategoryRequest("자식", parentId, null));

        // WHEN & THEN
        assertThrows(CustomException.class, () -> deviceCategoryService.delete(parentId));
    }

    @Test
    @DisplayName("실패: 연결된 디바이스가 있는 카테고리 삭제 시 예외가 발생한다")
    void delete_withAssociatedDevices_throwsCustomException() {
        // GIVEN
        Long categoryId =
                deviceCategoryService.create(new DeviceCategoryRequest("디바이스 있는 카테고리", null, null));
        DeviceCategory category = deviceCategoryService.findById(categoryId);
        Device device =
                new Device(
                        UUID.randomUUID().toString(),
                        "name",
                        null,
                        category,
                        DeviceType.TEMP_HUM,
                        DeviceCompanyType.DAWONDNS);

        device.changeCategory(category);
        deviceRepository.save(device);

        // WHEN & THEN
        assertThrows(CustomException.class, () -> deviceCategoryService.delete(categoryId));
    }

    @Test
    @DisplayName("실패: 유효하지 않은 부모 ID로 카테고리 생성 시 예외가 발생한다")
    void create_withInvalidParentId_throwsCustomException() {
        DeviceCategoryRequest request = new DeviceCategoryRequest("잘못된 자식", 9999L, null);
        assertThrows(CustomException.class, () -> deviceCategoryService.create(request));
    }

    @Test
    @DisplayName("실패: 최대 허용 깊이(2)를 초과하여 카테고리 생성 시 예외가 발생한다")
    void create_exceedsMaxDepth_throwsCustomException() {
        // GIVEN
        Long id1 = deviceCategoryService.create(new DeviceCategoryRequest("Depth 1", null, null));
        Long id2 = deviceCategoryService.create(new DeviceCategoryRequest("Depth 2", id1, null));
        DeviceCategoryRequest invalidRequest = new DeviceCategoryRequest("Depth 3", id2, null);

        // WHEN & THEN
        assertThrows(CustomException.class, () -> deviceCategoryService.create(invalidRequest));
    }

    @Test
    @DisplayName("실패: 자기 자신을 부모로 지정하여 수정 시 예외가 발생한다")
    void update_setSelfAsParent_throwsException() {
        // GIVEN
        Long categoryId = deviceCategoryService.create(new DeviceCategoryRequest("카테고리", null, null));
        DeviceCategoryUpdateRequest request = new DeviceCategoryUpdateRequest("이름변경", categoryId, null);

        // WHEN & THEN
        assertThrows(CustomException.class, () -> deviceCategoryService.update(categoryId, request));
    }

    @Test
    @DisplayName("실패: 자신의 자식을 부모로 지정하여 수정 시 예외가 발생한다")
    void update_setChildAsParent_throwsException() {
        // GIVEN
        Long parentId = deviceCategoryService.create(new DeviceCategoryRequest("부모", null, null));
        Long childId = deviceCategoryService.create(new DeviceCategoryRequest("자식", parentId, null));
        DeviceCategoryUpdateRequest request = new DeviceCategoryUpdateRequest("순환참조", childId, null);

        // WHEN & THEN
        assertThrows(CustomException.class, () -> deviceCategoryService.update(parentId, request));
    }

    @Test
    @DisplayName("성공: 아이콘 파일 없이 카테고리를 생성할 수 있다")
    void create_withoutIconFile_succeeds() {
        // GIVEN
        DeviceCategoryRequest request = new DeviceCategoryRequest("아이콘 없는 카테고리", null, null);

        // WHEN
        Long createdId = deviceCategoryService.create(request);

        // THEN
        DeviceCategoryResponse response = deviceCategoryService.getDeviceCategory(createdId);
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("아이콘 없는 카테고리");
        assertThat(response.thumbnailFile()).isNotNull();
        assertThat(response.thumbnailFile().id()).isNull();
    }

    @Test
    @DisplayName("성공: 카테고리 수정 시 아이콘 파일을 제거(null)할 수 있다")
    void update_toNullIconFile_updatesSuccessfully() {
        // GIVEN
        Long iconFileId = testFileUploader.initiateTestFileUpload("icon.png");
        Long categoryId =
                deviceCategoryService.create(new DeviceCategoryRequest("아이콘 있는 카테고리", null, iconFileId));
        assertThat(deviceCategoryService.getDeviceCategory(categoryId).thumbnailFile().id())
                .isNotNull();

        // WHEN: thumbnailFileId를 null로 하여 업데이트
        DeviceCategoryUpdateRequest request =
                new DeviceCategoryUpdateRequest("아이콘 제거된 카테고리", null, null);
        deviceCategoryService.update(categoryId, request);

        // THEN
        DeviceCategoryResponse updatedCategory = deviceCategoryService.getDeviceCategory(categoryId);
        assertThat(updatedCategory.name()).isEqualTo("아이콘 제거된 카테고리");
        assertThat(updatedCategory.thumbnailFile().id()).isNull();
    }

    @Test
    @DisplayName("성공: 자식 카테고리를 최상위(루트) 카테고리로 변경할 수 있다")
    void update_changeChildToRootCategory_succeeds() {
        // GIVEN
        Long parentId = deviceCategoryService.create(new DeviceCategoryRequest("부모", null, null));
        Long childId = deviceCategoryService.create(new DeviceCategoryRequest("자식", parentId, null));
        assertThat(deviceCategoryService.getDeviceCategory(childId).depth()).isEqualTo(2);

        // WHEN: 부모 ID를 null로 하여 업데이트 (최상위로 변경)
        DeviceCategoryUpdateRequest request = new DeviceCategoryUpdateRequest("이제 루트", null, null);
        deviceCategoryService.update(childId, request);

        // THEN
        DeviceCategoryResponse response = deviceCategoryService.getDeviceCategory(childId);
        assertThat(response.parentId()).isNull();
        assertThat(response.depth()).isEqualTo(1);
    }

    @Test
    @DisplayName("성공: 카테고리가 하나도 없을 때 전체 트리 조회 시 빈 리스트를 반환한다")
    void getDeviceCategories_whenNoCategoriesExist_returnsEmptyList() {
        // GIVEN: 모든 카테고리 삭제
        deviceCategoryRepository.deleteAll();

        // WHEN
        List<DeviceCategoryResponse> response = deviceCategoryService.getDeviceCategories();

        // THEN
        assertThat(response).isNotNull();
        assertThat(response).isEmpty();
    }

    @Test
    @DisplayName("성공: 루트 카테고리가 하나도 없을 때 조회 시 빈 리스트를 반환한다")
    void getRootDeviceCategoryResponses_whenNoRootCategories_returnsEmptyList() {
        // GIVEN
        deviceCategoryRepository.deleteAll();

        // WHEN
        List<DeviceCategoryResponse> responses = deviceCategoryService.getRootDeviceCategoryResponses();

        // THEN
        assertThat(responses).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("성공: 자식이 없는 카테고리에서 자식 목록 조회 시 빈 리스트를 반환한다")
    void getChildDeviceCategories_forCategoryWithNoChildren_returnsEmptyList() {
        // GIVEN
        Long parentId = deviceCategoryService.create(new DeviceCategoryRequest("자식 없는 부모", null, null));

        // WHEN
        List<DeviceCategoryResponse> children =
                deviceCategoryService.getChildDeviceCategories(parentId);

        // THEN
        assertThat(children).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("성공: 존재하지 않는 카테고리의 자식 목록 조회 시 빈 리스트를 반환한다")
    void getChildDeviceCategories_withNonExistingParentId_throwsCustomException() {
        // GIVEN
        Long nonExistingParentId = 9999L;

        // WHEN & THEN
        List<DeviceCategoryResponse> childDeviceCategories =
                deviceCategoryService.getChildDeviceCategories(nonExistingParentId);

        assertThat(childDeviceCategories).isNotNull().isEmpty();
    }
}
