package com.pluxity.feature.service

import com.pluxity.asset.entity.Asset
import com.pluxity.asset.repository.AssetRepository
import com.pluxity.device.entity.Device
import com.pluxity.device.entity.DeviceCategory
import com.pluxity.device.entity.DeviceCompanyType
import com.pluxity.device.entity.DeviceType
import com.pluxity.device.repository.DeviceCategoryRepository
import com.pluxity.device.repository.DeviceRepository
import com.pluxity.facility.Facility
import com.pluxity.facility.FacilityService
import com.pluxity.facility.dto.FacilityCreateRequest
import com.pluxity.feature.dto.FeatureAssignDto
import com.pluxity.feature.dto.FeatureCreateRequest
import com.pluxity.feature.dto.FeatureResponse
import com.pluxity.feature.dto.FeatureUpdateRequest
import com.pluxity.feature.entity.Feature
import com.pluxity.feature.entity.Spatial
import com.pluxity.feature.repository.FeatureRepository
import com.pluxity.global.exception.CustomException
import com.pluxity.station.Station
import com.pluxity.util.TestFileUploader
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@SpringBootTest
@Transactional
internal class FeatureServiceTest
    @Autowired
    constructor(
        private val featureService: FeatureService,
        private val featureRepository: FeatureRepository,
        private val assetRepository: AssetRepository,
        private val facilityService: FacilityService,
        private val deviceRepository: DeviceRepository,
        private val deviceCategoryRepository: DeviceCategoryRepository,
        private val testFileUploader: TestFileUploader,
    ) {
        lateinit var testAsset: Asset
        lateinit var testFacility: Facility
        lateinit var testDeviceCategory: DeviceCategory

        @MockitoBean
        lateinit var featureAssignment: FeatureAssignment

        @BeforeEach
        fun setUp() {
            testAsset = createAndSaveAsset("테스트 에셋", "ASSET_01")
            testFacility = createAndSaveFacility("테스트 시설", "FAC_01")
            val imageId = testFileUploader.initiateTestFileUpload("image")
            testDeviceCategory = deviceCategoryRepository.save(DeviceCategory(iconFileId = imageId))
        }

        @Test
        @DisplayName("성공: 모든 필드를 포함한 유효한 요청으로 피처를 생성하고, 모든 응답 필드와 DB 상태를 상세히 검증한다")
        fun createFeature_WithValidRequest_SavesFeatureAndReturnsDetailedResponse() {
            // GIVEN
            val featureId = UUID.randomUUID().toString()
            val request =
                FeatureCreateRequest(
                    featureId,
                    Spatial(10.0, 20.0, 30.0),
                    Spatial(0.0, 45.0, 0.0),
                    Spatial(1.5, 1.5, 1.5),
                    testAsset.id!!,
                    testFacility.id!!,
                    "B1",
                )

            // WHEN
            val response = featureService.createFeature(request)

            // THEN: 응답 DTO 검증
            Assertions.assertThat(response.id).isEqualTo(featureId)
            Assertions.assertThat(response.assetId).isEqualTo(testAsset.id)
            Assertions.assertThat(response.floorId).isEqualTo("B1")
            Assertions
                .assertThat(response.position)
                .usingRecursiveComparison()
                .isEqualTo(Spatial(10.0, 20.0, 30.0))
            Assertions
                .assertThat(response.rotation)
                .usingRecursiveComparison()
                .isEqualTo(Spatial(0.0, 45.0, 0.0))
            Assertions.assertThat(response.scale).usingRecursiveComparison().isEqualTo(Spatial(1.5, 1.5, 1.5))

            // THEN: 데이터베이스 최종 상태 직접 검증
            val savedFeature = featureRepository.findById(featureId).orElseThrow()
            Assertions.assertThat(savedFeature.id).isEqualTo(featureId)
            Assertions.assertThat(savedFeature.assetId).isEqualTo(testAsset.id)
            Assertions.assertThat(savedFeature.facility.id).isEqualTo(testFacility.id)
            Assertions.assertThat(savedFeature.floorId).isEqualTo("B1")
            Assertions.assertThat(savedFeature.position?.x).isEqualTo(10.0)
        }

        @Test
        @DisplayName("성공: Spatial 정보가 null일 때 기본값(0,0,0 / 0,0,0 / 1,1,1)으로 피처가 생성된다")
        fun createFeature_WithNullSpatials_CreatesWithDefaultValues() {
            // GIVEN
            val featureId = UUID.randomUUID().toString()
            val request =
                FeatureCreateRequest(
                    featureId,
                    null,
                    null,
                    null,
                    testAsset.id!!,
                    testFacility.id!!,
                    "Lobby",
                )

            // WHEN
            val response = featureService.createFeature(request)

            // THEN: 응답 DTO 및 DB 상태에서 기본값 검증
            Assertions
                .assertThat(response.position)
                .usingRecursiveComparison()
                .isEqualTo(Spatial(0.0, 0.0, 0.0))
            Assertions
                .assertThat(response.rotation)
                .usingRecursiveComparison()
                .isEqualTo(Spatial(0.0, 0.0, 0.0))
            Assertions.assertThat(response.scale).usingRecursiveComparison().isEqualTo(Spatial(1.0, 1.0, 1.0))

            val savedFeature = featureRepository.findById(featureId).orElseThrow()
            Assertions.assertThat(savedFeature.position?.x).isEqualTo(0.0)
            Assertions.assertThat(savedFeature.scale?.x).isEqualTo(1.0)
        }

        @Test
        @DisplayName("실패: 이미 존재하는 ID로 피처 생성 시 예외가 발생한다")
        fun createFeature_WithDuplicateId_ThrowsCustomException() {
            // GIVEN: 기준 피처 생성
            val duplicateId = UUID.randomUUID().toString()
            featureService.createFeature(
                FeatureCreateRequest(
                    duplicateId,
                    null,
                    null,
                    null,
                    testAsset.id!!,
                    testFacility.id!!,
                    "F1",
                ),
            )

            // GIVEN: 중복된 ID를 가진 두 번째 요청
            val duplicateRequest =
                FeatureCreateRequest(
                    duplicateId,
                    null,
                    null,
                    null,
                    testAsset.id!!,
                    testFacility.id!!,
                    "F2",
                )

            // WHEN & THEN
            assertThrows<CustomException> {
                featureService.createFeature(duplicateRequest)
            }
        }

        @Test
        @DisplayName("실패: 존재하지 않는 facilityId로 피처 생성 시 예외가 발생한다")
        fun createFeature_WithInvalidFacilityId_ThrowsCustomException() {
            // GIVEN
            val invalidFacilityId = 9999L
            val request =
                FeatureCreateRequest(
                    UUID.randomUUID().toString(),
                    null,
                    null,
                    null,
                    testAsset.id!!,
                    invalidFacilityId,
                    "F1",
                )

            // WHEN & THEN
            assertThrows<CustomException> {
                featureService.createFeature(request)
            }
        }

        @Test
        @DisplayName("실패: 존재하지 않는 assetId로 피처 생성 시 예외가 발생한다")
        fun createFeature_WithInvalidAssetId_ThrowsCustomException() {
            // GIVEN
            val invalidAssetId = 9999L
            val request =
                FeatureCreateRequest(
                    UUID.randomUUID().toString(),
                    null,
                    null,
                    null,
                    invalidAssetId,
                    testFacility.id!!,
                    "F1",
                )

            // WHEN & THEN
            assertThrows<CustomException> {
                featureService.createFeature(request)
            }
        }

        @Test
        @DisplayName("성공: facilityId로 피처 목록 조회 시 해당 시설의 피처 목록만 반환된다")
        fun getFeatures_ByFacilityId_ReturnsListOfFeatures() {
            // GIVEN: 테스트 시설에 2개의 피처 생성
            createAndSaveFeature("F1", testFacility)
            createAndSaveFeature("F2", testFacility)

            // GIVEN: 다른 시설과 그 시설의 피처 생성
            val otherFacility = createAndSaveFacility("다른 시설", "FAC_02")
            createAndSaveFeature("F3", otherFacility)

            // WHEN
            val responses = featureService.getFeatures(testFacility.id!!)

            // THEN
            Assertions
                .assertThat(responses)
                .hasSize(2)
                .extracting<String, RuntimeException>(FeatureResponse::id)
                .containsExactlyInAnyOrder("F1", "F2")
        }

        @Test
        @DisplayName("성공: 피처가 없는 시설 조회 시 빈 목록을 반환한다")
        fun getFeatures_WithNoFeatures_ReturnsEmptyList() {
            // GIVEN: 피처가 없는 상태
            // WHEN
            val responses = featureService.getFeatures(testFacility.id!!)
            // THEN
            Assertions.assertThat(responses).isNotNull().isEmpty()
        }

        @Test
        @DisplayName("성공(PATCH): 유효한 요청으로 피처 정보를 부분 수정하고, 변경된 필드와 유지된 필드를 모두 검증한다")
        fun updateFeature_PartialUpdate_UpdatesOnlyProvidedFields() {
            // GIVEN
            val originalFeature = createAndSaveFeature("F_UPDATE", testFacility)
            val newPosition = Spatial(100.0, 100.0, 100.0)
            val request = FeatureUpdateRequest(newPosition, null, null)

            // WHEN
            val response = featureService.updateFeature(originalFeature.id!!, request)

            // THEN: 응답 DTO 검증
            Assertions.assertThat(response.id).isEqualTo(originalFeature.id)
            Assertions.assertThat(response.position).isEqualTo(newPosition) // 변경됨
            Assertions.assertThat(response.rotation).isEqualTo(originalFeature.rotation) // 유지됨
            Assertions.assertThat(response.scale).isEqualTo(originalFeature.scale) // 유지됨

            // THEN: DB 직접 검증
            val updatedFeature = featureRepository.findById(originalFeature.id!!).orElseThrow()
            Assertions.assertThat(updatedFeature.position?.x).isEqualTo(100.0)
            Assertions.assertThat(updatedFeature.rotation?.x).isEqualTo(originalFeature.rotation?.x)
        }

        @Test
        @DisplayName("실패: 존재하지 않는 ID로 피처 업데이트 시 예외가 발생한다")
        fun updateFeature_WithNonExistingId_ThrowsException() {
            // GIVEN
            val nonExistingId = "non-existing-id"
            val request = FeatureUpdateRequest(null, null, null)
            // WHEN & THEN
            assertThrows<CustomException> {
                featureService.updateFeature(nonExistingId, request)
            }
        }

        @Test
        @DisplayName("성공: 존재하는 ID로 피처 삭제 시 DB에서 삭제된다")
        fun deleteFeature_WithExistingId_DeletesFeature() {
            // GIVEN
            val featureToDelete = createAndSaveFeature("F_DELETE", testFacility)
            Assertions.assertThat(featureRepository.findById(featureToDelete.id!!)).isPresent()

            // WHEN
            featureService.deleteFeature(featureToDelete.id!!)

            // THEN
            Assertions.assertThat(featureRepository.findById(featureToDelete.id!!)).isNotPresent()
        }

        @Test
        @DisplayName("실패: 존재하지 않는 ID로 피처 삭제 시 예외가 발생한다")
        fun deleteFeature_WithNonExistingId_ThrowsException() {
            // GIVEN
            val nonExistingId = "non-existing-id"
            // WHEN & THEN
            assertThrows<CustomException> {
                featureService.deleteFeature(nonExistingId)
            }
        }

        // --- Device Relation Test ---
        @Test
        @DisplayName("성공: 피처에 디바이스를 할당한다")
        fun assignDeviceToFeature_AssignsDevice() {
            // GIVEN
            val feature = createAndSaveFeature("F_DEVICE", testFacility)
            val device = createAndSaveDevice()
            Assertions.assertThat(device.feature).isNull()

            // WHEN
            featureService.assignSomethingToFeature(
                feature.id!!,
                FeatureAssignDto(device.id, FeatureAssignType.DEVICE),
                false,
            )

            // THEN: DB 직접 검증
            val updatedDevice = deviceRepository.findById(device.id).orElseThrow()
            Assertions.assertThat(updatedDevice.feature).isNotNull()
            Assertions.assertThat(updatedDevice.feature?.id).isEqualTo(feature.id)
        }

        @Test
        @DisplayName("성공: 피처에서 디바이스 할당을 해제한다")
        fun removeDeviceFromFeature_RemovesAssignment() {
            // GIVEN: 디바이스가 할당된 피처
            val feature = createAndSaveFeature("F_REMOVE_DEV", testFacility)
            val device = createAndSaveDevice()
            featureService.assignSomethingToFeature(
                feature.id!!,
                FeatureAssignDto(device.id, FeatureAssignType.DEVICE),
                false,
            )
            Assertions
                .assertThat(deviceRepository.findById(device.id).orElseThrow().feature)
                .isNotNull()

            // WHEN
            featureService.removeSomethingFromFeature(feature.id!!, FeatureAssignDto(device.id, FeatureAssignType.DEVICE))

            // THEN: DB 직접 검증
            val updatedDevice = deviceRepository.findById(device.id).orElseThrow()
            Assertions.assertThat(updatedDevice.feature).isNull()
        }

        @Test
        @DisplayName("실패: 할당되지 않은 디바이스 ID로 할당 해제를 시도하면 예외가 발생한다")
        fun removeDeviceFromFeature_WithMismatchedId_ThrowsException() {
            // GIVEN
            val feature = createAndSaveFeature("F_MISMATCH", testFacility)
            val assignedDevice = createAndSaveDevice()
            val otherDevice = createAndSaveDevice()
            featureService.assignSomethingToFeature(
                feature.id!!,
                FeatureAssignDto(assignedDevice.id, FeatureAssignType.DEVICE),
                false,
            )

            // WHEN & THEN: 다른 디바이스 ID로 해제 시도
            assertThrows<CustomException> {
                featureService.removeSomethingFromFeature(
                    feature.id!!,
                    FeatureAssignDto(otherDevice.id, FeatureAssignType.DEVICE),
                )
            }
        }

        private fun createAndSaveAsset(
            name: String,
            code: String,
        ): Asset =
            assetRepository.save(
                Asset(name = name, code = code),
            )

        private fun createAndSaveFacility(
            name: String,
            code: String,
        ): Facility =
            facilityService.save(
                Station(name, null),
                FacilityCreateRequest(name, code, null, null, null, null, null, null),
            )

        private fun createAndSaveFeature(
            id: String,
            facility: Facility,
        ): Feature {
            val feature =
                Feature(
                    id = id,
                    position = Spatial(1.0, 2.0, 3.0),
                    rotation = Spatial(0.0, 0.0, 0.0),
                    scale = Spatial(1.0, 1.0, 1.0),
                    assetId = testAsset.id!!,
                    facility = facility,
                    floorId = "TEST_FLOOR",
                )
            return featureRepository.save(feature)
        }

        private fun createAndSaveDevice(): Device {
            val deviceId = UUID.randomUUID().toString()
            val device =
                Device(
                    id = deviceId,
                    name = "Test Device",
                    category = testDeviceCategory,
                    deviceType = DeviceType.TEMP_HUM,
                    companyType = DeviceCompanyType.DAWONDNS,
                )
            return deviceRepository.save(device)
        }
    }
