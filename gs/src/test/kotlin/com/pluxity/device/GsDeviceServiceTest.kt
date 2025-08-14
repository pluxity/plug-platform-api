package com.pluxity.device

import com.pluxity.GsApplication
import com.pluxity.device.dto.GsDeviceResponse
import com.pluxity.device.dto.GsDeviceUpdateRequest
import com.pluxity.device.dto.dummyCreateGsDeviceRequest
import com.pluxity.device.entity.DeviceCategory
import com.pluxity.device.repository.DeviceCategoryRepository
import com.pluxity.feature.dto.FeatureAssignDto
import com.pluxity.feature.entity.Feature
import com.pluxity.feature.repository.FeatureRepository
import com.pluxity.feature.service.FeatureService
import com.pluxity.global.exception.CustomException
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@SpringBootTest(classes = [GsApplication::class])
@Transactional
internal class GsDeviceServiceTest {
    @Autowired
    lateinit var gsDeviceService: GsDeviceService

    @Autowired
    lateinit var gsDeviceRepository: GsDeviceRepository

    @Autowired
    lateinit var featureRepository: FeatureRepository

    @Autowired
    lateinit var deviceCategoryRepository: DeviceCategoryRepository

    @Autowired
    lateinit var em: EntityManager

    @Autowired
    lateinit var featureService: FeatureService

    @Test
    @DisplayName("유효한 요청으로 GS 디바이스 생성 시 성공한다")
    fun save_WithValidRequest_SavesDevice() {
        // when
        val deviceId = gsDeviceService.save(dummyCreateGsDeviceRequest())

        // then
        Assertions.assertThat(deviceId).isNotNull()
        val foundDevice = gsDeviceRepository.findById(deviceId).orElseThrow()
        Assertions.assertThat(foundDevice).isNotNull()
        Assertions.assertThat(foundDevice.name).isEqualTo("Test Device")
    }

    @Test
    @DisplayName("ID로 GS 디바이스 조회 시 정확한 정보를 반환한다")
    fun findById_WithExistingId_ReturnsDeviceResponse() {
        // given
        val savedId = gsDeviceService.save(dummyCreateGsDeviceRequest())

        // when
        val response = gsDeviceService.findById(savedId)

        // then
        Assertions.assertThat(response).isNotNull()
        Assertions.assertThat(response.id).isEqualTo(savedId)
        Assertions.assertThat(response.name).isEqualTo("Test Device")
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 예외가 발생한다")
    fun findById_WithNonExistingId_ThrowsException() {
        // given
        val nonExistingId = "non-existent-id"

        // when & then
        assertThatThrownBy { gsDeviceService.findById(nonExistingId) }
            .isInstanceOf(CustomException::class.java)
    }

    @Test
    @DisplayName("전체 조회 시 모든 디바이스 목록을 반환한다")
    fun findAll_WhenDevicesExist_ReturnsResponseList() {
        // given
        gsDeviceService.save(dummyCreateGsDeviceRequest())

        // when
        val responses: List<GsDeviceResponse> = gsDeviceService.findAll()

        // then
        Assertions.assertThat(responses).hasSize(1)
        Assertions.assertThat(responses.first().name).isEqualTo("Test Device")
    }

    @Test
    @DisplayName("디바이스 정보 업데이트 시 내용이 반영된다")
    fun update_WithValidRequest_UpdatesDevice() {
        // given
        val savedId = gsDeviceService.save(dummyCreateGsDeviceRequest())

        // 업데이트에 사용할 새로운 Feature와 Category를 DB에 저장
        val updatedFeature =
            featureRepository.save(Feature.builder().id(UUID.randomUUID().toString()).build())
        val updatedCategory =
            deviceCategoryRepository.save(DeviceCategory.builder().name("Updated Category").build())

        val updateRequest = GsDeviceUpdateRequest("Updated Name", updatedCategory.id)

        // when
        featureService.assignDeviceToFeature(updatedFeature.id, FeatureAssignDto(savedId), false)
        gsDeviceService.update(
            savedId,
            updateRequest,
        )
        em.flush()
        em.clear()

        // then
        val updatedDevice = gsDeviceRepository.findById(savedId).orElseThrow()
        Assertions.assertThat(updatedDevice.name).isEqualTo("Updated Name")
        Assertions.assertThat(updatedDevice.feature.id).isEqualTo(updatedFeature.id)
        Assertions.assertThat(updatedDevice.category.id).isEqualTo(updatedCategory.id)
    }

    @Test
    @DisplayName("디바이스 삭제 시 Repository에서 제거된다")
    fun delete_WithExistingId_RemovesDevice() {
        // given
        val savedId = gsDeviceService.save(dummyCreateGsDeviceRequest())
        Assertions.assertThat(gsDeviceRepository.existsById(savedId)).isTrue()

        // when
        gsDeviceService.delete(savedId)
        em.flush()
        em.clear()

        // then
        Assertions.assertThat(gsDeviceRepository.existsById(savedId)).isFalse()
    }
}
