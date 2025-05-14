package com.pluxity.device.service;

import com.pluxity.SasangApplication;
import com.pluxity.asset.entity.Asset;
import com.pluxity.asset.repository.AssetRepository;
import com.pluxity.domains.device.dto.DeviceCreateRequest;
import com.pluxity.domains.device.dto.DeviceResponse;
import com.pluxity.domains.device.dto.DeviceUpdateRequest;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.domains.device.repository.DefaultDeviceRepository;
import com.pluxity.domains.device.repository.DeviceCategoryRepository;
import com.pluxity.domains.device.service.DeviceService;
import com.pluxity.facility.entity.Station;
import com.pluxity.facility.repository.StationRepository;
import com.pluxity.feature.dto.FeatureCreateRequest;
import com.pluxity.feature.dto.FeatureUpdateRequest;
import com.pluxity.feature.entity.Spatial;
import com.pluxity.global.exception.CustomException;
import com.pluxity.icon.entity.Icon;
import com.pluxity.icon.repository.IconRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = SasangApplication.class)
@Transactional
class DeviceServiceTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    DeviceService deviceService;

    @Autowired
    DefaultDeviceRepository deviceRepository;

    @Autowired
    DeviceCategoryRepository deviceCategoryRepository;

    @Autowired
    StationRepository stationRepository;

    @Autowired
    AssetRepository assetRepository;
    
    @Autowired
    IconRepository iconRepository;

    private DeviceCategory category;
    private Station station;
    private Asset asset;
    private Icon icon;
    private DeviceCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 준비
        category = deviceCategoryRepository.save(DeviceCategory.builder()
                .name("테스트 카테고리")
                .build());

        station = stationRepository.save(Station.builder()
                .name("테스트 스테이션")
                .description("테스트용 스테이션입니다.")
                .build());

        asset = assetRepository.save(Asset.builder()
                .name("테스트 2D 에셋")
                .build());
                
        icon = iconRepository.save(Icon.builder()
                .name("테스트 아이콘")
                .build());

        // Feature 생성
        Spatial position = Spatial.builder().x(0.0).y(0.0).z(0.0).build();
        Spatial rotation = Spatial.builder().x(0.0).y(0.0).z(0.0).build();
        Spatial scale = Spatial.builder().x(1.0).y(1.0).z(1.0).build();

        FeatureCreateRequest featureRequest = FeatureCreateRequest.builder()
                .position(position)
                .rotation(rotation)
                .scale(scale)
                .build();

        createRequest = new DeviceCreateRequest(
                featureRequest,
                category.getId(),
                station.getId(),
                asset.getId(),
                icon.getId(),
                "테스트 디바이스",
                "TEST-001",
                "테스트용 디바이스입니다."
        );
    }

    @Test
    @DisplayName("유효한 요청으로 디바이스 생성 시 디바이스가 저장된다")
    void save_WithValidRequest_SavesDevice() {
        // when
        Long id = deviceService.save(createRequest);

        // then
        assertThat(id).isNotNull();

        // 저장된 디바이스 확인
        DeviceResponse savedDevice = deviceService.findById(id);
        assertThat(savedDevice).isNotNull();
        assertThat(savedDevice.name()).isEqualTo("테스트 디바이스");
        assertThat(savedDevice.code()).isEqualTo("TEST-001");
        assertThat(savedDevice.categoryId()).isEqualTo(category.getId());
        assertThat(savedDevice.facilityId()).isEqualTo(station.getId());
        assertThat(savedDevice.iconId()).isEqualTo(icon.getId());
    }

    @Test
    @DisplayName("모든 디바이스 조회 시 디바이스 목록이 반환된다")
    void findAll_ReturnsListOfDeviceResponses() {
        // given
        Long id = deviceService.save(createRequest);

        // when
        var responses = deviceService.findAll();

        // then
        assertThat(responses).isNotEmpty();
        assertThat(responses.get(0).name()).isEqualTo("테스트 디바이스");
        assertThat(responses.get(0).code()).isEqualTo("TEST-001");
    }

    @Test
    @DisplayName("ID로 디바이스 조회 시 디바이스 정보가 반환된다")
    void findById_WithExistingId_ReturnsDeviceResponse() {
        // given
        Long id = deviceService.save(createRequest);

        // when
        DeviceResponse response = deviceService.findById(id);

        // then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("테스트 디바이스");
        assertThat(response.code()).isEqualTo("TEST-001");
        assertThat(response.description()).isEqualTo("테스트용 디바이스입니다.");
        assertThat(response.iconId()).isEqualTo(icon.getId());
        assertThat(response.iconName()).isEqualTo(icon.getName());
    }

    @Test
    @DisplayName("존재하지 않는 ID로 디바이스 조회 시 예외가 발생한다")
    void findById_WithNonExistingId_ThrowsCustomException() {
        // given
        Long nonExistingId = 9999L;

        // when & then
        assertThrows(CustomException.class, () -> deviceService.findById(nonExistingId));
    }

    @Test
    @DisplayName("유효한 요청으로 디바이스 정보 수정 시 디바이스 정보가 업데이트된다")
    void update_WithValidRequest_UpdatesDevice() {
        // given
        Long id = deviceService.save(createRequest);
        
        // 새로운 아이콘 생성
        Icon newIcon = iconRepository.save(Icon.builder()
                .name("새 테스트 아이콘")
                .build());
        
        // Feature 업데이트 요청 생성
        Spatial newPosition = Spatial.builder().x(1.0).y(1.0).z(1.0).build();
        FeatureUpdateRequest featureUpdateRequest = FeatureUpdateRequest.builder()
                .position(newPosition)
                .build();
                
        DeviceUpdateRequest updateRequest = new DeviceUpdateRequest(
                featureUpdateRequest,
                null,
                null,
                null,
                newIcon.getId(),
                "수정된 디바이스",
                "TEST-002",
                "수정된 디바이스 설명입니다."
        );

        // when
        deviceService.update(id, updateRequest);

        // then
        DeviceResponse updatedDevice = deviceService.findById(id);
        assertThat(updatedDevice.name()).isEqualTo("수정된 디바이스");
        assertThat(updatedDevice.code()).isEqualTo("TEST-002");
        assertThat(updatedDevice.description()).isEqualTo("수정된 디바이스 설명입니다.");
        assertThat(updatedDevice.feature().position().getX()).isEqualTo(1.0);
        assertThat(updatedDevice.iconId()).isEqualTo(newIcon.getId());
        assertThat(updatedDevice.iconName()).isEqualTo(newIcon.getName());
    }

    @Test
    @DisplayName("디바이스 삭제 시 디바이스가 삭제된다")
    void delete_WithExistingId_DeletesDevice() {
        // given
        Long id = deviceService.save(createRequest);
        
        // when
        DeviceResponse response = deviceService.findById(id);
        assertThat(response).isNotNull();
        
        // then
        deviceService.delete(id);
        
        // 삭제 후에는 해당 ID로 디바이스를 찾을 수 없어야 함
        assertThrows(CustomException.class, () -> deviceService.findById(id));
    }
}