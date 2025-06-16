package com.pluxity.domains.device.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pluxity.SasangApplication;
import com.pluxity.asset.entity.Asset;
import com.pluxity.asset.repository.AssetRepository;
import com.pluxity.facility.station.Station;
import com.pluxity.facility.station.StationRepository;
import com.pluxity.feature.entity.Spatial;
import com.pluxity.feature.repository.FeatureRepository;
import com.pluxity.feature.service.FeatureService;
import com.pluxity.label3d.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(classes = SasangApplication.class)
@Transactional
class Label3DServiceTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    Label3DService label3DService;

    @Autowired
    Label3DRepository label3DRepository;

    @Autowired
    AssetRepository assetRepository;

    @Autowired
    FeatureRepository featureRepository;

    @Autowired
    FeatureService featureService;

    @Autowired
    StationRepository stationRepository;

    private Asset asset;
    private Station station;
    private Spatial defaultPosition;
    private Spatial defaultRotation;
    private Spatial defaultScale;

    @BeforeEach
    void setUp() {
        // 에셋 생성
        asset = assetRepository.save(Asset.builder()
                .code("TEST-ASSET")
                .name("테스트 에셋")
                .build());

        // 스테이션 생성
        station = stationRepository.save(Station.builder()
                .name("테스트 스테이션")
                .description("테스트용 스테이션입니다.")
                .build());

        // 기본 Spatial 값들
        defaultPosition = Spatial.builder().x(0.0).y(0.0).z(0.0).build();
        defaultRotation = Spatial.builder().x(0.0).y(0.0).z(0.0).build();
        defaultScale = Spatial.builder().x(1.0).y(1.0).z(1.0).build();
    }

    private Label3DCreateRequest createDefaultRequest(String displayText) {
        return new Label3DCreateRequest(
                UUID.randomUUID().toString(),
                displayText,
                station.getId(),
                "FLOOR-01",
                defaultPosition,
                defaultRotation,
                defaultScale
        );
    }

    private Label3DCreateRequest createRequestWithSpatial(String displayText, Spatial position, Spatial rotation, Spatial scale) {
        return new Label3DCreateRequest(
                UUID.randomUUID().toString(),
                displayText,
                station.getId(),
                "FLOOR-01",
                position,
                rotation,
                scale
        );
    }

    private void assertSpatialEquals(Spatial actual, Spatial expected) {
        assertThat(actual.getX()).isEqualTo(expected.getX());
        assertThat(actual.getY()).isEqualTo(expected.getY());
        assertThat(actual.getZ()).isEqualTo(expected.getZ());
    }

    @Test
    @DisplayName("Label3D 저장 테스트")
    void saveLabel3DTest() {
        // given
        Label3DCreateRequest request = createDefaultRequest("테스트 텍스트 내용");

        // when
        Label3DResponse response = label3DService.createLabel3D(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.displayText()).isEqualTo("테스트 텍스트 내용");
        assertThat(response.floorId()).isEqualTo("FLOOR-01");
        
        // Spatial 객체는 각 필드를 개별적으로 비교
        assertSpatialEquals(response.position(), defaultPosition);
        assertSpatialEquals(response.rotation(), defaultRotation);
        assertSpatialEquals(response.scale(), defaultScale);
    }

    @Test
    @DisplayName("Label3D 단일 조회 테스트")
    void findLabel3DTest() {
        // given
        Label3DCreateRequest request = createDefaultRequest("조회 테스트 텍스트");
        Label3DResponse savedResponse = label3DService.createLabel3D(request);

        // when
        Label3DResponse response = label3DService.getLabel3DById(savedResponse.id());

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(savedResponse.id());
        assertThat(response.displayText()).isEqualTo("조회 테스트 텍스트");
        assertThat(response.floorId()).isEqualTo("FLOOR-01");
    }

    @Test
    @DisplayName("존재하지 않는 Label3D 조회시 예외 발생")
    void notFoundLabel3DTest() {
        // given
        String nonExistentId = UUID.randomUUID().toString();

        // when & then
        assertThrows(EntityNotFoundException.class, () -> 
            label3DService.getLabel3DById(nonExistentId)
        );
    }

    @Test
    @DisplayName("Label3D 수정 테스트")
    void updateLabel3DTest() {
        // given
        Label3DCreateRequest createRequest = createDefaultRequest("원본 텍스트");
        Label3DResponse savedResponse = label3DService.createLabel3D(createRequest);

        Spatial newPosition = Spatial.builder().x(10.0).y(20.0).z(30.0).build();
        Spatial newRotation = Spatial.builder().x(45.0).y(90.0).z(180.0).build();
        Spatial newScale = Spatial.builder().x(2.0).y(2.0).z(2.0).build();
        
        Label3DUpdateRequest updateRequest = new Label3DUpdateRequest(
                newPosition, newRotation, newScale
        );

        // when
        Label3DResponse updatedResponse = label3DService.updateLabel3D(savedResponse.id(), updateRequest);

        // then
        assertThat(updatedResponse.id()).isEqualTo(savedResponse.id());
        assertThat(updatedResponse.displayText()).isEqualTo("원본 텍스트"); // displayText는 변경되지 않음
        
        // Spatial 객체는 각 필드를 개별적으로 비교
        assertSpatialEquals(updatedResponse.position(), newPosition);
        assertSpatialEquals(updatedResponse.rotation(), newRotation);
        assertSpatialEquals(updatedResponse.scale(), newScale);
    }

    @Test
    @DisplayName("NULL Spatial로 Label3D 수정 시 변경되지 않음")
    void updateLabel3DWithNullTest() {
        // given
        Label3DCreateRequest createRequest = createDefaultRequest("원본 텍스트");
        Label3DResponse savedResponse = label3DService.createLabel3D(createRequest);

        Label3DUpdateRequest updateRequest = new Label3DUpdateRequest(null, null, null);

        // when
        Label3DResponse updatedResponse = label3DService.updateLabel3D(savedResponse.id(), updateRequest);

        // then
        assertThat(updatedResponse.displayText()).isEqualTo("원본 텍스트");
        
        // Spatial 객체는 각 필드를 개별적으로 비교 - 변경되지 않음
        assertSpatialEquals(updatedResponse.position(), defaultPosition);
        assertSpatialEquals(updatedResponse.rotation(), defaultRotation);
        assertSpatialEquals(updatedResponse.scale(), defaultScale);
    }

    @Test
    @DisplayName("Label3D 목록 조회 테스트")
    void getAllLabel3DsTest() {
        // given
        Label3DCreateRequest request1 = createDefaultRequest("첫 번째 텍스트");
        Label3DCreateRequest request2 = createDefaultRequest("두 번째 텍스트");
        
        label3DService.createLabel3D(request1);
        label3DService.createLabel3D(request2);

        // when
        List<Label3DResponse> responses = label3DService.getAllLabel3Ds();

        // then
        assertThat(responses).hasSizeGreaterThanOrEqualTo(2);
        assertThat(responses.stream().map(Label3DResponse::displayText))
                .contains("첫 번째 텍스트", "두 번째 텍스트");
    }

    @Test
    @DisplayName("Facility ID로 Label3D 조회 테스트")
    void getLabel3DsByFacilityIdTest() {
        // given
        Label3DCreateRequest request1 = createDefaultRequest("스테이션 텍스트");
        label3DService.createLabel3D(request1);

        // 다른 스테이션 생성
        Station otherStation = stationRepository.save(Station.builder()
                .name("다른 스테이션")
                .description("다른 테스트 스테이션")
                .build());

        Label3DCreateRequest request2 = new Label3DCreateRequest(
                UUID.randomUUID().toString(),
                "다른 스테이션 텍스트",
                otherStation.getId(),
                "FLOOR-02",
                defaultPosition,
                defaultRotation,
                defaultScale
        );
        label3DService.createLabel3D(request2);

        // when
        List<Label3DResponse> responses = label3DService.getLabel3DsByFacilityId(station.getId().toString());

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().displayText()).isEqualTo("스테이션 텍스트");
        assertThat(responses.getFirst().floorId()).isEqualTo("FLOOR-01");
    }

    @Test
    @DisplayName("존재하지 않는 Facility ID로 조회시 빈 목록 반환")
    void getLabel3DsByNonExistentFacilityIdTest() {
        // given
        String nonExistentFacilityId = "999999";

        // when
        List<Label3DResponse> responses = label3DService.getLabel3DsByFacilityId(nonExistentFacilityId);

        // then
        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("Label3D 삭제 테스트")
    void deleteLabel3DTest() {
        // given
        Label3DCreateRequest request = createDefaultRequest("삭제될 텍스트");
        Label3DResponse savedResponse = label3DService.createLabel3D(request);

        // when
        label3DService.deleteLabel3D(savedResponse.id());

        // then
        assertThrows(EntityNotFoundException.class, () -> 
            label3DService.getLabel3DById(savedResponse.id())
        );
    }

    @Test
    @DisplayName("존재하지 않는 Facility ID로 Label3D 생성시 예외 발생")
    void createLabel3DWithNonExistentFacilityTest() {
        // given
        Label3DCreateRequest request = new Label3DCreateRequest(
                UUID.randomUUID().toString(),
                "잘못된 Facility 텍스트",
                999999L, // 존재하지 않는 facilityId
                "FLOOR-01",
                defaultPosition,
                defaultRotation,
                defaultScale
        );

        // when & then
        assertThrows(Exception.class, () ->
            label3DService.createLabel3D(request)
        );
    }

    @Test
    @DisplayName("Label3D 생성-수정-삭제 전체 라이프사이클 테스트")
    void label3DLifecycleTest() {
        // 1. Label3D 생성
        Label3DCreateRequest createRequest = createDefaultRequest("라이프사이클 테스트");
        Label3DResponse createdResponse = label3DService.createLabel3D(createRequest);
        assertThat(createdResponse.displayText()).isEqualTo("라이프사이클 테스트");

        // 2. Label3D 수정 (Spatial 정보 변경)
        Spatial newPosition = Spatial.builder().x(100.0).y(200.0).z(300.0).build();
        Label3DUpdateRequest updateRequest = new Label3DUpdateRequest(
                newPosition, defaultRotation, defaultScale
        );
        Label3DResponse updatedResponse = label3DService.updateLabel3D(createdResponse.id(), updateRequest);
        assertSpatialEquals(updatedResponse.position(), newPosition);

        // 3. 기본 정보 확인
        assertThat(updatedResponse.displayText()).isEqualTo("라이프사이클 테스트");
        assertThat(updatedResponse.floorId()).isEqualTo("FLOOR-01");

        // 4. Label3D 삭제
        label3DService.deleteLabel3D(createdResponse.id());

        // 5. 삭제 확인
        assertThrows(EntityNotFoundException.class, () -> 
            label3DService.getLabel3DById(createdResponse.id())
        );
    }

    @Test
    @DisplayName("여러 개의 Label3D 조회 테스트")
    void findMultipleLabel3DsTest() {
        // given
        Label3DCreateRequest request1 = createDefaultRequest("첫 번째 멀티 텍스트");
        Label3DCreateRequest request2 = createDefaultRequest("두 번째 멀티 텍스트");
        Label3DCreateRequest request3 = createDefaultRequest("세 번째 멀티 텍스트");

        Label3DResponse response1 = label3DService.createLabel3D(request1);
        Label3DResponse response2 = label3DService.createLabel3D(request2);
        Label3DResponse response3 = label3DService.createLabel3D(request3);

        // when
        List<Label3DResponse> allLabel3Ds = label3DService.getAllLabel3Ds();

        // then
        assertThat(allLabel3Ds).hasSizeGreaterThanOrEqualTo(3);

        List<String> label3DIds = allLabel3Ds.stream()
                .map(Label3DResponse::id)
                .toList();
        assertThat(label3DIds).contains(
                response1.id(), response2.id(), response3.id()
        );
    }

    @Test
    @DisplayName("다양한 Spatial 값으로 Label3D 생성 테스트")
    void createLabel3DWithVariousSpatialValuesTest() {
        // given
        Spatial customPosition = Spatial.builder().x(100.0).y(-50.0).z(200.0).build();
        Spatial customRotation = Spatial.builder().x(45.0).y(90.0).z(180.0).build();
        Spatial customScale = Spatial.builder().x(0.5).y(2.0).z(1.5).build();
        
        Label3DCreateRequest request = createRequestWithSpatial(
                "다양한 Spatial 값 테스트", customPosition, customRotation, customScale
        );

        // when
        Label3DResponse response = label3DService.createLabel3D(request);

        // then
        assertThat(response.displayText()).isEqualTo("다양한 Spatial 값 테스트");
        
        // Spatial 객체는 각 필드를 개별적으로 비교
        assertSpatialEquals(response.position(), customPosition);
        assertSpatialEquals(response.rotation(), customRotation);
        assertSpatialEquals(response.scale(), customScale);
    }

    @Test
    @DisplayName("Response 필드 검증 테스트")
    void responseFieldValidationTest() {
        // given
        Spatial customPosition = Spatial.builder().x(10.0).y(20.0).z(30.0).build();
        Spatial customRotation = Spatial.builder().x(45.0).y(90.0).z(135.0).build();
        Spatial customScale = Spatial.builder().x(1.5).y(2.0).z(0.8).build();
        
        Label3DCreateRequest request = createRequestWithSpatial(
                "필드 검증 테스트", customPosition, customRotation, customScale
        );

        // when
        Label3DResponse response = label3DService.createLabel3D(request);

        // then
        assertThat(response.id()).isNotNull();
        assertThat(response.displayText()).isEqualTo("필드 검증 테스트");
        assertThat(response.floorId()).isEqualTo("FLOOR-01");
        
        // Spatial 객체는 각 필드를 개별적으로 비교
        assertSpatialEquals(response.position(), customPosition);
        assertSpatialEquals(response.rotation(), customRotation);
        assertSpatialEquals(response.scale(), customScale);
    }
}