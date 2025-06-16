package com.pluxity.domains.device.service;

import com.pluxity.SasangApplication;
import com.pluxity.asset.entity.Asset;
import com.pluxity.asset.repository.AssetRepository;
import com.pluxity.domains.device.dto.Label3DCreateRequest;
import com.pluxity.domains.device.dto.Label3DResponse;
import com.pluxity.domains.device.dto.Label3DUpdateRequest;
import com.pluxity.domains.device.repository.Label3DRepository;
import com.pluxity.facility.station.Station;
import com.pluxity.facility.station.StationRepository;
import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.entity.Spatial;
import com.pluxity.feature.repository.FeatureRepository;
import com.pluxity.feature.service.FeatureService;
import com.pluxity.global.exception.CustomException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    private Feature feature;
    private Station station;

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

        // 피처 생성
        String uniqueFeatureId = UUID.randomUUID().toString();
        feature = featureRepository.save(Feature.builder()
                .id(uniqueFeatureId)
                .position(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .asset(asset)
                .facility(station)
                .build());
    }

    private Feature createUniqueFeature() {
        String uniqueFeatureId = UUID.randomUUID().toString();
        return featureRepository.save(Feature.builder()
                .id(uniqueFeatureId)
                .position(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .asset(asset)
                .facility(station)
                .build());
    }

    @Test
    @DisplayName("Label3D 저장 테스트")
    void saveLabel3DTest() {
        // given
        Label3DCreateRequest request = new Label3DCreateRequest(
                "테스트 텍스트 내용",
                feature.getId()
        );

        // when
        Label3DResponse response = label3DService.createLabel3D(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.displayText()).isEqualTo("테스트 텍스트 내용");
        assertThat(response.featureId()).isEqualTo(feature.getId());
    }

    @Test
    @DisplayName("Feature 없이 Label3D 생성 테스트")
    void createLabel3DWithoutFeatureTest() {
        // given
        Label3DCreateRequest request = new Label3DCreateRequest(
                "Feature 없는 텍스트",
                null
        );

        // when
        Label3DResponse response = label3DService.createLabel3D(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.displayText()).isEqualTo("Feature 없는 텍스트");
        assertThat(response.featureId()).isNull();
    }

    @Test
    @DisplayName("Label3D 단일 조회 테스트")
    void findLabel3DTest() {
        // given
        Label3DCreateRequest request = new Label3DCreateRequest(
                "조회 테스트 텍스트",
                feature.getId()
        );
        Label3DResponse savedResponse = label3DService.createLabel3D(request);

        // when
        Label3DResponse response = label3DService.getLabel3DById(savedResponse.id());

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(savedResponse.id());
        assertThat(response.displayText()).isEqualTo("조회 테스트 텍스트");
        assertThat(response.featureId()).isEqualTo(feature.getId());
    }

    @Test
    @DisplayName("존재하지 않는 Label3D 조회시 예외 발생")
    void notFoundLabel3DTest() {
        // given
        Long nonExistentId = 999999L;

        // when & then
        assertThrows(EntityNotFoundException.class, () -> 
            label3DService.getLabel3DById(nonExistentId)
        );
    }

    @Test
    @DisplayName("Label3D 수정 테스트")
    void updateLabel3DTest() {
        // given
        Label3DCreateRequest createRequest = new Label3DCreateRequest(
                "원본 텍스트",
                feature.getId()
        );
        Label3DResponse savedResponse = label3DService.createLabel3D(createRequest);

        Label3DUpdateRequest updateRequest = new Label3DUpdateRequest("수정된 텍스트");

        // when
        Label3DResponse updatedResponse = label3DService.updateLabel3D(savedResponse.id(), updateRequest);

        // then
        assertThat(updatedResponse.id()).isEqualTo(savedResponse.id());
        assertThat(updatedResponse.displayText()).isEqualTo("수정된 텍스트");
        assertThat(updatedResponse.featureId()).isNotNull();
    }

    @Test
    @DisplayName("NULL로 Label3D 수정 시 변경되지 않음")
    void updateLabel3DWithNullTest() {
        // given
        Label3DCreateRequest createRequest = new Label3DCreateRequest(
                "원본 텍스트",
                feature.getId()
        );
        Label3DResponse savedResponse = label3DService.createLabel3D(createRequest);

        Label3DUpdateRequest updateRequest = new Label3DUpdateRequest(null);

        // when
        Label3DResponse updatedResponse = label3DService.updateLabel3D(savedResponse.id(), updateRequest);

        // then
        assertThat(updatedResponse.displayText()).isEqualTo("원본 텍스트"); // 변경되지 않음
    }

    @Test
    @DisplayName("Label3D 목록 조회 테스트")
    void getAllLabel3DsTest() {
        // given
        Feature feature1 = createUniqueFeature();
        Feature feature2 = createUniqueFeature();
        
        Label3DCreateRequest request1 = new Label3DCreateRequest(
                "첫 번째 텍스트", feature1.getId()
        );
        Label3DCreateRequest request2 = new Label3DCreateRequest(
                "두 번째 텍스트", feature2.getId()
        );
        
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
        Feature feature1 = createUniqueFeature();
        Label3DCreateRequest request1 = new Label3DCreateRequest(
                "스테이션 텍스트", feature1.getId()
        );
        label3DService.createLabel3D(request1);

        // 다른 스테이션의 Feature
        Station otherStation = stationRepository.save(Station.builder()
                .name("다른 스테이션")
                .description("다른 테스트 스테이션")
                .build());

        String otherFeatureId = UUID.randomUUID().toString();
        Feature otherFeature = featureRepository.save(Feature.builder()
                .id(otherFeatureId)
                .position(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .asset(asset)
                .facility(otherStation)
                .build());

        Label3DCreateRequest request2 = new Label3DCreateRequest(
                "다른 스테이션 텍스트", otherFeature.getId()
        );
        label3DService.createLabel3D(request2);

        // when
        List<Label3DResponse> responses = label3DService.getLabel3DsByFacilityId(station.getId().toString());

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().displayText()).isEqualTo("스테이션 텍스트");
        assertThat(responses.getFirst().featureId()).isEqualTo(feature1.getId());
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
        Label3DCreateRequest request = new Label3DCreateRequest(
                "삭제될 텍스트", feature.getId()
        );
        Label3DResponse savedResponse = label3DService.createLabel3D(request);

        // when
        label3DService.deleteLabel3D(savedResponse.id());

        // then
        assertThrows(EntityNotFoundException.class, () -> 
            label3DService.getLabel3DById(savedResponse.id())
        );
    }

    @Test
    @DisplayName("존재하지 않는 Feature ID로 Label3D 생성시 예외 발생")
    void createLabel3DWithNonExistentFeatureTest() {
        // given
        String nonExistentFeatureId = "non-existent-feature";
        Label3DCreateRequest request = new Label3DCreateRequest(
                "잘못된 Feature 텍스트", nonExistentFeatureId
        );

        // when & then
        assertThrows(CustomException.class, () ->
            label3DService.createLabel3D(request)
        );
    }

    @Test
    @DisplayName("Label3D 생성-수정-삭제 전체 라이프사이클 테스트")
    void label3DLifecycleTest() {
        // 1. Label3D 생성
        Feature uniqueFeature = createUniqueFeature();
        Label3DCreateRequest createRequest = new Label3DCreateRequest(
                "라이프사이클 테스트", uniqueFeature.getId()
        );
        Label3DResponse createdResponse = label3DService.createLabel3D(createRequest);
        assertThat(createdResponse.displayText()).isEqualTo("라이프사이클 테스트");

        // 2. Label3D 수정
        Label3DUpdateRequest updateRequest = new Label3DUpdateRequest("수정된 라이프사이클 테스트");
        Label3DResponse updatedResponse = label3DService.updateLabel3D(createdResponse.id(), updateRequest);
        assertThat(updatedResponse.displayText()).isEqualTo("수정된 라이프사이클 테스트");

        // 3. Feature와의 연관관계 확인
        assertThat(updatedResponse.featureId()).isEqualTo(uniqueFeature.getId());

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
        Feature feature1 = createUniqueFeature();
        Feature feature2 = createUniqueFeature();
        Feature feature3 = createUniqueFeature();
        
        Label3DCreateRequest request1 = new Label3DCreateRequest(
                "첫 번째 멀티 텍스트", feature1.getId()
        );
        Label3DCreateRequest request2 = new Label3DCreateRequest(
                "두 번째 멀티 텍스트", feature2.getId()
        );
        Label3DCreateRequest request3 = new Label3DCreateRequest(
                "세 번째 멀티 텍스트", feature3.getId()
        );

        Label3DResponse response1 = label3DService.createLabel3D(request1);
        Label3DResponse response2 = label3DService.createLabel3D(request2);
        Label3DResponse response3 = label3DService.createLabel3D(request3);

        // when
        List<Label3DResponse> allLabel3Ds = label3DService.getAllLabel3Ds();

        // then
        assertThat(allLabel3Ds).hasSizeGreaterThanOrEqualTo(3);

        List<Long> label3DIds = allLabel3Ds.stream()
                .map(Label3DResponse::id)
                .toList();
        assertThat(label3DIds).contains(
                response1.id(), response2.id(), response3.id()
        );
    }

    @Test
    @DisplayName("동일한 Feature를 가진 여러 Label3D 생성 불가 테스트")
    void multipleLabel3DsCannotShareSameFeatureTest() {
        // given
        Feature uniqueFeature = createUniqueFeature();
        Label3DCreateRequest request1 = new Label3DCreateRequest(
                "첫 번째 텍스트", uniqueFeature.getId()
        );
        label3DService.createLabel3D(request1);

        Label3DCreateRequest request2 = new Label3DCreateRequest(
                "두 번째 텍스트", uniqueFeature.getId()
        );

        // when & then
        // 같은 Feature를 사용하려고 하면 unique constraint 에러가 발생
        assertThrows(Exception.class, () -> 
            label3DService.createLabel3D(request2)
        );
    }

    @Test
    @DisplayName("Response 필드 검증 테스트")
    void responseFieldValidationTest() {
        // given
        Label3DCreateRequest request = new Label3DCreateRequest(
                "필드 검증 테스트", feature.getId()
        );

        // when
        Label3DResponse response = label3DService.createLabel3D(request);

        // then
        assertThat(response.id()).isNotNull();
        assertThat(response.displayText()).isEqualTo("필드 검증 테스트");
        assertThat(response.featureId()).isEqualTo(feature.getId());
        assertThat(response.floorId()).isEqualTo(feature.getFloorId());
        assertThat(response.position()).isEqualTo(feature.getPosition());
        assertThat(response.rotation()).isEqualTo(feature.getRotation());
        assertThat(response.scale()).isEqualTo(feature.getScale());
    }
}