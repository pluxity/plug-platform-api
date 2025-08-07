package com.pluxity.device;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pluxity.GsApplication;
import com.pluxity.device.dto.GsDeviceCreateRequest;
import com.pluxity.device.dto.GsDeviceResponse;
import com.pluxity.device.dto.GsDeviceUpdateRequest;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.device.repository.DeviceCategoryRepository;
import com.pluxity.feature.dto.FeatureAssignDto;
import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.repository.FeatureRepository;
import com.pluxity.feature.service.FeatureService;
import com.pluxity.global.exception.CustomException;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = GsApplication.class)
@Transactional
class GsDeviceServiceTest {

    @Autowired
    private GsDeviceService gsDeviceService;
    @Autowired
    private GsDeviceRepository gsDeviceRepository;
    @Autowired
    private FeatureRepository featureRepository;
    @Autowired
    private DeviceCategoryRepository deviceCategoryRepository;
    @Autowired
    private EntityManager em;

    private GsDeviceCreateRequest createRequest;
    @Autowired
    private FeatureService featureService;

    @BeforeEach
    void setUp() {
        createRequest = new GsDeviceCreateRequest(UUID.randomUUID().toString(), "Test Device",  null);
    }

    @Test
    @DisplayName("유효한 요청으로 GS 디바이스 생성 시 성공한다")
    void save_WithValidRequest_SavesDevice() {
        // when
        String deviceId = gsDeviceService.save(createRequest);

        // then
        assertThat(deviceId).isNotNull();
        GsDevice foundDevice = gsDeviceRepository.findById(deviceId).orElseThrow();

        assertThat(foundDevice.getName()).isEqualTo("Test Device");
    }

    @Test
    @DisplayName("ID로 GS 디바이스 조회 시 정확한 정보를 반환한다")
    void findById_WithExistingId_ReturnsDeviceResponse() {
        // given
        String savedId = gsDeviceService.save(createRequest);

        // when
        GsDeviceResponse response = gsDeviceService.findById(savedId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(savedId);
        assertThat(response.name()).isEqualTo("Test Device");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 조회 시 예외가 발생한다")
    void findById_WithNonExistingId_ThrowsException() {
        // given
        String nonExistingId = "non-existent-id";

        // when & then
        assertThrows(CustomException.class, () -> gsDeviceService.findById(nonExistingId));
    }

    @Test
    @DisplayName("전체 조회 시 모든 디바이스 목록을 반환한다")
    void findAll_WhenDevicesExist_ReturnsResponseList() {
        // given
        gsDeviceService.save(createRequest);

        // when
        List<GsDeviceResponse> responses = gsDeviceService.findAll();

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().name()).isEqualTo("Test Device");
    }

    @Test
    @DisplayName("디바이스 정보 업데이트 시 내용이 반영된다")
    void update_WithValidRequest_UpdatesDevice() {
        // given
        String savedId = gsDeviceService.save(createRequest);

        // 업데이트에 사용할 새로운 Feature와 Category를 DB에 저장
        Feature updatedFeature = featureRepository.save(Feature.builder().id(UUID.randomUUID().toString()).build());
        DeviceCategory updatedCategory = deviceCategoryRepository.save(DeviceCategory.builder().name("Updated Category").build());

        GsDeviceUpdateRequest updateRequest = new GsDeviceUpdateRequest("Updated Name", updatedCategory.getId());

        // when
        featureService.assignDeviceToFeature(updatedFeature.getId(), new FeatureAssignDto(savedId));
        assertDoesNotThrow(() -> gsDeviceService.update(savedId, updateRequest));
        em.flush();
        em.clear();

        // then
        GsDevice updatedDevice = gsDeviceRepository.findById(savedId).orElseThrow();
        assertThat(updatedDevice.getName()).isEqualTo("Updated Name");
        assertThat(updatedDevice.getFeature().getId()).isEqualTo(updatedFeature.getId());
        assertThat(updatedDevice.getCategory().getId()).isEqualTo(updatedCategory.getId());
    }

    @Test
    @DisplayName("디바이스 삭제 시 Repository에서 제거된다")
    void delete_WithExistingId_RemovesDevice() {
        // given
        String savedId = gsDeviceService.save(createRequest);
        assertThat(gsDeviceRepository.existsById(savedId)).isTrue();

        // when
        assertDoesNotThrow(() -> gsDeviceService.delete(savedId));
        em.flush();
        em.clear();

        // then
        assertThat(gsDeviceRepository.existsById(savedId)).isFalse();
    }
}