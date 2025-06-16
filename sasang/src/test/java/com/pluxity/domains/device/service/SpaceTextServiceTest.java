package com.pluxity.domains.device.service;

import com.pluxity.SasangApplication;
import com.pluxity.asset.entity.Asset;
import com.pluxity.asset.repository.AssetRepository;
import com.pluxity.domains.device.dto.SpaceTextCreateRequest;
import com.pluxity.domains.device.dto.SpaceTextResponse;
import com.pluxity.domains.device.dto.SpaceTextUpdateRequest;
import com.pluxity.domains.device.entity.SpaceText;
import com.pluxity.domains.device.repository.SpaceTextRepository;
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
class SpaceTextServiceTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    SpaceTextService spaceTextService;

    @Autowired
    SpaceTextRepository spaceTextRepository;

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

    private String generateUniqueId(String prefix) {
        return "TEST-" + prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
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
    @DisplayName("SpaceText 저장 테스트")
    void saveSpaceTextTest() {
        // given
        SpaceTextCreateRequest request = new SpaceTextCreateRequest(
                "테스트 텍스트 내용",
                feature.getId()
        );

        // when
        SpaceTextResponse response = spaceTextService.createSpaceText(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.textContent()).isEqualTo("테스트 텍스트 내용");
        assertThat(response.feature()).isNotNull();
        assertThat(response.feature().id()).isEqualTo(feature.getId());
    }

    @Test
    @DisplayName("Feature 없이 SpaceText 생성 테스트")
    void createSpaceTextWithoutFeatureTest() {
        // given
        SpaceTextCreateRequest request = new SpaceTextCreateRequest(
                "Feature 없는 텍스트",
                null
        );

        // when
        SpaceTextResponse response = spaceTextService.createSpaceText(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.textContent()).isEqualTo("Feature 없는 텍스트");
        assertThat(response.feature()).isNull();
    }

    @Test
    @DisplayName("SpaceText 단일 조회 테스트")
    void findSpaceTextTest() {
        // given
        SpaceTextCreateRequest request = new SpaceTextCreateRequest(
                "조회 테스트 텍스트",
                feature.getId()
        );
        SpaceTextResponse savedResponse = spaceTextService.createSpaceText(request);

        // when
        SpaceTextResponse response = spaceTextService.getSpaceTextById(savedResponse.id());

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(savedResponse.id());
        assertThat(response.textContent()).isEqualTo("조회 테스트 텍스트");
        assertThat(response.feature()).isNotNull();
        assertThat(response.feature().id()).isEqualTo(feature.getId());
    }

    @Test
    @DisplayName("존재하지 않는 SpaceText 조회시 예외 발생")
    void notFoundSpaceTextTest() {
        // given
        String nonExistentId = "non-existent-id";

        // when & then
        assertThrows(EntityNotFoundException.class, () -> 
            spaceTextService.getSpaceTextById(nonExistentId)
        );
    }

    @Test
    @DisplayName("SpaceText 수정 테스트")
    void updateSpaceTextTest() {
        // given
        SpaceTextCreateRequest createRequest = new SpaceTextCreateRequest(
                "원본 텍스트",
                feature.getId()
        );
        SpaceTextResponse savedResponse = spaceTextService.createSpaceText(createRequest);

        SpaceTextUpdateRequest updateRequest = new SpaceTextUpdateRequest(
                null, // name field
                "수정된 텍스트" // textContent field
        );

        // when
        SpaceTextResponse updatedResponse = spaceTextService.updateSpaceText(savedResponse.id(), updateRequest);

        // then
        assertThat(updatedResponse.id()).isEqualTo(savedResponse.id());
        assertThat(updatedResponse.textContent()).isEqualTo("수정된 텍스트");
        assertThat(updatedResponse.feature()).isNotNull();
    }

    @Test
    @DisplayName("NULL로 SpaceText 수정 시 변경되지 않음")
    void updateSpaceTextWithNullTest() {
        // given
        SpaceTextCreateRequest createRequest = new SpaceTextCreateRequest(
                "원본 텍스트",
                feature.getId()
        );
        SpaceTextResponse savedResponse = spaceTextService.createSpaceText(createRequest);

        SpaceTextUpdateRequest updateRequest = new SpaceTextUpdateRequest(null, null);

        // when
        SpaceTextResponse updatedResponse = spaceTextService.updateSpaceText(savedResponse.id(), updateRequest);

        // then
        assertThat(updatedResponse.textContent()).isEqualTo("원본 텍스트"); // 변경되지 않음
    }

    @Test
    @DisplayName("SpaceText 목록 조회 테스트")
    void getAllSpaceTextsTest() {
        // given
        Feature feature1 = createUniqueFeature();
        Feature feature2 = createUniqueFeature();
        
        SpaceTextCreateRequest request1 = new SpaceTextCreateRequest(
                "첫 번째 텍스트", feature1.getId()
        );
        SpaceTextCreateRequest request2 = new SpaceTextCreateRequest(
                "두 번째 텍스트", feature2.getId()
        );
        
        spaceTextService.createSpaceText(request1);
        spaceTextService.createSpaceText(request2);

        // when
        List<SpaceTextResponse> responses = spaceTextService.getAllSpaceTexts();

        // then
        assertThat(responses).hasSizeGreaterThanOrEqualTo(2);
        assertThat(responses.stream().map(SpaceTextResponse::textContent))
                .contains("첫 번째 텍스트", "두 번째 텍스트");
    }

    @Test
    @DisplayName("Facility ID로 SpaceText 조회 테스트")
    void getSpaceTextsByFacilityIdTest() {
        // given
        // 첫 번째 Feature (station과 연결)
        Feature feature1 = createUniqueFeature();
        SpaceTextCreateRequest request1 = new SpaceTextCreateRequest(
                "스테이션 텍스트", feature1.getId()
        );
        spaceTextService.createSpaceText(request1);

        // 두 번째 Feature (다른 station과 연결)
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

        SpaceTextCreateRequest request2 = new SpaceTextCreateRequest(
                "다른 스테이션 텍스트", otherFeature.getId()
        );
        spaceTextService.createSpaceText(request2);

        // Feature 없는 SpaceText
        SpaceTextCreateRequest request3 = new SpaceTextCreateRequest(
                "Feature 없는 텍스트", null
        );
        spaceTextService.createSpaceText(request3);

        // when
        List<SpaceTextResponse> responses = spaceTextService.getSpaceByFacilityId(station.getId().toString());

        // then
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).textContent()).isEqualTo("스테이션 텍스트");
        assertThat(responses.get(0).feature()).isNotNull();
        assertThat(responses.get(0).feature().id()).isEqualTo(feature1.getId());
    }

    @Test
    @DisplayName("존재하지 않는 Facility ID로 조회시 빈 목록 반환")
    void getSpaceTextsByNonExistentFacilityIdTest() {
        // given
        String nonExistentFacilityId = "999999"; // Long으로 파싱 가능한 형태

        // when
        List<SpaceTextResponse> responses = spaceTextService.getSpaceByFacilityId(nonExistentFacilityId);

        // then
        assertThat(responses).isEmpty();
    }

    @Test
    @DisplayName("SpaceText 삭제 테스트")
    void deleteSpaceTextTest() {
        // given
        SpaceTextCreateRequest request = new SpaceTextCreateRequest(
                "삭제될 텍스트", feature.getId()
        );
        SpaceTextResponse savedResponse = spaceTextService.createSpaceText(request);

        // when
        spaceTextService.deleteSpaceText(savedResponse.id());

        // then
        assertThrows(EntityNotFoundException.class, () -> 
            spaceTextService.getSpaceTextById(savedResponse.id())
        );
    }

    @Test
    @DisplayName("존재하지 않는 SpaceText 삭제시 예외 발생")
    void deleteNonExistentSpaceTextTest() {
        // given
        String nonExistentId = "non-existent-id";

        // when & then
        assertThrows(EntityNotFoundException.class, () -> 
            spaceTextService.deleteSpaceText(nonExistentId)
        );
    }

    @Test
    @DisplayName("존재하지 않는 Feature ID로 SpaceText 생성시 예외 발생")
    void createSpaceTextWithNonExistentFeatureTest() {
        // given
        String nonExistentFeatureId = "non-existent-feature";
        SpaceTextCreateRequest request = new SpaceTextCreateRequest(
                "잘못된 Feature 텍스트", nonExistentFeatureId
        );

        // when & then
        assertThrows(CustomException.class, () ->
            spaceTextService.createSpaceText(request)
        );
    }

    @Test
    @DisplayName("빈 텍스트 내용으로 SpaceText 생성 테스트")
    void createSpaceTextWithEmptyContentTest() {
        // given
        SpaceTextCreateRequest request = new SpaceTextCreateRequest(
                "", feature.getId()
        );

        // when
        SpaceTextResponse response = spaceTextService.createSpaceText(request);

        // then
        assertThat(response.textContent()).isEmpty();
        assertThat(response.feature()).isNotNull();
    }

    @Test
    @DisplayName("NULL 텍스트 내용으로 SpaceText 생성 테스트")
    void createSpaceTextWithNullContentTest() {
        // given
        Feature uniqueFeature = createUniqueFeature();
        SpaceTextCreateRequest request = new SpaceTextCreateRequest(
                null, uniqueFeature.getId()
        );

        // when
        SpaceTextResponse response = spaceTextService.createSpaceText(request);

        // then
        assertThat(response.textContent()).isNull();
        assertThat(response.feature()).isNotNull();
    }

    @Test
    @DisplayName("매우 긴 텍스트 내용으로 SpaceText 생성 테스트")
    void createSpaceTextWithVeryLongContentTest() {
        // given
        String longContent = "가".repeat(1000); // 1000자 텍스트
        Feature uniqueFeature = createUniqueFeature();
        SpaceTextCreateRequest request = new SpaceTextCreateRequest(
                longContent, uniqueFeature.getId()
        );

        // when
        SpaceTextResponse response = spaceTextService.createSpaceText(request);

        // then
        assertThat(response.textContent()).isEqualTo(longContent);
        assertThat(response.feature()).isNotNull();
    }

    @Test
    @DisplayName("Feature 변경 테스트")
    void changeFeatureTest() {
        // given
        // 첫 번째 SpaceText 생성
        SpaceTextCreateRequest request = new SpaceTextCreateRequest(
                "Feature 변경 테스트", feature.getId()
        );
        SpaceTextResponse savedResponse = spaceTextService.createSpaceText(request);

        // 새로운 Feature 생성
        String newFeatureId = UUID.randomUUID().toString();
        Feature newFeature = featureRepository.save(Feature.builder()
                .id(newFeatureId)
                .position(Spatial.builder().x(2.0).y(2.0).z(2.0).build())
                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .asset(asset)
                .facility(station)
                .build());

        // when
        SpaceText spaceText = spaceTextService.findSpaceTextById(savedResponse.id());
        spaceText.changeFeature(newFeature);
        entityManager.flush();

        // then
        SpaceTextResponse updatedResponse = spaceTextService.getSpaceTextById(savedResponse.id());
        assertThat(updatedResponse.feature()).isNotNull();
        assertThat(updatedResponse.feature().id()).isEqualTo(newFeatureId);
    }

    @Test
    @DisplayName("Feature 제거 테스트")
    void removeFeatureTest() {
        // given
        Feature uniqueFeature = createUniqueFeature();
        SpaceTextCreateRequest request = new SpaceTextCreateRequest(
                "Feature 제거 테스트", uniqueFeature.getId()
        );
        SpaceTextResponse savedResponse = spaceTextService.createSpaceText(request);

        // when
        SpaceText spaceText = spaceTextService.findSpaceTextById(savedResponse.id());
        spaceText.changeFeature(null);
        entityManager.flush();

        // then
        SpaceTextResponse updatedResponse = spaceTextService.getSpaceTextById(savedResponse.id());
        assertThat(updatedResponse.feature()).isNull();
    }

    @Test
    @DisplayName("SpaceText 생성-수정-삭제 전체 라이프사이클 테스트")
    void spaceTextLifecycleTest() {
        // 1. SpaceText 생성
        Feature uniqueFeature = createUniqueFeature();
        SpaceTextCreateRequest createRequest = new SpaceTextCreateRequest(
                "라이프사이클 테스트", uniqueFeature.getId()
        );
        SpaceTextResponse createdResponse = spaceTextService.createSpaceText(createRequest);
        assertThat(createdResponse.textContent()).isEqualTo("라이프사이클 테스트");

        // 2. SpaceText 수정
        SpaceTextUpdateRequest updateRequest = new SpaceTextUpdateRequest(
                null, // name field
                "수정된 라이프사이클 테스트" // textContent field
        );
        SpaceTextResponse updatedResponse = spaceTextService.updateSpaceText(createdResponse.id(), updateRequest);
        assertThat(updatedResponse.textContent()).isEqualTo("수정된 라이프사이클 테스트");

        // 3. Feature와의 연관관계 확인
        assertThat(updatedResponse.feature()).isNotNull();
        assertThat(updatedResponse.feature().id()).isEqualTo(uniqueFeature.getId());

        // 4. SpaceText 삭제
        spaceTextService.deleteSpaceText(createdResponse.id());

        // 5. 삭제 확인
        assertThrows(EntityNotFoundException.class, () -> 
            spaceTextService.getSpaceTextById(createdResponse.id())
        );
    }

    @Test
    @DisplayName("여러 개의 SpaceText 조회 테스트")
    void findMultipleSpaceTextsTest() {
        // given
        Feature feature1 = createUniqueFeature();
        Feature feature2 = createUniqueFeature();
        Feature feature3 = createUniqueFeature();
        
        SpaceTextCreateRequest request1 = new SpaceTextCreateRequest(
                "첫 번째 멀티 텍스트", feature1.getId()
        );
        SpaceTextCreateRequest request2 = new SpaceTextCreateRequest(
                "두 번째 멀티 텍스트", feature2.getId()
        );
        SpaceTextCreateRequest request3 = new SpaceTextCreateRequest(
                "세 번째 멀티 텍스트", feature3.getId()
        );

        SpaceTextResponse response1 = spaceTextService.createSpaceText(request1);
        SpaceTextResponse response2 = spaceTextService.createSpaceText(request2);
        SpaceTextResponse response3 = spaceTextService.createSpaceText(request3);

        // when
        List<SpaceTextResponse> allSpaceTexts = spaceTextService.getAllSpaceTexts();

        // then
        assertThat(allSpaceTexts).hasSizeGreaterThanOrEqualTo(3);

        List<String> spaceTextIds = allSpaceTexts.stream()
                .map(SpaceTextResponse::id)
                .toList();
        assertThat(spaceTextIds).contains(
                response1.id(), response2.id(), response3.id()
        );
    }

    @Test
    @DisplayName("엔티티 영속성 테스트")
    void entityPersistenceTest() {
        // given
        Feature uniqueFeature = createUniqueFeature();
        SpaceTextCreateRequest request = new SpaceTextCreateRequest(
                "영속성 테스트", uniqueFeature.getId()
        );
        SpaceTextResponse savedResponse = spaceTextService.createSpaceText(request);

        // when
        entityManager.flush();
        entityManager.clear();

        // SpaceText 다시 로드
        SpaceText spaceText = spaceTextService.findSpaceTextById(savedResponse.id());

        // then
        assertThat(spaceText).isNotNull();
        assertThat(spaceText.getId()).isEqualTo(savedResponse.id());
        assertThat(spaceText.getTextContent()).isEqualTo("영속성 테스트");
    }

    @Test
    @DisplayName("SpaceText가 없는 경우 findAll 결과 테스트")
    void findAllWithNoSpaceTextsTest() {
        // given
        spaceTextRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // when
        List<SpaceTextResponse> result = spaceTextService.getAllSpaceTexts();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("특수 문자가 포함된 텍스트 내용 처리 테스트")
    void spaceTextWithSpecialCharactersTest() {
        // given
        String specialContent = "특수문자 테스트: !@#$%^&*()_+-=[]{}|;':\",./<>?`~";
        Feature uniqueFeature = createUniqueFeature();
        SpaceTextCreateRequest request = new SpaceTextCreateRequest(
                specialContent, uniqueFeature.getId()
        );

        // when
        SpaceTextResponse response = spaceTextService.createSpaceText(request);

        // then
        assertThat(response.textContent()).isEqualTo(specialContent);
    }

    @Test
    @DisplayName("여러 줄 텍스트 내용 처리 테스트")
    void spaceTextWithMultilineContentTest() {
        // given
        String multilineContent = "첫 번째 줄\n두 번째 줄\n세 번째 줄";
        Feature uniqueFeature = createUniqueFeature();
        SpaceTextCreateRequest request = new SpaceTextCreateRequest(
                multilineContent, uniqueFeature.getId()
        );

        // when
        SpaceTextResponse response = spaceTextService.createSpaceText(request);

        // then
        assertThat(response.textContent()).isEqualTo(multilineContent);
    }
}