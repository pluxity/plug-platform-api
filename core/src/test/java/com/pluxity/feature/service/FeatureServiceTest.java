package com.pluxity.feature.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pluxity.asset.entity.Asset;
import com.pluxity.asset.repository.AssetRepository;
import com.pluxity.device.entity.Device;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.device.repository.DeviceCategoryRepository;
import com.pluxity.device.repository.DeviceRepository;
import com.pluxity.facility.Facility;
import com.pluxity.facility.FacilityRepository;
import com.pluxity.feature.dto.FeatureAssignDto;
import com.pluxity.feature.dto.FeatureCreateRequest;
import com.pluxity.feature.dto.FeatureResponse;
import com.pluxity.feature.dto.FeatureUpdateRequest;
import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.entity.Spatial;
import com.pluxity.feature.repository.FeatureRepository;
import com.pluxity.global.exception.CustomException;
import com.pluxity.station.Station;
import com.pluxity.util.TestFileUploader;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.util.List;
import java.util.UUID;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class FeatureServiceTest {

    @Autowired private FeatureService featureService;
    @Autowired private FeatureRepository featureRepository;
    @Autowired private AssetRepository assetRepository;
    @Autowired private FacilityRepository facilityRepository;
    @Autowired private DeviceRepository deviceRepository;
    @Autowired private DeviceCategoryRepository deviceCategoryRepository;
    @Autowired private TestFileUploader testFileUploader;

    private Asset testAsset;
    private Facility testFacility;
    private DeviceCategory testDeviceCategory;

    // Device가 추상 클래스이므로, 테스트용 구체 클래스를 정의
    @Entity
    @DiscriminatorValue("TEST")
    @NoArgsConstructor
    public static class DeviceInstance extends Device {
        public DeviceInstance(String id, DeviceCategory category) {
            super(id, category);
        }
        @Override
        public String getName() {
            return "Test Device Instance";
        }
    }

    @BeforeEach
    void setUp() {
        testAsset = createAndSaveAsset("테스트 에셋", "ASSET_01");
        testFacility = createAndSaveFacility("테스트 시설", "FAC_01");
        Long imageId = testFileUploader.initiateTestFileUpload("image");
        testDeviceCategory = deviceCategoryRepository.save(new DeviceCategory("테스트 카테고리", imageId));
    }


    @Test
    @DisplayName("성공: 모든 필드를 포함한 유효한 요청으로 피처를 생성하고, 모든 응답 필드와 DB 상태를 상세히 검증한다")
    void createFeature_WithValidRequest_SavesFeatureAndReturnsDetailedResponse() {
        // GIVEN
        String featureId = UUID.randomUUID().toString();
        FeatureCreateRequest request = new FeatureCreateRequest(
                featureId,
                new Spatial(10.0, 20.0, 30.0),
                new Spatial(0.0, 45.0, 0.0),
                new Spatial(1.5, 1.5, 1.5),
                testAsset.getId(),
                testFacility.getId(),
                "B1"
        );

        // WHEN
        FeatureResponse response = featureService.createFeature(request);

        // THEN: 응답 DTO 검증
        assertThat(response.id()).isEqualTo(featureId);
        assertThat(response.assetId()).isEqualTo(testAsset.getId());
        assertThat(response.floorId()).isEqualTo("B1");
        assertThat(response.position()).usingRecursiveComparison().isEqualTo(new Spatial(10.0, 20.0, 30.0));
        assertThat(response.rotation()).usingRecursiveComparison().isEqualTo(new Spatial(0.0, 45.0, 0.0));
        assertThat(response.scale()).usingRecursiveComparison().isEqualTo(new Spatial(1.5, 1.5, 1.5));

        // THEN: 데이터베이스 최종 상태 직접 검증
        Feature savedFeature = featureRepository.findById(featureId).orElseThrow();
        assertThat(savedFeature.getId()).isEqualTo(featureId);
        assertThat(savedFeature.getAssetId()).isEqualTo(testAsset.getId());
        assertThat(savedFeature.getFacility().getId()).isEqualTo(testFacility.getId());
        assertThat(savedFeature.getFloorId()).isEqualTo("B1");
        assertThat(savedFeature.getPosition().getX()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("성공: Spatial 정보가 null일 때 기본값(0,0,0 / 0,0,0 / 1,1,1)으로 피처가 생성된다")
    void createFeature_WithNullSpatials_CreatesWithDefaultValues() {
        // GIVEN
        String featureId = UUID.randomUUID().toString();
        FeatureCreateRequest request = new FeatureCreateRequest(
                featureId, null, null, null, testAsset.getId(), testFacility.getId(), "Lobby");

        // WHEN
        FeatureResponse response = featureService.createFeature(request);

        // THEN: 응답 DTO 및 DB 상태에서 기본값 검증
        assertThat(response.position()).usingRecursiveComparison().isEqualTo(new Spatial(0.0, 0.0, 0.0));
        assertThat(response.rotation()).usingRecursiveComparison().isEqualTo(new Spatial(0.0, 0.0, 0.0));
        assertThat(response.scale()).usingRecursiveComparison().isEqualTo(new Spatial(1.0, 1.0, 1.0));

        Feature savedFeature = featureRepository.findById(featureId).orElseThrow();
        assertThat(savedFeature.getPosition().getX()).isEqualTo(0.0);
        assertThat(savedFeature.getScale().getX()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("실패: 이미 존재하는 ID로 피처 생성 시 예외가 발생한다")
    void createFeature_WithDuplicateId_ThrowsCustomException() {
        // GIVEN: 기준 피처 생성
        String duplicateId = UUID.randomUUID().toString();
        featureService.createFeature(new FeatureCreateRequest(
                duplicateId, null, null, null, testAsset.getId(), testFacility.getId(), "F1"));

        // GIVEN: 중복된 ID를 가진 두 번째 요청
        FeatureCreateRequest duplicateRequest = new FeatureCreateRequest(
                duplicateId, null, null, null, testAsset.getId(), testFacility.getId(), "F2");

        // WHEN & THEN
        assertThrows(CustomException.class, () -> featureService.createFeature(duplicateRequest));
    }

    @Test
    @DisplayName("실패: 존재하지 않는 facilityId로 피처 생성 시 예외가 발생한다")
    void createFeature_WithInvalidFacilityId_ThrowsCustomException() {
        // GIVEN
        Long invalidFacilityId = 9999L;
        FeatureCreateRequest request = new FeatureCreateRequest(
                UUID.randomUUID().toString(), null, null, null, testAsset.getId(), invalidFacilityId, "F1");

        // WHEN & THEN
        assertThrows(CustomException.class, () -> featureService.createFeature(request));
    }

    @Test
    @DisplayName("실패: 존재하지 않는 assetId로 피처 생성 시 예외가 발생한다")
    void createFeature_WithInvalidAssetId_ThrowsCustomException() {
        // GIVEN
        Long invalidAssetId = 9999L;
        FeatureCreateRequest request = new FeatureCreateRequest(
                UUID.randomUUID().toString(), null, null, null, invalidAssetId, testFacility.getId(), "F1");

        // WHEN & THEN
        assertThrows(CustomException.class, () -> featureService.createFeature(request));
    }



    @Test
    @DisplayName("성공: facilityId로 피처 목록 조회 시 해당 시설의 피처 목록만 반환된다")
    void getFeatures_ByFacilityId_ReturnsListOfFeatures() {
        // GIVEN: 테스트 시설에 2개의 피처 생성
        createAndSaveFeature("F1", testFacility);
        createAndSaveFeature("F2", testFacility);

        // GIVEN: 다른 시설과 그 시설의 피처 생성
        Facility otherFacility = createAndSaveFacility("다른 시설", "FAC_02");
        createAndSaveFeature("F3", otherFacility);

        // WHEN
        List<FeatureResponse> responses = featureService.getFeatures(testFacility.getId());

        // THEN
        assertThat(responses).hasSize(2)
                .extracting(FeatureResponse::id)
                .containsExactlyInAnyOrder("F1", "F2");
    }

    @Test
    @DisplayName("성공: 피처가 없는 시설 조회 시 빈 목록을 반환한다")
    void getFeatures_WithNoFeatures_ReturnsEmptyList() {
        // GIVEN: 피처가 없는 상태
        // WHEN
        List<FeatureResponse> responses = featureService.getFeatures(testFacility.getId());
        // THEN
        assertThat(responses).isNotNull().isEmpty();
    }


    @Test
    @DisplayName("성공(PATCH): 유효한 요청으로 피처 정보를 부분 수정하고, 변경된 필드와 유지된 필드를 모두 검증한다")
    void updateFeature_PartialUpdate_UpdatesOnlyProvidedFields() {
        // GIVEN
        Feature originalFeature = createAndSaveFeature("F_UPDATE", testFacility);
        Spatial newPosition = new Spatial(100.0, 100.0, 100.0);
        FeatureUpdateRequest request = new FeatureUpdateRequest(newPosition, null, null);

        // WHEN
        FeatureResponse response = featureService.updateFeature(originalFeature.getId(), request);

        // THEN: 응답 DTO 검증
        assertThat(response.id()).isEqualTo(originalFeature.getId());
        assertThat(response.position()).isEqualTo(newPosition); // 변경됨
        assertThat(response.rotation()).isEqualTo(originalFeature.getRotation()); // 유지됨
        assertThat(response.scale()).isEqualTo(originalFeature.getScale()); // 유지됨

        // THEN: DB 직접 검증
        Feature updatedFeature = featureRepository.findById(originalFeature.getId()).orElseThrow();
        assertThat(updatedFeature.getPosition().getX()).isEqualTo(100.0);
        assertThat(updatedFeature.getRotation().getX()).isEqualTo(originalFeature.getRotation().getX());
    }

    @Test
    @DisplayName("실패: 존재하지 않는 ID로 피처 업데이트 시 예외가 발생한다")
    void updateFeature_WithNonExistingId_ThrowsException() {
        // GIVEN
        String nonExistingId = "non-existing-id";
        FeatureUpdateRequest request = new FeatureUpdateRequest(null, null, null);
        // WHEN & THEN
        assertThrows(CustomException.class, () -> featureService.updateFeature(nonExistingId, request));
    }



    @Test
    @DisplayName("성공: 존재하는 ID로 피처 삭제 시 DB에서 삭제된다")
    void deleteFeature_WithExistingId_DeletesFeature() {
        // GIVEN
        Feature featureToDelete = createAndSaveFeature("F_DELETE", testFacility);
        assertThat(featureRepository.findById(featureToDelete.getId())).isPresent();

        // WHEN
        featureService.deleteFeature(featureToDelete.getId());

        // THEN
        assertThat(featureRepository.findById(featureToDelete.getId())).isNotPresent();
    }

    @Test
    @DisplayName("실패: 존재하지 않는 ID로 피처 삭제 시 예외가 발생한다")
    void deleteFeature_WithNonExistingId_ThrowsException() {
        // GIVEN
        String nonExistingId = "non-existing-id";
        // WHEN & THEN
        assertThrows(CustomException.class, () -> featureService.deleteFeature(nonExistingId));
    }


    // --- Device Relation Test ---

    @Test
    @DisplayName("성공: 피처에 디바이스를 할당한다")
    void assignDeviceToFeature_AssignsDevice() {
        // GIVEN
        Feature feature = createAndSaveFeature("F_DEVICE", testFacility);
        Device device = createAndSaveDevice();
        assertThat(device.getFeature()).isNull();

        // WHEN
        featureService.assignDeviceToFeature(feature.getId(), new FeatureAssignDto(device.getId()), false);

        // THEN: DB 직접 검증
        Device updatedDevice = deviceRepository.findById(device.getId()).orElseThrow();
        assertThat(updatedDevice.getFeature()).isNotNull();
        assertThat(updatedDevice.getFeature().getId()).isEqualTo(feature.getId());
    }

    @Test
    @DisplayName("성공: 피처에서 디바이스 할당을 해제한다")
    void removeDeviceFromFeature_RemovesAssignment() {
        // GIVEN: 디바이스가 할당된 피처
        Feature feature = createAndSaveFeature("F_REMOVE_DEV", testFacility);
        Device device = createAndSaveDevice();
        featureService.assignDeviceToFeature(feature.getId(), new FeatureAssignDto(device.getId()), false);
        assertThat(deviceRepository.findById(device.getId()).orElseThrow().getFeature()).isNotNull();

        // WHEN
        featureService.removeDeviceFromFeature(feature.getId(), new FeatureAssignDto(device.getId()));

        // THEN: DB 직접 검증
        Device updatedDevice = deviceRepository.findById(device.getId()).orElseThrow();
        assertThat(updatedDevice.getFeature()).isNull();
    }

    @Test
    @DisplayName("실패: 할당되지 않은 디바이스 ID로 할당 해제를 시도하면 예외가 발생한다")
    void removeDeviceFromFeature_WithMismatchedId_ThrowsException() {
        // GIVEN
        Feature feature = createAndSaveFeature("F_MISMATCH", testFacility);
        Device assignedDevice = createAndSaveDevice();
        Device otherDevice = createAndSaveDevice();
        featureService.assignDeviceToFeature(feature.getId(), new FeatureAssignDto(assignedDevice.getId()), false);

        // WHEN & THEN: 다른 디바이스 ID로 해제 시도
        assertThrows(CustomException.class, () -> featureService.removeDeviceFromFeature(feature.getId(), new FeatureAssignDto(otherDevice.getId())));
    }


    private Asset createAndSaveAsset(String name, String code) {
        return assetRepository.save(Asset.builder().name(name).code(code).build());
    }

    private Facility createAndSaveFacility(String name, String code) {
        return facilityRepository.save(Station.builder().name(name).code(code).build());
    }

    private Feature createAndSaveFeature(String id, Facility facility) {
        Feature feature = Feature.builder()
                .id(id)
                .position(new Spatial(1.0, 2.0, 3.0))
                .rotation(new Spatial(0.0, 0.0, 0.0))
                .scale(new Spatial(1.0, 1.0, 1.0))
                .assetId(testAsset.getId())
                .facility(facility)
                .floorId("TEST_FLOOR")
                .build();
        return featureRepository.save(feature);
    }

    private Device createAndSaveDevice() {
        String deviceId = UUID.randomUUID().toString();
        DeviceInstance device = new DeviceInstance(deviceId, testDeviceCategory);
        return deviceRepository.save(device);
    }
}