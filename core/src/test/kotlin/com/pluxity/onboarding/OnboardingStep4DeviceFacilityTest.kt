package com.pluxity.onboarding

import com.pluxity.building.BuildingService
import com.pluxity.building.dto.BuildingCreateRequest
import com.pluxity.device.dto.DeviceCategoryRequest
import com.pluxity.device.dto.DeviceCreateRequest
import com.pluxity.device.entity.DeviceCompanyType
import com.pluxity.device.entity.DeviceType
import com.pluxity.device.repository.DeviceRepository
import com.pluxity.device.service.DeviceCategoryService
import com.pluxity.device.service.DeviceService
import com.pluxity.facility.dto.FacilityCreateRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

/**
 * 온보딩 4단계: 장비 및 시설 도메인 테스트
 *
 * 과제 1: Building 및 Device 생성
 * 과제 2: Device 카테고리 변경 시 양방향 관계 검증
 *
 */

@SpringBootTest
@Transactional
class OnboardingStep4DeviceFacilityTest @Autowired constructor(
    private val deviceService: DeviceService,
    private val deviceRepository: DeviceRepository,
    private val buildingService: BuildingService,
    private val deviceCategoryService: DeviceCategoryService,
) {
    private var testBuildingId: Long = 0
    private var categoryAId: Long = 0
    private var categoryBId: Long = 0

    @BeforeEach
    fun setUp() {
        testBuildingId = buildingService.save(
            BuildingCreateRequest(
                FacilityCreateRequest(
                    name = "테스트 건물",
                    code = "TEST-CODE",
                    description = "온보딩 테스트용 건물",
                    drawingFileId = null,
                    thumbnailFileId = null,
                    lon = null,
                    lat = null,
                    locationMeta = null
                ),
                emptyList()
            )
        )

        categoryAId = deviceCategoryService.create(
            DeviceCategoryRequest(
                name = "카테고리A",
                parentId = null,
                thumbnailFileId = null
            )
        )

        categoryBId = deviceCategoryService.create(
            DeviceCategoryRequest(
                name = "카테고리B",
                parentId = null,
                thumbnailFileId = null
            )
        )
    }

    @Test
    @DisplayName("과제 1: Building 생성 및 저장 성공")
    fun createBuilding_success() {
        // then
        val building = buildingService.findById(testBuildingId)

        assertThat(building.facility.id).isEqualTo(testBuildingId)
        assertThat(building.facility.name).isEqualTo("테스트 건물")
        assertThat(building.facility.code).isEqualTo("TEST-CODE")
        assertThat(building.facility.description).isEqualTo("온보딩 테스트용 건물")
    }

    @Test
    @DisplayName("과제 1: Device 생성 및 DeviceCategory 할당 성공")
    fun createDevice_withCategory_success() {
        // given
        val deviceId = "DEVICE-01"
        val request = DeviceCreateRequest(
            id = deviceId,
            name = "TEST-DEVICE",
            categoryId = categoryAId,
            companyType = DeviceCompanyType.DAWONDNS,
            deviceType = DeviceType.TEMP_HUM
        )

        // when
        val savedDeviceId = deviceService.save(request)

        // then
        val device = deviceRepository.findByIdOrNullCustom(deviceId)!!

        val categoryA = deviceCategoryService.findById(categoryAId)

        assertThat(categoryA.devices).anyMatch { it.id == deviceId }
        assertThat(savedDeviceId).isEqualTo(deviceId)
        assertThat(device.name).isEqualTo("TEST-DEVICE")
        assertThat(device.category).isNotNull
        assertThat(device.category?.id).isEqualTo(categoryAId)
        assertThat(device.category?.name).isEqualTo("카테고리A")
    }

    @Test
    @DisplayName("과제 2: Device 카테고리 변경 시 양방향 관계 정리 검증")
    fun changeDeviceCategory_verifyBidirectionalRelations() {
        // given
        val deviceId = deviceService.save(
            DeviceCreateRequest(
                id = "테스트 ID",
                name = "테스트 장비",
                categoryId = categoryAId,
                companyType = DeviceCompanyType.DAWONDNS,
                deviceType = DeviceType.TEMP_HUM
            )
        )

        // flush로 DB 반영
        deviceRepository.flush()

        // 초기 상태 확인
        val device = deviceRepository.findById(deviceId).orElse(null)!!
        val oldCategory = device.category!!
        val categoryB = deviceCategoryService.findById(categoryBId)

        assertThat(oldCategory.devices).hasSize(1)
        assertThat(oldCategory.devices.first().id).isEqualTo(deviceId)
        assertThat(categoryB.devices).isEmpty()

        // when
        device.changeCategory(categoryB)

        deviceRepository.flush()

        // then - 같은 인스턴스로 검증
        assertThat(oldCategory.devices)
            .describedAs("CategoryA의 장비 목록에 Device가 없어야 함")
            .noneMatch { it.id == deviceId }

        assertThat(categoryB.devices)
            .describedAs("CategoryB의 장비 목록에 Device가 있어야 함")
            .anyMatch { it.id == deviceId }

        assertThat(device.category?.id).isEqualTo(categoryBId)
    }

    @Test
    @DisplayName("과제 2: Device 카테고리를 null로 변경 시 이전 카테고리에서 제거됨")
    fun changeDeviceCategory_toNull_removesFromPreviousCategory() {
        // given
        val deviceId = "DEVICE-NULL-TEST"
        deviceService.save(
            DeviceCreateRequest(
                id = deviceId,
                name = "카테고리 제거 테스트 장비",
                categoryId = categoryAId,
                companyType = DeviceCompanyType.DAWONDNS,
                deviceType = DeviceType.TEMP_HUM
            )
        )

        val categoryA = deviceCategoryService.findById(categoryAId)
        assertThat(categoryA.devices).anyMatch { it.id == deviceId }


        // when
        val device = deviceRepository.findByIdOrNullCustom(deviceId)!!
        device.changeCategory(null)
        deviceRepository.flush()

        // then
        val updatedCategoryA = deviceCategoryService.findById(categoryAId)

        assertThat(updatedCategoryA.devices)
            .describedAs("CategoryA에서 Device가 제거되어야 함")
            .noneMatch { it.id == deviceId }

        assertThat(device.category).isNull()

    }
}