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
import com.pluxity.feature.dto.FeatureCreateRequest;
import com.pluxity.feature.dto.FeatureUpdateRequest;
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
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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
        try {
            // 테스트 이미지 파일 준비
            ClassPathResource resource = new ClassPathResource("temp/temp.png");
            fileContent = Files.readAllBytes(Path.of(resource.getURI()));
        } catch (IOException e) {
            // 파일을 찾을 수 없는 경우 기본 바이트 배열 생성
            fileContent = new byte[100];
        }
        
        // 테스트 데이터 준비
        category = deviceCategoryRepository.save(DeviceCategory.builder()
                .name("테스트 카테고리")
                .build());

        asset = assetRepository.save(Asset.builder()
                .name("테스트 2D 에셋")
                .build());

        // Feature 생성
        String featureId = UUID.randomUUID().toString();
        feature = featureRepository.save(Feature.builder()
                .id(featureId)
                .position(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .asset(asset)
                .build());

        // FeatureCreateRequest 생성 (NfluxCreateRequest 내부용)
        FeatureCreateRequest featureRequestForDevice = new FeatureCreateRequest(
                UUID.randomUUID().toString(), // 디바이스 생성시 새로운 Feature ID
                Spatial.builder().x(1.0).y(1.0).z(1.0).build(),
                Spatial.builder().x(0.0).y(0.0).z(0.0).build(),
                Spatial.builder().x(1.0).y(1.0).z(1.0).build(),
                asset.getId(),
                null,
                null
        );

        // 디바이스 생성 요청
        createRequest = new NfluxCreateRequest(
                category.getId(),
                asset.getId(),
                "테스트 디바이스",
                "TEST001",
                "테스트용 디바이스입니다."
        );
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
    @DisplayName("디바이스 생성 테스트")
    void createDeviceTest() {
        // when
        Long id = nfluxService.save(createRequest);
        
        // then
        NfluxResponse savedDevice = nfluxService.findDeviceById(id);
        assertThat(savedDevice).isNotNull();
        assertThat(savedDevice.name()).isEqualTo("테스트 디바이스");
        assertThat(savedDevice.code()).isEqualTo("TEST001");
        assertThat(savedDevice.categoryId()).isEqualTo(category.getId());
    }

    @Test
    @DisplayName("디바이스 목록 조회 테스트")
    void getAllDevicesTest() {
        // given
        nfluxService.save(createRequest);
        
        // when
        List<NfluxResponse> responses = nfluxService.findAll();
        
        // then
        assertThat(responses).isNotEmpty();
        assertThat(responses.getFirst().name()).isEqualTo("테스트 디바이스");
        assertThat(responses.getFirst().code()).isEqualTo("TEST001");
    }

    @Test
    @DisplayName("디바이스 단일 조회 테스트")
    void getDeviceByIdTest() {
        // given
        Long id = nfluxService.save(createRequest);
        
        // when
        NfluxResponse response = nfluxService.findDeviceById(id);
        
        // then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("테스트 디바이스");
        assertThat(response.code()).isEqualTo("TEST001");
        assertThat(response.description()).isEqualTo("테스트용 디바이스입니다.");
    }

    @Test
    @DisplayName("디바이스 수정 테스트")
    void updateDeviceTest() {
        // given
        Long id = nfluxService.save(createRequest);
        
        // 위치 정보 업데이트
        Spatial updatedPosition = Spatial.builder().x(1.0).y(1.0).z(1.0).build();
        FeatureUpdateRequest featureUpdateRequest = new FeatureUpdateRequest(
                updatedPosition,
                null, // rotation 변경 없음
                null  // scale 변경 없음
        );
        
        NfluxUpdateRequest updateRequest = new NfluxUpdateRequest(
                null, // 카테고리 변경 없음
                null, // 에셋 변경 없음
                "수정된 디바이스",
                "TEST002",
                "수정된 디바이스 설명입니다."
        );
        
        // when
        nfluxService.update(id, updateRequest);
        
        // then
        NfluxResponse updatedDevice = nfluxService.findDeviceById(id);
        assertThat(updatedDevice.name()).isEqualTo("수정된 디바이스");
        assertThat(updatedDevice.code()).isEqualTo("TEST002");
        assertThat(updatedDevice.description()).isEqualTo("수정된 디바이스 설명입니다.");
    }

    @Test
    @DisplayName("디바이스 삭제 테스트")
    void deleteDeviceTest() {
        // given
        Long id = nfluxService.save(createRequest);
        
        // when
        nfluxService.delete(id);
        
        // then
        assertThrows(CustomException.class, () -> nfluxService.findById(id));
    }

    @Test
    @DisplayName("존재하지 않는 디바이스 조회시 예외 발생")
    void notFoundDeviceTest() {
        // given
        Long nonExistentId = 9999L;
        
        // when & then
        assertThrows(CustomException.class, () -> nfluxService.findById(nonExistentId));
    }

    @Test
    @DisplayName("카테고리가 없는 디바이스 생성 테스트")
    void createDeviceWithoutCategoryTest() {
        // given
        NfluxCreateRequest requestWithoutCategory = new NfluxCreateRequest(
                null, // 카테고리 없음
                asset.getId(),
                createRequest.name(),
                createRequest.code(),
                createRequest.description()
        );
        
        // when
        Long id = nfluxService.save(requestWithoutCategory);
        
        // then
        NfluxResponse savedDevice = nfluxService.findDeviceById(id);
        assertThat(savedDevice).isNotNull();
        assertThat(savedDevice.categoryId()).isNull();
    }

    @Test
    @DisplayName("디바이스에 피처 할당 테스트")
    void assignFeatureToNfluxTest() {
        // given
        Long deviceId = nfluxService.save(createRequest); // 디바이스 먼저 생성 (이때 자체 Feature 보유)
        Nflux device = nfluxService.findById(deviceId);
        
        // 기존 디바이스의 Feature 제거 (새 Feature 할당을 위해)
        if (device.getFeature() != null) {
            Feature oldFeature = device.getFeature();
            device.changeFeature(null); // Device에서 Feature 연결 제거
            featureRepository.delete(oldFeature); // 기존 Feature 삭제
            entityManager.flush(); // 변경사항 즉시 반영
            entityManager.clear(); // 영속성 컨텍스트 초기화
            device = nfluxService.findById(deviceId); // 디바이스 다시 조회
        }

        String newFeatureId = UUID.randomUUID().toString();
        Feature newFeature = featureRepository.save(Feature.builder().id(newFeatureId).build());

        // when
        NfluxResponse response = nfluxService.assignFeatureToNflux(deviceId, newFeature.getId());

        // then
        assertThat(response).isNotNull();

        Nflux updatedDevice = nfluxService.findById(deviceId);
        assertThat(updatedDevice.getFeature()).isNotNull();
        assertThat(updatedDevice.getFeature().getId()).isEqualTo(newFeature.getId());
    }

    @Test
    @DisplayName("디바이스에서 피처 제거 테스트")
    void removeFeatureFromNfluxTest() {
        // given
        // 디바이스 생성
        Long deviceId = nfluxService.save(createRequest);
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
        Long deviceId1 = nfluxService.save(createRequest);
        
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
                category.getId(),
                asset.getId(),
                "테스트 디바이스 2",
                "TEST002",
                "두번째 테스트 디바이스"
        );
        Long deviceId2 = nfluxService.save(createRequestForDevice2);

        // when & then
        // 디바이스2에 디바이스1의 Feature를 할당하려고 시도
        assertThrows(CustomException.class, () -> {
            nfluxService.assignFeatureToNflux(deviceId2, featureId);
        });
    }

//    @Test //TODO: 이 테스트는 현재 피처가 없는 디바이스에 대해 예외를 발생시키지 않도록 변경되어 주석 처리함
//    @DisplayName("디바이스에 피처가 할당되지 않았을 때 제거 시도 시 예외 발생")
//    void removeFeatureFromNflux_NoFeatureAssigned_ThrowsException() {
//        // given
//        NfluxCreateRequest requestWithoutFeature = new NfluxCreateRequest(
//            null, // Feature 없음
//            category.getId(),
//            "피처 없는 디바이스",
//            "TEST003",
//            "피처가 없는 테스트용 디바이스입니다."
//        );
//        Long deviceId = nfluxService.save(requestWithoutFeature);
//
//        // when & then
//        assertThrows(CustomException.class, () -> {
//            nfluxService.removeFeatureFromNflux(deviceId);
//        });
//    }

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
        Long deviceId1 = nfluxService.save(new NfluxCreateRequest(
                regularCategory.getId(),
                asset.getId(),
                "일반 카테고리 디바이스",
                "REG001",
                "일반 카테고리 디바이스입니다."
        ));
        
        Long deviceId2 = nfluxService.save(new NfluxCreateRequest(
                nfluxCategory.getId(),
                asset.getId(),
                "Nflux 카테고리 디바이스",
                "NFL001",
                "Nflux 카테고리 디바이스입니다."
        ));
        
        // 4. 피처 생성 및 스테이션에 연결
        String featureId1 = UUID.randomUUID().toString();
        Feature feature1 = Feature.builder()
                .id(featureId1)
                .position(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .asset(asset)
                .facility(station)  // 스테이션 연결
                .build();
        featureRepository.save(feature1);
        
        String featureId2 = UUID.randomUUID().toString();
        Feature feature2 = Feature.builder()
                .id(featureId2)
                .position(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .asset(asset)
                .facility(station)  // 스테이션 연결
                .build();
        featureRepository.save(feature2);
        
        // 5. 디바이스에 피처 할당
        nfluxService.assignFeatureToNflux(deviceId1, featureId1);
        nfluxService.assignFeatureToNflux(deviceId2, featureId2);
        
        // when
        List<NfluxCategoryGroupResponse> result = nfluxService.findByStationIdGroupByCategory(station.getId());
        
        // then
        assertThat(result).hasSize(2);  // 두 개의 다른 카테고리
        
        // 카테고리 ID 기준으로 정렬 (불변 리스트를 새 ArrayList로 복사 후 정렬)
        List<NfluxCategoryGroupResponse> sortedResult = new ArrayList<>(result);
        sortedResult.sort((a, b) -> a.categoryId().compareTo(b.categoryId()));
        
        // 첫 번째 카테고리 (일반 카테고리) 검증
        assertThat(sortedResult.get(0).categoryId()).isEqualTo(regularCategory.getId());
        assertThat(sortedResult.get(0).categoryName()).isEqualTo(regularCategory.getName());
        assertThat(sortedResult.get(0).contextPath()).isNull();  // 일반 카테고리는 contextPath가 없음
        assertThat(sortedResult.get(0).iconFile()).isNotNull();  // 파일이 있어야 함
        assertThat(sortedResult.get(0).iconFile().id()).isEqualTo(regularIconFileId);
        assertThat(sortedResult.get(0).devices()).hasSize(1);
        assertThat(sortedResult.get(0).devices().get(0).name()).isEqualTo("일반 카테고리 디바이스");
        
        // 두 번째 카테고리 (NfluxCategory) 검증
        assertThat(sortedResult.get(1).categoryId()).isEqualTo(nfluxCategory.getId());
        assertThat(sortedResult.get(1).categoryName()).isEqualTo(nfluxCategory.getName());
        assertThat(sortedResult.get(1).contextPath()).isEqualTo("/test-context");  // NfluxCategory는 contextPath가 있음
        assertThat(sortedResult.get(1).iconFile()).isNotNull();  // 파일이 있어야 함
        assertThat(sortedResult.get(1).iconFile().id()).isEqualTo(nfluxIconFileId);
        assertThat(sortedResult.get(1).devices()).hasSize(1);
        assertThat(sortedResult.get(1).devices().get(0).name()).isEqualTo("Nflux 카테고리 디바이스");
    }

    @Test
    @DisplayName("카테고리 ID로 디바이스 목록 조회 테스트")
    void findByCategoryIdTest() {
        // given
        Long deviceId = nfluxService.save(createRequest);
        
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
        nfluxService.save(createRequest);
        Long nonExistentCategoryId = 9999L;
        
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
                null, asset.getId(), "카테고리 없는 디바이스", "CAT001", "카테고리 테스트용 디바이스"
        );
        Long deviceId = nfluxService.save(requestWithoutCategory);
        
        // 새 카테고리 생성
        DeviceCategory newCategory = deviceCategoryRepository.save(DeviceCategory.builder()
                .name("새 카테고리")
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
        Long deviceId = nfluxService.save(createRequest);
        
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
                null, asset.getId(), "카테고리 없는 디바이스", "CAT002", "카테고리 테스트용 디바이스"
        );
        Long deviceId = nfluxService.save(requestWithoutCategory);
        
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
        Long deviceId = nfluxService.save(createRequest);
        Long nonExistentCategoryId = 9999L;
        
        // when & then
        assertThrows(CustomException.class, () -> nfluxService.assignCategory(deviceId, nonExistentCategoryId));
    }
    
    @Test
    @DisplayName("빈 이름으로 디바이스 생성 및 수정 테스트")
    void createAndUpdateDeviceWithEmptyNameTest() {
        // given
        NfluxCreateRequest requestWithEmptyName = new NfluxCreateRequest(
                category.getId(), asset.getId(), "", "EMPTY001", "빈 이름 테스트"
        );
        
        // when
        Long deviceId = nfluxService.save(requestWithEmptyName);
        
        // then
        NfluxResponse savedDevice = nfluxService.findDeviceById(deviceId);
        assertThat(savedDevice.name()).isEmpty();
        
        // 빈 이름으로 업데이트
        NfluxUpdateRequest updateWithEmptyName = new NfluxUpdateRequest(
                null, null, "", null, null
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
                category.getId(), 9999L, "잘못된 에셋 디바이스", "ASSET001", "잘못된 에셋 테스트"
        );
        
        // when & then
        assertThrows(CustomException.class, () -> nfluxService.save(requestWithNonExistentAsset));
    }
    
    @Test
    @DisplayName("스테이션 없는 디바이스 조회 테스트")
    void findDevicesWithoutStationTest() {
        // given
        // 스테이션이 연결되지 않은 디바이스 생성
        Long deviceId = nfluxService.save(createRequest);
        
        // when
        // then
        // 스테이션이 존재하지 않으므로 예외가 발생해야 함
        assertThrows(CustomException.class, () -> nfluxService.findByStationIdGroupByCategory(9999L));
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
        List<NfluxCategoryGroupResponse> result = nfluxService.findByStationIdGroupByCategory(emptyStation.getId());
        
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
                category.getId(), asset.getId(), "첫 번째 디바이스", "DEV001", "첫 번째 디바이스 설명"
        );
        Long deviceId1 = nfluxService.save(request1);
        
        NfluxCreateRequest request2 = new NfluxCreateRequest(
                category.getId(), asset.getId(), "두 번째 디바이스", "DEV002", "두 번째 디바이스 설명"
        );
        Long deviceId2 = nfluxService.save(request2);
        
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
        List<NfluxCategoryGroupResponse> result = nfluxService.findByStationIdGroupByCategory(station.getId());
        
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
                null, asset.getId(), "카테고리 없는 디바이스", "NO_CAT", "카테고리가 없는 디바이스 설명"
        );
        Long deviceId = nfluxService.save(requestWithoutCategory);
        
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
        List<NfluxCategoryGroupResponse> result = nfluxService.findByStationIdGroupByCategory(station.getId());
        
        // then
        assertThat(result).isEmpty(); // 카테고리가 없는 디바이스는 그룹화 결과에 포함되지 않음
    }

    @Test
    @DisplayName("동일한 코드의 디바이스 중복 생성 테스트")
    void createDuplicateDeviceCodeTest() {
        // given
        // 첫 번째 디바이스 생성
        Long firstDeviceId = nfluxService.save(createRequest);
        NfluxResponse firstDevice = nfluxService.findDeviceById(firstDeviceId);
        assertThat(firstDevice.code()).isEqualTo("TEST001");
        
        // 같은 코드로 두 번째 디바이스 생성
        NfluxCreateRequest duplicateRequest = new NfluxCreateRequest(
                category.getId(), asset.getId(), "중복 코드 디바이스", "TEST001", "중복 코드 테스트"
        );
        
        // when
        Long secondDeviceId = nfluxService.save(duplicateRequest);
        NfluxResponse secondDevice = nfluxService.findDeviceById(secondDeviceId);
        
        // then
        // 코드 중복 검사가 없다면 두 디바이스의 코드가 동일할 것
        assertThat(secondDevice.code()).isEqualTo(firstDevice.code());
        assertThat(secondDeviceId).isNotEqualTo(firstDeviceId);
    }
    
    @Test
    @DisplayName("NULL 설명으로 디바이스 생성 및 업데이트 테스트")
    void createAndUpdateDeviceWithNullDescriptionTest() {
        // given
        NfluxCreateRequest requestWithNullDescription = new NfluxCreateRequest(
                category.getId(), asset.getId(), "NULL 설명 디바이스", "NULL001", null
        );
        
        // when
        Long deviceId = nfluxService.save(requestWithNullDescription);
        
        // then
        NfluxResponse savedDevice = nfluxService.findDeviceById(deviceId);
        assertThat(savedDevice.description()).isNull();
        
        // 설명을 추가하는 업데이트
        NfluxUpdateRequest updateWithDescription = new NfluxUpdateRequest(
                null, null, null, null, "추가된 설명"
        );
        nfluxService.update(deviceId, updateWithDescription);
        
        // 업데이트 확인
        NfluxResponse updatedDevice = nfluxService.findDeviceById(deviceId);
        assertThat(updatedDevice.description()).isEqualTo("추가된 설명");
        
        // 다시 NULL로 설명을 업데이트
        NfluxUpdateRequest updateToNullDescription = new NfluxUpdateRequest(
                null, null, null, null, null
        );
        nfluxService.update(deviceId, updateToNullDescription);
        
        // 업데이트 확인
        NfluxResponse finalDevice = nfluxService.findDeviceById(deviceId);
        // NULL 파라미터는 무시되어야 함
        assertThat(finalDevice.description()).isEqualTo("추가된 설명");
    }
    
    @Test
    @DisplayName("이미 삭제된 디바이스에 대한 작업 시도시 예외 발생")
    void operationsOnDeletedDeviceTest() {
        // given
        Long deviceId = nfluxService.save(createRequest);
        
        // 디바이스 삭제
        nfluxService.delete(deviceId);
        
        // when & then
        // 삭제된 디바이스 조회 시도
        assertThrows(CustomException.class, () -> nfluxService.findDeviceById(deviceId));
        
        // 삭제된 디바이스 업데이트 시도
        NfluxUpdateRequest updateRequest = new NfluxUpdateRequest(
                null, null, "업데이트 시도", null, null
        );
        assertThrows(CustomException.class, () -> nfluxService.update(deviceId, updateRequest));
        
        // 삭제된 디바이스에 카테고리 할당 시도
        assertThrows(CustomException.class, () -> nfluxService.assignCategory(deviceId, category.getId()));
        
        // 삭제된 디바이스에서 카테고리 제거 시도
        assertThrows(CustomException.class, () -> nfluxService.removeCategory(deviceId));
    }
    
    @Test
    @DisplayName("디바이스 피처 변경 테스트")
    void changeFeatureTest() {
        // given
        // 디바이스 생성
        Long deviceId = nfluxService.save(createRequest);
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
        Long deviceId = nfluxService.save(createRequest);
        String nonExistentFeatureId = UUID.randomUUID().toString();
        
        // when & then
        assertThrows(CustomException.class, () -> 
            nfluxService.assignFeatureToNflux(deviceId, nonExistentFeatureId)
        );
    }
    
    @Test
    @DisplayName("여러 개의 디바이스 조회 테스트")
    void findMultipleDevicesTest() {
        // given
        // 여러 디바이스 생성
        Long deviceId1 = nfluxService.save(createRequest);
        
        NfluxCreateRequest request2 = new NfluxCreateRequest(
                category.getId(), asset.getId(), "두 번째 디바이스", "TEST002", "두 번째 테스트 디바이스"
        );
        Long deviceId2 = nfluxService.save(request2);
        
        NfluxCreateRequest request3 = new NfluxCreateRequest(
                category.getId(), asset.getId(), "세 번째 디바이스", "TEST003", "세 번째 테스트 디바이스"
        );
        Long deviceId3 = nfluxService.save(request3);
        
        // when
        List<NfluxResponse> allDevices = nfluxService.findAll();
        
        // then
        assertThat(allDevices).hasSize(3);
        
        // 각 디바이스 ID 확인
        List<Long> deviceIds = allDevices.stream()
                .map(NfluxResponse::id)
                .toList();
        assertThat(deviceIds).containsExactlyInAnyOrder(deviceId1, deviceId2, deviceId3);
    }
    
    @Test
    @DisplayName("업데이트시 모든 필드가 NULL일 때 변경이 없어야 함")
    void updateWithAllNullFieldsTest() {
        // given
        Long deviceId = nfluxService.save(createRequest);
        NfluxResponse originalDevice = nfluxService.findDeviceById(deviceId);
        
        // NULL로만 구성된 업데이트 요청
        NfluxUpdateRequest nullUpdateRequest = new NfluxUpdateRequest(
                null, null, null, null, null
        );
        
        // when
        nfluxService.update(deviceId, nullUpdateRequest);
        
        // then
        NfluxResponse updatedDevice = nfluxService.findDeviceById(deviceId);
        
        // 업데이트 전후 모든 필드가 동일해야 함
        assertThat(updatedDevice.name()).isEqualTo(originalDevice.name());
        assertThat(updatedDevice.code()).isEqualTo(originalDevice.code());
        assertThat(updatedDevice.description()).isEqualTo(originalDevice.description());
        assertThat(updatedDevice.categoryId()).isEqualTo(originalDevice.categoryId());
    }
    
    @Test
    @DisplayName("엔티티 영속성 테스트")
    void entityPersistenceTest() {
        // given
        Long deviceId = nfluxService.save(createRequest);
        
        // when
        // 엔티티 매니저 캐시 초기화
        entityManager.flush();
        entityManager.clear();
        
        // 디바이스 다시 로드
        Nflux device = nfluxService.findById(deviceId);
        
        // then
        assertThat(device).isNotNull();
        assertThat(device.getId()).isEqualTo(deviceId);
        assertThat(device.getName()).isEqualTo(createRequest.name());
        assertThat(device.getDeviceCode()).isEqualTo(createRequest.code());
        assertThat(device.getDescription()).isEqualTo(createRequest.description());
    }

    @Test
    @DisplayName("특수 문자가 포함된 디바이스 코드 처리 테스트")
    void deviceCodeWithSpecialCharactersTest() {
        // given
        NfluxCreateRequest requestWithSpecialChars = new NfluxCreateRequest(
                category.getId(), asset.getId(), "특수문자 코드 디바이스", "TEST!@#$%^&*()", "특수문자 코드 테스트"
        );
        
        // when
        Long deviceId = nfluxService.save(requestWithSpecialChars);
        
        // then
        NfluxResponse device = nfluxService.findDeviceById(deviceId);
        assertThat(device.code()).isEqualTo("TEST!@#$%^&*()");
    }
    
    @Test
    @DisplayName("매우 긴 이름과 설명으로 디바이스 생성 테스트")
    void deviceWithVeryLongNameAndDescriptionTest() {
        // given
        String longName = "a".repeat(255); // 최대 길이의 이름
        String longDescription = "b".repeat(255); // 매우 긴 설명
        
        NfluxCreateRequest requestWithLongValues = new NfluxCreateRequest(
                category.getId(), asset.getId(), longName, "LONG001", longDescription
        );
        
        // when
        Long deviceId = nfluxService.save(requestWithLongValues);
        
        // then
        NfluxResponse device = nfluxService.findDeviceById(deviceId);
        assertThat(device.name()).isEqualTo(longName);
        assertThat(device.description()).isEqualTo(longDescription);
    }
    
    @Test
    @DisplayName("스테이션 ID가 NULL인 경우 findByStationIdGroupByCategory 테스트")
    void findByNullStationIdTest() {
        // when & then
        assertThrows(InvalidDataAccessApiUsageException.class, () ->
            nfluxService.findByStationIdGroupByCategory(null)
        );
    }
    
    @Test
    @DisplayName("NfluxCategory의 contextPath 테스트")
    void nfluxCategoryContextPathTest() {
        // given
        // 1. NfluxCategory 생성
        NfluxCategory nfluxCategory = (NfluxCategory) deviceCategoryRepository.save(
                NfluxCategory.nfluxBuilder()
                .name("Context 테스트 카테고리")
                .contextPath("/custom-context")
                .build());
        
        // 2. 디바이스 생성
        NfluxCreateRequest requestWithNfluxCategory = new NfluxCreateRequest(
                nfluxCategory.getId(), asset.getId(), "Context 테스트 디바이스", "CTX001", "Context 테스트 설명"
        );
        Long deviceId = nfluxService.save(requestWithNfluxCategory);
        
        // 3. 스테이션 생성
        Station station = stationRepository.save(Station.builder()
                .name("Context 테스트 스테이션")
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
        List<NfluxCategoryGroupResponse> result = nfluxService.findByStationIdGroupByCategory(station.getId());
        
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
        Long deviceId1 = nfluxService.save(createRequest);
        
        // 2. 두 번째 디바이스 생성
        NfluxCreateRequest request2 = new NfluxCreateRequest(
                category.getId(), asset.getId(), "두 번째 디바이스", "TEST002", "두 번째 테스트 디바이스"
        );
        Long deviceId2 = nfluxService.save(request2);
        
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
                category.getId(), null, "에셋 없는 디바이스", "NO_ASSET", "에셋 없는 테스트 디바이스"
        );
        
        // when
        Long deviceId = nfluxService.save(requestWithoutAsset);
        
        // then
        NfluxResponse device = nfluxService.findDeviceById(deviceId);
    }

    @Test
    @DisplayName("디바이스 생성-업데이트-삭제 전체 라이프사이클 테스트")
    void deviceLifecycleTest() {
        // 1. 디바이스 생성
        Long deviceId = nfluxService.save(createRequest);
        NfluxResponse createdDevice = nfluxService.findDeviceById(deviceId);
        assertThat(createdDevice.name()).isEqualTo("테스트 디바이스");
        
        // 2. 디바이스 업데이트
        NfluxUpdateRequest updateRequest = new NfluxUpdateRequest(
                null, null, "업데이트된 디바이스", "UPDATED", "업데이트된 설명"
        );
        nfluxService.update(deviceId, updateRequest);
        
        NfluxResponse updatedDevice = nfluxService.findDeviceById(deviceId);
        assertThat(updatedDevice.name()).isEqualTo("업데이트된 디바이스");
        assertThat(updatedDevice.code()).isEqualTo("UPDATED");
        
        // 3. 새 카테고리 생성 및 할당
        DeviceCategory newCategory = deviceCategoryRepository.save(DeviceCategory.builder()
                .name("새 라이프사이클 카테고리")
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
    @DisplayName("디바이스 삭제 시 모든 관계가 정리된 후 삭제되는지 테스트")
    void delete_ClearsAllRelationsBeforeDelete() {
        // given
        // 1. 카테고리 생성
        DeviceCategory category = deviceCategoryRepository.save(DeviceCategory.builder()
                .name("관계 정리 테스트 카테고리")
                .build());
        
        // 2. 피처 생성
        String featureId = UUID.randomUUID().toString();
        Feature feature = featureRepository.save(Feature.builder()
                .id(featureId)
                .position(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .asset(asset)
                .build());
        
        // 3. 디바이스 생성 및 관계 설정
        Nflux device = Nflux.builder()
                .name("관계 정리 테스트 디바이스")
                .code("RELATION001")
                .description("관계 정리 테스트 설명")
                .category(category)
                .build();
        
        Long deviceId = deviceRepository.save(device).getId();
        
        // 디바이스에 피처 할당
        nfluxService.assignFeatureToNflux(deviceId, featureId);
        
        // 모든 관계가 설정되었는지 확인
        Nflux savedDevice = deviceRepository.findById(deviceId).orElseThrow();
        assertThat(savedDevice.getCategory()).isNotNull();
        assertThat(savedDevice.getFeature()).isNotNull();
        
        // when
        nfluxService.delete(deviceId);
        
        // then
        // 1. 디바이스가 삭제되었는지 확인
        assertThrows(CustomException.class, () -> nfluxService.findById(deviceId));
        
        // 2. 피처에서 디바이스 참조가 제거되었는지 확인
        Feature updatedFeature = featureRepository.findById(featureId).orElseThrow();
        assertThat(updatedFeature.getDevice()).isNull();
    }
    
    @Test
    @DisplayName("디바이스 clearAllRelations 메소드 단위 테스트")
    void clearAllRelations_RemovesAllRelationsFromDevice() {
        // given
        // 1. 카테고리 생성
        DeviceCategory category = deviceCategoryRepository.save(DeviceCategory.builder()
                .name("clearAllRelations 테스트 카테고리")
                .build());
        
        // 2. 피처 생성
        String featureId = UUID.randomUUID().toString();
        Feature feature = featureRepository.save(Feature.builder()
                .id(featureId)
                .position(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .asset(asset)
                .build());
        
        // 3. 디바이스 생성 및 관계 설정
        Nflux device = Nflux.builder()
                .name("clearAllRelations 테스트 디바이스")
                .code("CLEAR001")
                .description("clearAllRelations 테스트 설명")
                .category(category)
                .build();
        
        Long deviceId = deviceRepository.save(device).getId();
        
        // 디바이스에 피처 할당
        nfluxService.assignFeatureToNflux(deviceId, featureId);
        
        // 모든 관계가 설정되었는지 확인
        Nflux savedDevice = deviceRepository.findById(deviceId).orElseThrow();
        assertThat(savedDevice.getCategory()).isNotNull();
        assertThat(savedDevice.getFeature()).isNotNull();
        
        // when
        // clearAllRelations 메소드 직접 호출
        savedDevice.clearAllRelations();
        deviceRepository.save(savedDevice);
        
        // then
        // 디바이스에서 모든 관계가 제거되었는지 확인
        Nflux updatedDevice = deviceRepository.findById(deviceId).orElseThrow();
        assertThat(updatedDevice.getCategory()).isNull();
        assertThat(updatedDevice.getFeature()).isNull();
        
        // 피처에서 디바이스 참조가 제거되었는지 확인
        Feature updatedFeature = featureRepository.findById(featureId).orElseThrow();
        assertThat(updatedFeature.getDevice()).isNull();
    }
    
    @Test
    @DisplayName("디바이스 삭제 시 관계 제거 실패하면 예외 발생")
    void delete_ThrowsExceptionWhenRelationRemovalFails() {
        // given
        // 1. 디바이스 생성
        Nflux device = Nflux.builder()
                .name("예외 테스트 디바이스")
                .code("EXCEPTION001")
                .description("예외 테스트 설명")
                .build();
        
        Long deviceId = deviceRepository.save(device).getId();
        
        // 디바이스 스파이 생성
        Nflux spyDevice = Mockito.spy(device);
        
        // clearAllRelations 호출 시 예외 발생하도록 설정
        Mockito.doThrow(new RuntimeException("관계 제거 실패")).when(spyDevice).clearAllRelations();
        
        // 목 레포지토리 설정
        NfluxRepository mockRepo = Mockito.mock(NfluxRepository.class);
        Mockito.when(mockRepo.findById(deviceId)).thenReturn(java.util.Optional.of(spyDevice));
        
        // 원본 레포지토리 저장
        NfluxRepository originalRepo = (NfluxRepository) ReflectionTestUtils.getField(
                nfluxService, "repository");
        
        // 목 주입
        ReflectionTestUtils.setField(nfluxService, "repository", mockRepo);
        
        try {
            // when & then
            assertThrows(RuntimeException.class, () -> nfluxService.delete(deviceId));
            
        } finally {
            // 원래 레포지토리 복원
            ReflectionTestUtils.setField(nfluxService, "repository", originalRepo);
        }
    }
}