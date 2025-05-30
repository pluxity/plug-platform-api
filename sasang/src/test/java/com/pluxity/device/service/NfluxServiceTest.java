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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
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
        assertThat(response.feature()).isNotNull();
        assertThat(response.feature().id()).isEqualTo(newFeature.getId());

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
        assertThat(response.feature()).isNull();

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

    @Test
    @DisplayName("디바이스에 피처가 할당되지 않았을 때 제거 시도 시 예외 발생")
    void removeFeatureFromNflux_NoFeatureAssigned_ThrowsException() {
        // given
        NfluxCreateRequest requestWithoutFeature = new NfluxCreateRequest(
            null, // Feature 없음
            category.getId(),
            "피처 없는 디바이스",
            "TEST003",
            "피처가 없는 테스트용 디바이스입니다."
        );
        Long deviceId = nfluxService.save(requestWithoutFeature);

        // when & then
        assertThrows(CustomException.class, () -> {
            nfluxService.removeFeatureFromNflux(deviceId);
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
}