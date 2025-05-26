package com.pluxity.device.service;

import com.pluxity.SasangApplication;
import com.pluxity.asset.entity.Asset;
import com.pluxity.asset.repository.AssetRepository;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.device.repository.DeviceCategoryRepository;
import com.pluxity.domains.device.dto.SasangDeviceCreateRequest;
import com.pluxity.domains.device.dto.SasangDeviceResponse;
import com.pluxity.domains.device.dto.SasangDeviceUpdateRequest;
import com.pluxity.domains.device.repository.SasangDeviceRepository;
import com.pluxity.domains.device.service.SasangDeviceService;
import com.pluxity.facility.station.Station;
import com.pluxity.facility.station.StationRepository;
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
class SasangDeviceServiceTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    SasangDeviceService sasangDeviceService;

    @Autowired
    SasangDeviceRepository deviceRepository;

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
    private SasangDeviceCreateRequest createRequest;

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

        FeatureCreateRequest featureRequest = new FeatureCreateRequest(position, rotation, scale);

        createRequest = new SasangDeviceCreateRequest(
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
        Long id = sasangDeviceService.save(createRequest);

        // then
        assertThat(id).isNotNull();

        // 저장된 디바이스 확인
        SasangDeviceResponse savedDevice = sasangDeviceService.findDeviceById(id);
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
        Long id = sasangDeviceService.save(createRequest);

        // when
        var responses = sasangDeviceService.findAll();

        // then
        assertThat(responses).isNotEmpty();
        assertThat(responses.getFirst().name()).isEqualTo("테스트 디바이스");
        assertThat(responses.getFirst().code()).isEqualTo("TEST-001");
    }

    @Test
    @DisplayName("ID로 디바이스 조회 시 디바이스 정보가 반환된다")
    void findById_WithExistingId_2_ReturnsDeviceResponse() {
        // given
        Long id = sasangDeviceService.save(createRequest);

        // when
        SasangDeviceResponse response = sasangDeviceService.findDeviceById(id);

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
    void findById_WithNonExistingId_2_ThrowsCustomException() {
        // given
        Long nonExistingId = 9999L;

        // when & then
        assertThrows(CustomException.class, () -> sasangDeviceService.findDeviceById(nonExistingId));
    }

    @Test
    @DisplayName("유효한 요청으로 디바이스 정보 수정 시 디바이스 정보가 업데이트된다")
    void update_WithValidRequest_UpdatesDevice() {
        // given
        Long id = sasangDeviceService.save(createRequest);
        
        // 새로운 아이콘 생성
        Icon newIcon = iconRepository.save(Icon.builder()
                .name("새 테스트 아이콘")
                .build());
        
        // Feature 업데이트 요청 생성
        Spatial newPosition = Spatial.builder().x(1.0).y(1.0).z(1.0).build();
        FeatureUpdateRequest featureUpdateRequest = new FeatureUpdateRequest(newPosition, null, null);
                
        SasangDeviceUpdateRequest updateRequest = new SasangDeviceUpdateRequest(
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
        sasangDeviceService.update(id, updateRequest);

        // then
        SasangDeviceResponse updatedDevice = sasangDeviceService.findDeviceById(id);
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
        Long id = sasangDeviceService.save(createRequest);
        
        // when
        SasangDeviceResponse response = sasangDeviceService.findDeviceById(id);
        assertThat(response).isNotNull();
        
        // then
        sasangDeviceService.delete(id);
        
        // 삭제 후에는 해당 ID로 디바이스를 찾을 수 없어야 함
        assertThrows(CustomException.class, () -> sasangDeviceService.findDeviceById(id));
    }
    
    @Test
    @DisplayName("디바이스에 카테고리를 할당한다")
    void assignCategory_SetsDeviceCategory() {
        // given
        // 카테고리 없이 디바이스 생성
        SasangDeviceCreateRequest requestWithoutCategory = new SasangDeviceCreateRequest(
                createRequest.feature(),
                null, // 카테고리 없음
                createRequest.stationId(),
                createRequest.asset(),
                createRequest.iconId(),
                createRequest.name(),
                createRequest.code(),
                createRequest.description()
        );
        
        Long deviceId = sasangDeviceService.save(requestWithoutCategory);
        SasangDeviceResponse deviceBeforeAssign = sasangDeviceService.findDeviceById(deviceId);
        assertThat(deviceBeforeAssign.categoryId()).isNull();
        
        // 새 카테고리 생성
        DeviceCategory newCategory = deviceCategoryRepository.save(DeviceCategory.builder()
                .name("새 테스트 카테고리")
                .build());
        
        // when
        SasangDeviceResponse updatedDevice = sasangDeviceService.assignCategory(deviceId, newCategory.getId());
        
        // then
        assertThat(updatedDevice.categoryId()).isEqualTo(newCategory.getId());
        assertThat(updatedDevice.categoryName()).isEqualTo(newCategory.getName());
        
        // 데이터베이스에서 확인
        SasangDeviceResponse fetchedDevice = sasangDeviceService.findDeviceById(deviceId);
        assertThat(fetchedDevice.categoryId()).isEqualTo(newCategory.getId());
        assertThat(fetchedDevice.categoryName()).isEqualTo(newCategory.getName());
    }
    
    @Test
    @DisplayName("디바이스에서 카테고리를 제거한다")
    void removeCategory_RemovesDeviceCategory() {
        // given
        // 카테고리가 있는 디바이스 생성
        Long deviceId = sasangDeviceService.save(createRequest);
        SasangDeviceResponse deviceBeforeRemove = sasangDeviceService.findDeviceById(deviceId);
        assertThat(deviceBeforeRemove.categoryId()).isEqualTo(category.getId());
        
        // when
        SasangDeviceResponse updatedDevice = sasangDeviceService.removeCategory(deviceId);
        
        // then
        assertThat(updatedDevice.categoryId()).isNull();
        assertThat(updatedDevice.categoryName()).isNull();
        
        // 데이터베이스에서 확인
        SasangDeviceResponse fetchedDevice = sasangDeviceService.findDeviceById(deviceId);
        assertThat(fetchedDevice.categoryId()).isNull();
        assertThat(fetchedDevice.categoryName()).isNull();
    }
    
    @Test
    @DisplayName("존재하지 않는 카테고리 할당 시 예외가 발생한다")
    void assignCategory_WithNonExistingCategoryId_ThrowsCustomException() {
        // given
        Long deviceId = sasangDeviceService.save(createRequest);
        Long nonExistingCategoryId = 9999L;
        
        // when & then
        assertThrows(CustomException.class, () -> 
            sasangDeviceService.assignCategory(deviceId, nonExistingCategoryId)
        );
    }
    
    @Test
    @DisplayName("존재하지 않는 디바이스에 카테고리 할당 시 예외가 발생한다")
    void assignCategory_WithNonExistingDeviceId_ThrowsCustomException() {
        // given
        Long nonExistingDeviceId = 9999L;
        
        // when & then
        assertThrows(CustomException.class, () -> 
            sasangDeviceService.assignCategory(nonExistingDeviceId, category.getId())
        );
    }
}