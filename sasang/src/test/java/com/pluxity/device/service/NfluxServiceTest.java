package com.pluxity.device.service;

import com.pluxity.SasangApplication;
import com.pluxity.asset.entity.Asset;
import com.pluxity.asset.repository.AssetRepository;
import com.pluxity.device.entity.DeviceCategory;
import com.pluxity.device.repository.DeviceCategoryRepository;
import com.pluxity.domains.device.dto.NfluxCategoryGroupResponse;
import com.pluxity.domains.device.dto.NfluxCreateRequest;
import com.pluxity.domains.device.dto.NfluxResponse;
import com.pluxity.domains.device.dto.NfluxUpdateRequest;
import com.pluxity.domains.device.entity.Nflux;
import com.pluxity.domains.device.entity.NfluxCategory;
import com.pluxity.domains.device.repository.NfluxRepository;
import com.pluxity.domains.device.service.NfluxService;
import com.pluxity.facility.station.Station;
import com.pluxity.facility.station.StationRepository;
import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.entity.Spatial;
import com.pluxity.feature.repository.FeatureRepository;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = SasangApplication.class)
@Transactional
class NfluxServiceTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    NfluxService nfluxService;

    @Autowired
    NfluxRepository deviceRepository;

    @Autowired
    DeviceCategoryRepository deviceCategoryRepository;
    

    @Autowired
    AssetRepository assetRepository;

    @Autowired
    FeatureRepository featureRepository;
    
    @Autowired
    StationRepository stationRepository;

    @Autowired
    FileService fileService;

    private DeviceCategory category;
    private Asset asset;
    private Feature feature;
    private NfluxCreateRequest createRequest;
    private byte[] fileContent;

    @BeforeEach
    void setUp() {
        fileContent = "가짜 파일 내용".getBytes();
        
        // 카테고리 생성
        category = deviceCategoryRepository.save(DeviceCategory.builder()
                .name("테스트 카테고리")
                .build());
        
        // 에셋 생성
        asset = assetRepository.save(Asset.builder()
                        .code("TEST-ASSET")
                        .name("테스트 에셋")
                .build());
        
        // 피처 생성
        String uniqueFeatureId = UUID.randomUUID().toString();
        feature = featureRepository.save(Feature.builder()
                .id(uniqueFeatureId) // ID 명시적 설정
                .position(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .asset(asset)
                .build());
        
        // createRequest는 더 이상 생성하지 않음
    }

    private String generateUniqueId(String prefix) {
        String uniqueId = "TEST-" + UUID.randomUUID().toString().substring(0, 8);
        return prefix + "-" + uniqueId;
    }

    // 테스트용 파일 ID 생성 헬퍼 메서드
    private Long createFileId(String fileName) {
        try {
            MultipartFile file = new MockMultipartFile(
                    fileName, fileName, "image/png", fileContent);
            Long fileId = fileService.initiateUpload(file);
            // 파일 영구 저장 처리
            fileService.finalizeUpload(fileId, "test/" + fileName);
            return fileId;
        } catch (Exception e) {
            throw new RuntimeException("파일 생성 실패: " + e.getMessage(), e);
        }
    }

    @Test
    @DisplayName("디바이스 저장 테스트")
    void saveDeviceTest() {
        // given & when
        NfluxCreateRequest testRequest = new NfluxCreateRequest(
                generateUniqueId("SAVE"),
                category.getId(),
                asset.getId(),
                "저장 테스트 디바이스"
        );
        String id = nfluxService.save(testRequest);

        // then
        assertThat(id).isNotNull();
        NfluxResponse response = nfluxService.findDeviceById(id);
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.name()).isEqualTo(testRequest.name());
    }

    @Test
    @DisplayName("디바이스 단일 조회 테스트")
    void findDeviceTest() {
        // given
        NfluxCreateRequest testRequest = new NfluxCreateRequest(
                generateUniqueId("FIND"),
                category.getId(),
                asset.getId(),
                "조회 테스트 디바이스"
        );
        String id = nfluxService.save(testRequest);

        // when
        NfluxResponse response = nfluxService.findDeviceById(id);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(id);
        assertThat(response.name()).isEqualTo(testRequest.name());
    }

    @Test
    @DisplayName("디바이스 수정 테스트")
    void updateDeviceTest() {
        // given
        NfluxCreateRequest testRequest = new NfluxCreateRequest(
                generateUniqueId("UPDATE"),
                category.getId(),
                asset.getId(),
                "수정 테스트 디바이스"
        );
        String id = nfluxService.save(testRequest);

        NfluxUpdateRequest updateRequest = new NfluxUpdateRequest(
                category.getId(), // deviceCategoryId
                asset.getId(),    // asset  
                "수정된 이름"      // name
        );

        // when
        nfluxService.update(id, updateRequest);

        // then
        NfluxResponse updatedResponse = nfluxService.findDeviceById(id);
        assertThat(updatedResponse.id()).isEqualTo(id);
        assertThat(updatedResponse.name()).isEqualTo("수정된 이름");
    }

    @Test
    @DisplayName("디바이스 목록 조회 테스트")
    void getAllDevicesTest() {
        // given
        NfluxCreateRequest testRequest = new NfluxCreateRequest(
                generateUniqueId("LIST"), category.getId(), asset.getId(), "테스트 디바이스"
        );
        nfluxService.save(testRequest);
        
        // when
        List<NfluxResponse> responses = nfluxService.findAll();
        
        // then
        assertThat(responses).isNotEmpty();
        assertThat(responses.getFirst().name()).isEqualTo("테스트 디바이스");
    }

    @Test
    @DisplayName("존재하지 않는 디바이스 조회시 예외 발생")
    void notFoundDeviceTest() {
        // given
        String nonExistentId = "9999";
        
        // when & then
        assertThrows(CustomException.class, () -> nfluxService.findById(nonExistentId));
    }

    @Test
    @DisplayName("카테고리가 없는 디바이스 생성 테스트")
    void createDeviceWithoutCategoryTest() {
        // given
        NfluxCreateRequest requestWithoutCategory = new NfluxCreateRequest(
                generateUniqueId("NOCAT"), // String id
                null, // 카테고리 없음
                asset.getId(),
                "카테고리 없는 디바이스"
        );
        
        // when
        String deviceId = nfluxService.save(requestWithoutCategory);
        
        // then
        NfluxResponse device = nfluxService.findDeviceById(deviceId);
        assertThat(device.id()).isEqualTo(deviceId);
        assertThat(device.categoryId()).isNull();
        assertThat(device.name()).isEqualTo("카테고리 없는 디바이스");
    }

    @Test
    @DisplayName("디바이스에 Feature 할당 테스트")
    void assignFeatureToNfluxTest() {
        // given
        NfluxCreateRequest testRequest = new NfluxCreateRequest(
                generateUniqueId("FEATURE"),
                category.getId(),
                asset.getId(),
                "Feature 할당 테스트 디바이스"
        );
        String deviceId = nfluxService.save(testRequest); // 디바이스 먼저 생성 (이때 자체 Feature 보유)

        // Feature 생성
        String featureId = UUID.randomUUID().toString();
        Feature newFeature = Feature.builder()
                .id(featureId) // ID 명시적 설정
                .position(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .asset(asset)
                .build();
        Feature savedFeature = featureRepository.save(newFeature);

        // when
        nfluxService.assignFeatureToNflux(deviceId, savedFeature.getId());

        // then
        Nflux device = nfluxService.findById(deviceId);
        assertThat(device.getFeature()).isNotNull();
        assertThat(device.getFeature().getId()).isEqualTo(savedFeature.getId());
    }

    @Test
    @DisplayName("디바이스에서 피처 제거 테스트")
    void removeFeatureFromNfluxTest() {
        // given
        // 디바이스 생성
        NfluxCreateRequest testRequest = new NfluxCreateRequest(
                generateUniqueId("REMOVE"),
                category.getId(),
                asset.getId(),
                "피처 제거 테스트 디바이스"
        );
        String deviceId = nfluxService.save(testRequest);
        Nflux device = nfluxService.findById(deviceId);
        
        // Feature 직접 생성 및 저장
        String featureId = UUID.randomUUID().toString();
        Feature newFeature = Feature.builder()
                .id(featureId)
                .position(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .asset(asset)
                .build();
        featureRepository.save(newFeature);
        
        // 생성한 Feature를 디바이스에 할당
        nfluxService.assignFeatureToNflux(deviceId, featureId);
        
        // 할당 확인
        device = nfluxService.findById(deviceId);
        assertThat(device.getFeature()).isNotNull();
        assertThat(device.getFeature().getId()).isEqualTo(featureId);

        // when
        NfluxResponse response = nfluxService.removeFeatureFromNflux(deviceId);

        // then
        assertThat(response).isNotNull();

        Nflux updatedDevice = nfluxService.findById(deviceId);
        assertThat(updatedDevice.getFeature()).isNull();
    }

    @Test
    @DisplayName("이미 다른 디바이스에 할당된 피처를 할당하려 할 때 예외 발생")
    void assignFeatureToNflux_FeatureAlreadyAssigned_ThrowsException() {
        // given
        // 디바이스1 생성
        NfluxCreateRequest testRequest1 = new NfluxCreateRequest(
                generateUniqueId("ALREADY1"),
                category.getId(),
                asset.getId(),
                "테스트 디바이스 1"
        );
        String deviceId1 = nfluxService.save(testRequest1);
        
        // Feature 직접 생성 및 저장
        String featureId = UUID.randomUUID().toString();
        Feature newFeature = Feature.builder()
                .id(featureId)
                .position(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .asset(asset)
                .build();
        featureRepository.save(newFeature);
        
        // 생성한 Feature를 디바이스1에 할당
        nfluxService.assignFeatureToNflux(deviceId1, featureId);
        
        // 할당 확인
        Nflux device1 = nfluxService.findById(deviceId1);
        assertThat(device1.getFeature()).isNotNull();
        assertThat(device1.getFeature().getId()).isEqualTo(featureId);
        
        // 디바이스2 생성
        NfluxCreateRequest createRequestForDevice2 = new NfluxCreateRequest(
                generateUniqueId("ALREADY2"), // id 필드 추가
                category.getId(),
                asset.getId(),
                "테스트 디바이스 2"
        );
        String deviceId2 = nfluxService.save(createRequestForDevice2);

        // when & then
        // 디바이스2에 디바이스1의 Feature를 할당하려고 시도
        assertThrows(CustomException.class, () -> {
            nfluxService.assignFeatureToNflux(deviceId2, featureId);
        });
    }

    @Test
    @DisplayName("스테이션 ID로 디바이스 조회 및 카테고리별 그룹화 테스트")
    void findByStationIdGroupByCategoryTest() {
        // given
        // 1. 스테이션 생성
        Station station = stationRepository.save(Station.builder()
                .name("테스트 스테이션")
                .description("테스트용 스테이션입니다.")
                .build());
        
        // 2. 카테고리 생성 (일반 카테고리와 NfluxCategory 둘 다 생성)
        DeviceCategory regularCategory = deviceCategoryRepository.save(DeviceCategory.builder()
                .name("일반 카테고리")
                .build());
        // 일반 카테고리에 아이콘 파일 ID 설정
        Long regularIconFileId = createFileId("regular_icon.png");
        regularCategory.updateIconFileId(regularIconFileId);
        
        NfluxCategory nfluxCategory = (NfluxCategory) deviceCategoryRepository.save(
                NfluxCategory.nfluxBuilder()
                .name("NfluxCategory")
                .contextPath("/test-context")
                .build());
        // NfluxCategory에 아이콘 파일 ID 설정
        Long nfluxIconFileId = createFileId("nflux_icon.png");
        nfluxCategory.updateIconFileId(nfluxIconFileId);
        
        // 3. 디바이스 생성 (2개의 다른 카테고리로)
        String deviceId1 = nfluxService.save(new NfluxCreateRequest(
                generateUniqueId("REG"),
                regularCategory.getId(),
                asset.getId(),
                "일반 카테고리 디바이스"
        ));
        
        String deviceId2 = nfluxService.save(new NfluxCreateRequest(
                generateUniqueId("NFL"),
                nfluxCategory.getId(),
                asset.getId(),
                "Nflux 카테고리 디바이스"
        ));
        
        // 4. 피처 생성 및 스테이션에 연결
        String featureId1 = UUID.randomUUID().toString();
        Feature feature1 = Feature.builder()
                .id(featureId1)
                .position(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .asset(asset)
                .facility(station)
                .build();
        featureRepository.save(feature1);
        
        String featureId2 = UUID.randomUUID().toString();
        Feature feature2 = Feature.builder()
                .id(featureId2)
                .position(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .asset(asset)
                .facility(station)
                .build();
        featureRepository.save(feature2);
        
        // 5. 디바이스에 피처 할당
        nfluxService.assignFeatureToNflux(deviceId1, featureId1);
        nfluxService.assignFeatureToNflux(deviceId2, featureId2);
        
        // when
        List<NfluxCategoryGroupResponse> result = nfluxService.findByStationCodeGroupByCategory(station.getCode());
        
        // then
        assertThat(result).hasSize(2);
        assertThat(result.stream().map(NfluxCategoryGroupResponse::categoryName))
                .containsExactlyInAnyOrder("일반 카테고리", "NfluxCategory");
    }

    @Test
    @DisplayName("카테고리 ID로 디바이스 목록 조회 테스트")
    void findByCategoryIdTest() {
        // given
        NfluxCreateRequest testRequest = new NfluxCreateRequest(
                generateUniqueId("CATEGORY"), category.getId(), asset.getId(), "테스트 디바이스"
        );
        String deviceId = nfluxService.save(testRequest);
        
        // when
        List<NfluxResponse> responses = nfluxService.findByCategoryId(category.getId());
        
        // then
        assertThat(responses).isNotEmpty();
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).id()).isEqualTo(deviceId);
        assertThat(responses.get(0).categoryId()).isEqualTo(category.getId());
    }
    
    @Test
    @DisplayName("존재하지 않는 카테고리 ID로 조회시 빈 목록 반환")
    void findByNonExistentCategoryIdTest() {
        // given
        NfluxCreateRequest testRequest = new NfluxCreateRequest(
                generateUniqueId("NONEXIST"), category.getId(), asset.getId(), "테스트 디바이스"
        );
        nfluxService.save(testRequest);
        
        Long nonExistentCategoryId = 99999L;
        
        // when
        List<NfluxResponse> responses = nfluxService.findByCategoryId(nonExistentCategoryId);
        
        // then
        assertThat(responses).isEmpty();
    }
    
    @Test
    @DisplayName("디바이스에 카테고리 할당 테스트")
    void assignCategoryTest() {
        // given
        // 카테고리 없이 디바이스 생성
        NfluxCreateRequest requestWithoutCategory = new NfluxCreateRequest(
                generateUniqueId("ASSIGN"), // String id
                null, // 카테고리 없음
                asset.getId(),
                "카테고리 없는 디바이스"
        );
        String deviceId = nfluxService.save(requestWithoutCategory);
        
        // 새 카테고리 생성 (고유한 이름)
        DeviceCategory newCategory = deviceCategoryRepository.save(DeviceCategory.builder()
                .name("새 카테고리 " + UUID.randomUUID().toString().substring(0, 8))
                .build());
        
        // when
        NfluxResponse response = nfluxService.assignCategory(deviceId, newCategory.getId());
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.categoryId()).isEqualTo(newCategory.getId());
        assertThat(response.categoryName()).isEqualTo(newCategory.getName());
        
        // 디바이스를 다시 조회하여 카테고리가 정상적으로 할당되었는지 확인
        Nflux updatedDevice = nfluxService.findById(deviceId);
        assertThat(updatedDevice.getCategory()).isNotNull();
        assertThat(updatedDevice.getCategory().getId()).isEqualTo(newCategory.getId());
    }
    
    @Test
    @DisplayName("디바이스에서 카테고리 제거 테스트")
    void removeCategoryTest() {
        // given
        NfluxCreateRequest testRequest = new NfluxCreateRequest(
                generateUniqueId("RMCAT"),
                category.getId(),
                asset.getId(),
                "카테고리 제거 테스트 디바이스"
        );
        String deviceId = nfluxService.save(testRequest);
        
        // 카테고리가 할당되었는지 확인
        Nflux device = nfluxService.findById(deviceId);
        assertThat(device.getCategory()).isNotNull();
        
        // when
        NfluxResponse response = nfluxService.removeCategory(deviceId);
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.categoryId()).isNull();
        assertThat(response.categoryName()).isNull();
        
        // 디바이스를 다시 조회하여 카테고리가 정상적으로 제거되었는지 확인
        Nflux updatedDevice = nfluxService.findById(deviceId);
        assertThat(updatedDevice.getCategory()).isNull();
    }
    
    @Test
    @DisplayName("카테고리가 없는 디바이스에서 카테고리 제거 시도시 예외 발생")
    void removeCategoryFromDeviceWithoutCategory() {
        // given
        NfluxCreateRequest requestWithoutCategory = new NfluxCreateRequest(
                generateUniqueId("RMNOCAT"), // String id
                null, // 카테고리 없음
                asset.getId(),
                "카테고리 없는 디바이스"
        );
        String deviceId = nfluxService.save(requestWithoutCategory);
        
        // 카테고리가 없는지 확인
        Nflux device = nfluxService.findById(deviceId);
        assertThat(device.getCategory()).isNull();
        
        // when & then
        assertThrows(CustomException.class, () -> nfluxService.removeCategory(deviceId));
    }
    
    @Test
    @DisplayName("존재하지 않는 카테고리를 디바이스에 할당 시도시 예외 발생")
    void assignNonExistentCategoryTest() {
        // given
        NfluxCreateRequest testRequest = new NfluxCreateRequest(
                generateUniqueId("NONEXCAT"),
                category.getId(),
                asset.getId(),
                "존재하지 않는 카테고리 테스트 디바이스"
        );
        String deviceId = nfluxService.save(testRequest);
        Long nonExistentCategoryId = 9999L;
        
        // when & then
        assertThrows(CustomException.class, () -> nfluxService.assignCategory(deviceId, nonExistentCategoryId));
    }
    
    @Test
    @DisplayName("빈 이름으로 디바이스 생성 및 수정 테스트")
    void createAndUpdateDeviceWithEmptyNameTest() {
        // given
        NfluxCreateRequest requestWithEmptyName = new NfluxCreateRequest(
                generateUniqueId("EMPTY"), // String id
                category.getId(),
                asset.getId(),
                "" // 빈 이름
        );
        
        // when
        String deviceId = nfluxService.save(requestWithEmptyName);
        
        // then
        NfluxResponse savedDevice = nfluxService.findDeviceById(deviceId);
        assertThat(savedDevice.name()).isEmpty();
        
        // 빈 이름으로 업데이트
        NfluxUpdateRequest updateWithEmptyName = new NfluxUpdateRequest(
                null, null, "" // 빈 이름으로 업데이트
        );
        nfluxService.update(deviceId, updateWithEmptyName);
        
        // 업데이트 확인
        NfluxResponse updatedDevice = nfluxService.findDeviceById(deviceId);
        assertThat(updatedDevice.name()).isEmpty();
    }
    
    @Test
    @DisplayName("존재하지 않는 에셋으로 디바이스 생성 시도시 예외 발생")
    void createDeviceWithNonExistentAssetTest() {
        // given
        NfluxCreateRequest requestWithNonExistentAsset = new NfluxCreateRequest(
                generateUniqueId("ASSET"), // String id
                category.getId(),
                9999L, // 존재하지 않는 에셋 ID
                "잘못된 에셋 디바이스"
        );
        
        // when & then
        assertThrows(CustomException.class, () -> nfluxService.save(requestWithNonExistentAsset));
    }
    
    @Test
    @DisplayName("스테이션 없는 디바이스 조회 테스트")
    void findDevicesWithoutStationTest() {
        // given
        // 스테이션이 연결되지 않은 디바이스 생성
        NfluxCreateRequest testRequest = new NfluxCreateRequest(
                generateUniqueId("NOSTATION"),
                category.getId(),
                asset.getId(),
                "스테이션 없는 디바이스"
        );
        String deviceId = nfluxService.save(testRequest);
        
        // when
        // then
        // 스테이션이 존재하지 않으므로 예외가 발생해야 함
        assertThrows(CustomException.class, () -> nfluxService.findByStationCodeGroupByCategory("^^^^^"));
    }
    
    @Test
    @DisplayName("유효한 스테이션이지만 디바이스가 없는 경우 빈 목록 반환")
    void findDevicesForEmptyStationTest() {
        // given
        // 스테이션만 생성 (디바이스 연결 없음)
        Station emptyStation = stationRepository.save(Station.builder()
                .name("빈 스테이션")
                .description("디바이스가 없는 스테이션입니다.")
                .build());
        
        // when
        List<NfluxCategoryGroupResponse> result = nfluxService.findByStationCodeGroupByCategory(emptyStation.getCode());
        
        // then
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("여러 디바이스를 동일한 카테고리로 그룹화 테스트")
    void groupMultipleDevicesByCategoryTest() {
        // given
        // 1. 스테이션 생성
        Station station = stationRepository.save(Station.builder()
                .name("그룹화 테스트 스테이션")
                .description("그룹화 테스트용 스테이션")
                .build());
        
        // 2. 동일한 카테고리로 여러 디바이스 생성
        NfluxCreateRequest request1 = new NfluxCreateRequest(
                generateUniqueId("GRP1"), // String id
                category.getId(),
                asset.getId(),
                "첫 번째 디바이스"
        );
        String deviceId1 = nfluxService.save(request1);
        
        NfluxCreateRequest request2 = new NfluxCreateRequest(
                generateUniqueId("GRP2"), // String id
                category.getId(),
                asset.getId(),
                "두 번째 디바이스"
        );
        String deviceId2 = nfluxService.save(request2);
        
        // 3. 각 디바이스의 피처 생성 및 스테이션에 연결
        String featureId1 = UUID.randomUUID().toString();
        Feature feature1 = Feature.builder()
                .id(featureId1)
                .position(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .asset(asset)
                .facility(station)
                .build();
        featureRepository.save(feature1);
        
        String featureId2 = UUID.randomUUID().toString();
        Feature feature2 = Feature.builder()
                .id(featureId2)
                .position(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .asset(asset)
                .facility(station)
                .build();
        featureRepository.save(feature2);
        
        // 4. 피처를 디바이스에 할당
        nfluxService.assignFeatureToNflux(deviceId1, featureId1);
        nfluxService.assignFeatureToNflux(deviceId2, featureId2);
        
        // when
        List<NfluxCategoryGroupResponse> result = nfluxService.findByStationCodeGroupByCategory(station.getCode());
        
        // then
        assertThat(result).hasSize(1); // 하나의 카테고리만 사용했으므로
        assertThat(result.get(0).categoryId()).isEqualTo(category.getId());
        assertThat(result.get(0).devices()).hasSize(2); // 카테고리 내 디바이스 2개
        
    }
    
    @Test
    @DisplayName("카테고리 없이 그룹화된 디바이스는 결과에 포함되지 않음")
    void devicesWithoutCategoryNotIncludedInGroupingTest() {
        // given
        // 1. 스테이션 생성
        Station station = stationRepository.save(Station.builder()
                .name("카테고리 없는 디바이스 테스트 스테이션")
                .description("카테고리가 없는 디바이스 테스트용 스테이션")
                .build());
        
        // 2. 카테고리 없는 디바이스 생성
        NfluxCreateRequest requestWithoutCategory = new NfluxCreateRequest(
                generateUniqueId("NOCAT3"), // String id
                null, // 카테고리 없음
                asset.getId(),
                "카테고리 없는 디바이스"
        );
        String deviceId = nfluxService.save(requestWithoutCategory);
        
        // 3. 피처 생성 및 스테이션에 연결
        String featureId = UUID.randomUUID().toString();
        Feature feature = Feature.builder()
                .id(featureId)
                .position(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .asset(asset)
                .facility(station)
                .build();
        featureRepository.save(feature);
        
        // 4. 피처를 디바이스에 할당
        nfluxService.assignFeatureToNflux(deviceId, featureId);
        
        // when
        List<NfluxCategoryGroupResponse> result = nfluxService.findByStationCodeGroupByCategory(station.getCode());
        // then
        assertThat(result).isEmpty(); // 카테고리가 없는 디바이스는 그룹화 결과에 포함되지 않음
    }

    @Test
    @DisplayName("동일한 코드의 디바이스 중복 생성 테스트")
    void createDuplicateDeviceCodeTest() {
        // given
        // 첫 번째 디바이스 생성
        NfluxCreateRequest testRequest = new NfluxCreateRequest(
                generateUniqueId("FIRST"),
                category.getId(),
                asset.getId(),
                "첫 번째 디바이스"
        );
        String firstDeviceId = nfluxService.save(testRequest);
        NfluxResponse firstDevice = nfluxService.findDeviceById(firstDeviceId);
        
        // 같은 id로 두 번째 디바이스 생성 시도
        NfluxCreateRequest duplicateRequest = new NfluxCreateRequest(
                firstDevice.id(), // 중복된 id
                category.getId(),
                asset.getId(),
                "중복 코드 디바이스"
        );
        
        // when & then
        assertThrows(Exception.class, () -> nfluxService.save(duplicateRequest));
    }
    
    @Test
    @DisplayName("NULL 설명으로 디바이스 생성 및 업데이트 테스트")
    void createAndUpdateDeviceWithNullDescriptionTest() {
        // given - description 필드가 제거되었으므로 이 테스트는 이름 관련으로 변경
        NfluxCreateRequest requestWithNullName = new NfluxCreateRequest(
                generateUniqueId("NULL"), // String id
                category.getId(),
                asset.getId(),
                "NULL 테스트 디바이스"
        );
        
        // when
        String deviceId = nfluxService.save(requestWithNullName);
        
        // then
        NfluxResponse savedDevice = nfluxService.findDeviceById(deviceId);
        assertThat(savedDevice.name()).isEqualTo("NULL 테스트 디바이스");
        
        // 이름을 다른 값으로 업데이트
        NfluxUpdateRequest updateWithNewName = new NfluxUpdateRequest(
                null, null, "업데이트된 이름"
        );
        nfluxService.update(deviceId, updateWithNewName);
        
        // 업데이트 확인
        NfluxResponse updatedDevice = nfluxService.findDeviceById(deviceId);
        assertThat(updatedDevice.name()).isEqualTo("업데이트된 이름");
        
        // 다시 NULL로 이름을 업데이트 (NULL 파라미터는 무시되어야 함)
        NfluxUpdateRequest updateToNullName = new NfluxUpdateRequest(
                null, null, null
        );
        nfluxService.update(deviceId, updateToNullName);
        
        // 업데이트 확인 - NULL 파라미터는 무시되어야 함
        NfluxResponse finalDevice = nfluxService.findDeviceById(deviceId);
        assertThat(finalDevice.name()).isEqualTo("업데이트된 이름");
    }
    
//    @Test
//    @DisplayName("이미 삭제된 디바이스에 대한 작업 시도시 예외 발생")
//    void operationsOnDeletedDeviceTest() {
//        // given
//        NfluxCreateRequest testRequest = new NfluxCreateRequest(
//                generateUniqueId("DELETED"),
//                category.getId(),
//                asset.getId(),
//                "삭제될 디바이스"
//        );
//        String deviceId = nfluxService.save(testRequest);
//
//        // 디바이스 삭제
//        nfluxService.delete(deviceId);
//
//        // 영속성 컨텍스트 강제 flush 및 clear
//        entityManager.flush();
//        entityManager.clear();
//
//        // when & then
//        // 삭제된 디바이스 조회 시도
//        NfluxResponse deviceById = nfluxService.findDeviceById(deviceId);
//        System.out.println("deviceById = " + deviceById);
//        assertThrows(CustomException.class, () -> nfluxService.findDeviceById(deviceId));
//
//        // 삭제된 디바이스 업데이트 시도
//        NfluxUpdateRequest updateRequest = new NfluxUpdateRequest(
//                null, null, "업데이트 시도"
//        );
//        assertThrows(CustomException.class, () -> nfluxService.update(deviceId, updateRequest));
//
//        // 삭제된 디바이스에 카테고리 할당 시도
//        assertThrows(CustomException.class, () -> nfluxService.assignCategory(deviceId, category.getId()));
//
//        // 삭제된 디바이스에서 카테고리 제거 시도
//        assertThrows(CustomException.class, () -> nfluxService.removeCategory(deviceId));
//    }
    
    @Test
    @DisplayName("디바이스 피처 변경 테스트")
    void changeFeatureTest() {
        // given
        // 디바이스 생성
        NfluxCreateRequest testRequest = new NfluxCreateRequest(
                generateUniqueId("CHANGE"),
                category.getId(),
                asset.getId(),
                "피처 변경 테스트 디바이스"
        );
        String deviceId = nfluxService.save(testRequest);
        Nflux device = nfluxService.findById(deviceId);
        
        // 원래 피처 확인
        Feature originalFeature = device.getFeature();
        assertThat(originalFeature).isNull(); // 초기에는 Feature가 없을 수 있음
        
        // 첫 번째 피처 생성 및 할당
        String featureId1 = UUID.randomUUID().toString();
        Feature feature1 = Feature.builder()
                .id(featureId1)
                .position(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .asset(asset)
                .build();
        featureRepository.save(feature1);
        
        // 첫 번째 피처 할당
        nfluxService.assignFeatureToNflux(deviceId, featureId1);
        
        // 할당 확인
        device = nfluxService.findById(deviceId);
        assertThat(device.getFeature()).isNotNull();
        assertThat(device.getFeature().getId()).isEqualTo(featureId1);
        
        // 두 번째 피처 생성
        String featureId2 = UUID.randomUUID().toString();
        Feature feature2 = Feature.builder()
                .id(featureId2)
                .position(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .asset(asset)
                .build();
        featureRepository.save(feature2);
        
        // when
        // 첫 번째 피처 제거
        nfluxService.removeFeatureFromNflux(deviceId);
        
        // 두 번째 피처 할당
        NfluxResponse response = nfluxService.assignFeatureToNflux(deviceId, featureId2);
        
        // then
        Nflux updatedDevice = nfluxService.findById(deviceId);
        assertThat(updatedDevice.getFeature()).isNotNull();
        assertThat(updatedDevice.getFeature().getId()).isEqualTo(featureId2);
    }
    
    @Test
    @DisplayName("존재하지 않는 피처 할당 시도시 예외 발생")
    void assignNonExistentFeatureTest() {
        // given
        NfluxCreateRequest testRequest = new NfluxCreateRequest(
                generateUniqueId("NONFEAT"),
                category.getId(),
                asset.getId(),
                "존재하지 않는 피처 테스트 디바이스"
        );
        String deviceId = nfluxService.save(testRequest);
        String nonExistentFeatureId = UUID.randomUUID().toString();
        
        // when & then
        assertThrows(CustomException.class, () -> 
            nfluxService.assignFeatureToNflux(deviceId, nonExistentFeatureId)
        );
    }

    @Test
    @DisplayName("업데이트시 모든 필드가 NULL일 때 변경이 없어야 함")
    void updateWithAllNullFieldsTest() {
        // given
        NfluxCreateRequest testRequest = new NfluxCreateRequest(
                generateUniqueId("NULLUP"),
                category.getId(),
                asset.getId(),
                "NULL 업데이트 테스트 디바이스"
        );
        String deviceId = nfluxService.save(testRequest);
        NfluxResponse originalDevice = nfluxService.findDeviceById(deviceId);
        
        // NULL로만 구성된 업데이트 요청
        NfluxUpdateRequest nullUpdateRequest = new NfluxUpdateRequest(
                null, null, null
        );
        
        // when
        nfluxService.update(deviceId, nullUpdateRequest);
        
        // then
        NfluxResponse updatedDevice = nfluxService.findDeviceById(deviceId);
        
        // 업데이트 전후 모든 필드가 동일해야 함
        assertThat(updatedDevice.name()).isEqualTo(originalDevice.name());
        assertThat(updatedDevice.id()).isEqualTo(originalDevice.id()); // code 대신 id 비교
        assertThat(updatedDevice.categoryId()).isEqualTo(originalDevice.categoryId());
    }

    @Test
    @DisplayName("디바이스 생성-업데이트-삭제 전체 라이프사이클 테스트")
    void deviceLifecycleTest() {
        // 1. 디바이스 생성
        NfluxCreateRequest testRequest = new NfluxCreateRequest(
                generateUniqueId("LIFECYCLE"),
                category.getId(),
                asset.getId(),
                "라이프사이클 테스트 디바이스"
        );
        String deviceId = nfluxService.save(testRequest);
        NfluxResponse createdDevice = nfluxService.findDeviceById(deviceId);
        assertThat(createdDevice.name()).isEqualTo("라이프사이클 테스트 디바이스");
        
        // 2. 디바이스 업데이트
        NfluxUpdateRequest updateRequest = new NfluxUpdateRequest(
                null, null, "업데이트된 디바이스"
        );
        nfluxService.update(deviceId, updateRequest);
        
        NfluxResponse updatedDevice = nfluxService.findDeviceById(deviceId);
        assertThat(updatedDevice.name()).isEqualTo("업데이트된 디바이스");
        
        // 3. 새 카테고리 생성 및 할당 (고유한 이름)
        String uniqueCategoryName = "새 라이프사이클 카테고리 " + UUID.randomUUID().toString().substring(0, 8);
        DeviceCategory newCategory = deviceCategoryRepository.save(DeviceCategory.builder()
                .name(uniqueCategoryName)
                .build());
        
        nfluxService.assignCategory(deviceId, newCategory.getId());
        
        NfluxResponse deviceWithNewCategory = nfluxService.findDeviceById(deviceId);
        assertThat(deviceWithNewCategory.categoryId()).isEqualTo(newCategory.getId());
        
        // 4. 피처 생성 및 할당
        String featureId = UUID.randomUUID().toString();
        Feature feature = Feature.builder()
                .id(featureId)
                .position(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .asset(asset)
                .build();
        featureRepository.save(feature);
        
        nfluxService.assignFeatureToNflux(deviceId, featureId);
        
        NfluxResponse deviceWithFeature = nfluxService.findDeviceById(deviceId);

        // 5. 피처 제거
        nfluxService.removeFeatureFromNflux(deviceId);
        
        NfluxResponse deviceWithoutFeature = nfluxService.findDeviceById(deviceId);

        // 6. 카테고리 제거
        nfluxService.removeCategory(deviceId);
        
        NfluxResponse deviceWithoutCategory = nfluxService.findDeviceById(deviceId);
        assertThat(deviceWithoutCategory.categoryId()).isNull();
        
        // 7. 디바이스 삭제
        nfluxService.delete(deviceId);
        
        // 8. 삭제 확인
        assertThrows(CustomException.class, () -> nfluxService.findById(deviceId));
    }

    @Test
    @DisplayName("여러 개의 디바이스 조회 테스트")
    void findMultipleDevicesTest() {
        // given
        // 여러 디바이스 생성
        NfluxCreateRequest request1 = new NfluxCreateRequest(
                generateUniqueId("MULTI1"),
                category.getId(),
                asset.getId(),
                "첫 번째 디바이스"
        );
        String deviceId1 = nfluxService.save(request1);
        
        NfluxCreateRequest request2 = new NfluxCreateRequest(
                generateUniqueId("MULTI2"), // String id
                category.getId(),
                asset.getId(),
                "두 번째 디바이스"
        );
        String deviceId2 = nfluxService.save(request2);
        
        NfluxCreateRequest request3 = new NfluxCreateRequest(
                generateUniqueId("MULTI3"), // String id
                category.getId(),
                asset.getId(),
                "세 번째 디바이스"
        );
        String deviceId3 = nfluxService.save(request3);
        
        // when
        List<NfluxResponse> allDevices = nfluxService.findAll();
        
        // then
        assertThat(allDevices).hasSize(3);
        
        // 각 디바이스 ID 확인
        List<String> deviceIds = allDevices.stream()
                .map(NfluxResponse::id)
                .toList();
        assertThat(deviceIds).containsExactlyInAnyOrder(deviceId1, deviceId2, deviceId3);
    }

    @Test
    @DisplayName("엔티티 영속성 테스트")
    void entityPersistenceTest() {
        // given
        NfluxCreateRequest testRequest = new NfluxCreateRequest(
                generateUniqueId("PERSIST"), // 고유한 ID 생성
                category.getId(),
                asset.getId(),
                "영속성 테스트 디바이스"
        );
        String deviceId = nfluxService.save(testRequest);
        
        // when
        // 엔티티 매니저 캐시 초기화
        entityManager.flush();
        entityManager.clear();
        
        // 디바이스 다시 로드
        Nflux device = nfluxService.findById(deviceId);
        
        // then
        assertThat(device).isNotNull();
        assertThat(device.getId()).isEqualTo(deviceId);
        assertThat(device.getName()).isEqualTo(testRequest.name());
        assertThat(device.getDeviceCode()).isEqualTo(deviceId); // code는 이제 id와 동일
    }

    @Test
    @DisplayName("특수 문자가 포함된 디바이스 코드 처리 테스트")
    void deviceCodeWithSpecialCharactersTest() {
        // given
        String specialId = generateUniqueId("SPEC!@#$%^&*()");
        NfluxCreateRequest requestWithSpecialChars = new NfluxCreateRequest(
                specialId, // 특수문자 포함 id
                category.getId(),
                asset.getId(),
                "특수문자 코드 디바이스"
        );
        
        // when
        String deviceId = nfluxService.save(requestWithSpecialChars);
        
        // then
        NfluxResponse device = nfluxService.findDeviceById(deviceId);
        assertThat(device.id()).isEqualTo(specialId); // 실제 생성된 ID와 비교
    }
    
    @Test
    @DisplayName("매우 긴 이름과 설명으로 디바이스 생성 테스트")
    void deviceWithVeryLongNameAndDescriptionTest() {
        // given
        String longName = "a".repeat(255); // 최대 길이의 이름
        
        NfluxCreateRequest requestWithLongValues = new NfluxCreateRequest(
                generateUniqueId("LONG"), // String id
                category.getId(),
                asset.getId(),
                longName
        );
        
        // when
        String deviceId = nfluxService.save(requestWithLongValues);
        
        // then
        NfluxResponse device = nfluxService.findDeviceById(deviceId);
        assertThat(device.name()).isEqualTo(longName);
    }
    
    @Test
    @DisplayName("스테이션 ID가 NULL인 경우 findByStationIdGroupByCategory 테스트")
    void findByNullStationIdTest() {
        // when & then
        assertThrows(CustomException.class, () ->
            nfluxService.findByStationCodeGroupByCategory(null)
        );
    }
    
    @Test
    @DisplayName("NfluxCategory의 contextPath 테스트")
    void nfluxCategoryContextPathTest() {
        // given
        // 1. NfluxCategory 생성 (고유한 이름)
        String uniqueName = "Context 테스트 카테고리 " + UUID.randomUUID().toString().substring(0, 8);
        NfluxCategory nfluxCategory = (NfluxCategory) deviceCategoryRepository.save(
                NfluxCategory.nfluxBuilder()
                .name(uniqueName)
                .contextPath("/custom-context")
                .build());
        
        // 2. 디바이스 생성
        NfluxCreateRequest requestWithNfluxCategory = new NfluxCreateRequest(
                generateUniqueId("CTX"), // String id
                nfluxCategory.getId(),
                asset.getId(),
                "Context 테스트 디바이스"
        );
        String deviceId = nfluxService.save(requestWithNfluxCategory);
        
        // 3. 스테이션 생성 (고유한 이름)
        String stationName = "Context 테스트 스테이션 " + UUID.randomUUID().toString().substring(0, 8);
        Station station = stationRepository.save(Station.builder()
                .name(stationName)
                .description("Context 테스트용 스테이션")
                .build());
        
        // 4. 피처 생성 및 스테이션에 연결
        String featureId = UUID.randomUUID().toString();
        Feature feature = Feature.builder()
                .id(featureId)
                .position(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .asset(asset)
                .facility(station)
                .build();
        featureRepository.save(feature);
        
        // 5. 피처를 디바이스에 할당
        nfluxService.assignFeatureToNflux(deviceId, featureId);
        
        // when
        List<NfluxCategoryGroupResponse> result = nfluxService.findByStationCodeGroupByCategory(station.getCode());
        
        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).categoryId()).isEqualTo(nfluxCategory.getId());
        assertThat(result.get(0).contextPath()).isEqualTo("/custom-context");
    }
    
    @Test
    @DisplayName("디바이스가 없는 경우 findAll 결과 테스트")
    void findAllWithNoDevicesTest() {
        // given
        // 모든 디바이스 삭제 (현재 테스트에서 생성된 디바이스가 있을 수 있음)
        deviceRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
        
        // when
        List<NfluxResponse> result = nfluxService.findAll();
        
        // then
        assertThat(result).isEmpty();
    }
    
    @Test
    @DisplayName("두 디바이스가 동일한 Feature를 가질 수 없음을 검증하는 테스트")
    void twoDevicesCannotShareSameFeatureTest() {
        // given
        // 1. 첫 번째 디바이스 생성
        NfluxCreateRequest request1 = new NfluxCreateRequest(
                generateUniqueId("SHARE1"), // 고유한 ID 생성
                category.getId(),
                asset.getId(),
                "첫 번째 디바이스"
        );
        String deviceId1 = nfluxService.save(request1);
        
        // 2. 두 번째 디바이스 생성
        NfluxCreateRequest request2 = new NfluxCreateRequest(
                generateUniqueId("SHARE2"), // String id
                category.getId(),
                asset.getId(),
                "두 번째 디바이스"
        );
        String deviceId2 = nfluxService.save(request2);
        
        // 3. Feature 생성
        String featureId = UUID.randomUUID().toString();
        Feature feature = Feature.builder()
                .id(featureId)
                .position(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .asset(asset)
                .build();
        featureRepository.save(feature);
        
        // 4. 첫 번째 디바이스에 Feature 할당
        nfluxService.assignFeatureToNflux(deviceId1, featureId);
        
        // 5. 할당 확인
        Nflux device1 = nfluxService.findById(deviceId1);
        assertThat(device1.getFeature()).isNotNull();
        assertThat(device1.getFeature().getId()).isEqualTo(featureId);
        
        // when & then
        // 6. 두 번째 디바이스에 동일한 Feature 할당 시도 - 예외 발생해야 함
        assertThrows(CustomException.class, () -> 
            nfluxService.assignFeatureToNflux(deviceId2, featureId)
        );
    }

    @Test
    @DisplayName("에셋 없이 디바이스 생성 테스트")
    void createDeviceWithoutAssetTest() {
        // given
        NfluxCreateRequest requestWithoutAsset = new NfluxCreateRequest(
                generateUniqueId("NOASSET"), // String id
                category.getId(),
                null, // 에셋 없음
                "에셋 없는 디바이스"
        );
        
        // when
        String deviceId = nfluxService.save(requestWithoutAsset);
        
        // then
        NfluxResponse device = nfluxService.findDeviceById(deviceId);
        assertThat(device.id()).isEqualTo(deviceId);
    }
}