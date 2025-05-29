package com.pluxity.feature.service;

import com.pluxity.asset.entity.Asset;
import com.pluxity.asset.repository.AssetRepository;
import com.pluxity.asset.service.AssetService;
import com.pluxity.facility.facility.FacilityRepository;
import com.pluxity.facility.facility.FacilityService;
import com.pluxity.facility.station.Station;
import com.pluxity.feature.dto.FeatureCreateRequest;
import com.pluxity.feature.dto.FeatureResponse;
import com.pluxity.feature.dto.FeatureUpdateRequest;
import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.entity.Spatial;
import com.pluxity.feature.repository.FeatureRepository;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class FeatureServiceTest {

    @Autowired
    private FeatureRepository featureRepository;
    
    @Autowired
    private AssetRepository assetRepository;
    
    @Autowired
    private FacilityService facilityService;
    
    @Autowired
    private AssetService assetService;
    
    @Autowired
    private FileService fileService;
    
    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private FeatureService featureService;

    private Asset createAndSaveTestAsset() {
        Asset asset = Asset.builder().name("Test Asset").code("T01").build();
        return assetRepository.save(asset);
    }
    
    private Station createAndSaveTestFacility() {
        // Station 클래스 사용하여 Facility 생성
        Station station = Station.builder()
                .name("Test Station")
                .description("Test Description")
                .route("Test Route")
                .build();
        
        return facilityRepository.save(station);
    }

    @Test
    @DisplayName("유효한 요청으로 피처 생성 시 피처가 저장된다")
    void createFeature_WithValidRequest_SavesFeature() {
        // given
        String featureId = UUID.randomUUID().toString();
        Asset testAsset = createAndSaveTestAsset();
        Long assetId = testAsset.getId();
        
        Station testStation = createAndSaveTestFacility();
        Long facilityId = testStation.getId();
        Long floorId = 1L;

        Spatial position = Spatial.builder().x(1.0).y(2.0).z(3.0).build();
        Spatial rotation = Spatial.builder().x(0.0).y(90.0).z(0.0).build();
        Spatial scale = Spatial.builder().x(1.0).y(1.0).z(1.0).build();
        
        FeatureCreateRequest request = new FeatureCreateRequest(
                featureId, position, rotation, scale, assetId, facilityId, floorId);
        
        // when
        FeatureResponse response = featureService.createFeature(request);
        
        // then
        assertNotNull(response);
        assertEquals(featureId, response.id());
        assertEquals(position.getX(), response.position().getX());
        assertEquals(position.getY(), response.position().getY());
        assertEquals(position.getZ(), response.position().getZ());
        assertEquals(rotation.getX(), response.rotation().getX());
        assertEquals(rotation.getY(), response.rotation().getY());
        assertEquals(rotation.getZ(), response.rotation().getZ());
        assertEquals(scale.getX(), response.scale().getX());
        assertEquals(scale.getY(), response.scale().getY());
        assertEquals(scale.getZ(), response.scale().getZ());
        assertNotNull(response.asset());
        assertEquals(assetId, response.asset().id());
        assertNotNull(response.facilityId());
        assertEquals(facilityId, response.facilityId().id());
        assertEquals(floorId, response.floorId());
        
        // 저장된 피처 확인
        Feature savedFeature = featureRepository.findById(featureId).orElse(null);
        assertNotNull(savedFeature);
        assertEquals(featureId, savedFeature.getId());
        assertNotNull(savedFeature.getAsset());
        assertEquals(assetId, savedFeature.getAsset().getId());
        assertNotNull(savedFeature.getFacility());
        assertEquals(facilityId, savedFeature.getFacility().getId());
        assertEquals(floorId, savedFeature.getFloorId());
    }
    
    @Test
    @DisplayName("기본값으로 피처 생성 시 기본 Spatial 값으로 피처가 생성된다")
    void createFeature_WithDefaultValues_CreatesFeatureWithDefaultSpatials() {
        // given
        String featureId = UUID.randomUUID().toString();
        Spatial defaultPosition = Spatial.builder().x(0.0).y(0.0).z(0.0).build();
        Spatial defaultRotation = Spatial.builder().x(0.0).y(0.0).z(0.0).build();
        Spatial defaultScale = Spatial.builder().x(1.0).y(1.0).z(1.0).build();
        
        Asset testAsset = createAndSaveTestAsset();
        Long assetId = testAsset.getId();
        
        Station testStation = createAndSaveTestFacility();
        Long facilityId = testStation.getId();
        Long floorId = 1L;
        
        FeatureCreateRequest request = new FeatureCreateRequest(
                featureId, defaultPosition, defaultRotation, defaultScale, assetId, facilityId, floorId);
        
        // when
        FeatureResponse response = featureService.createFeature(request);
        
        // then
        assertNotNull(response);
        assertEquals(featureId, response.id());
        assertEquals(0.0, response.position().getX());
        assertEquals(0.0, response.position().getY());
        assertEquals(0.0, response.position().getZ());
        
        assertEquals(0.0, response.rotation().getX());
        assertEquals(0.0, response.rotation().getY());
        assertEquals(0.0, response.rotation().getZ());
        
        assertEquals(1.0, response.scale().getX());
        assertEquals(1.0, response.scale().getY());
        assertEquals(1.0, response.scale().getZ());
        assertNotNull(response.asset());
        assertNotNull(response.facilityId());
        assertEquals(floorId, response.floorId());

        Feature savedFeature = featureRepository.findById(featureId).orElse(null);
        assertNotNull(savedFeature);
        assertNotNull(savedFeature.getAsset());
        assertNotNull(savedFeature.getFacility());
        assertEquals(floorId, savedFeature.getFloorId());
    }
    
    @Test
    @DisplayName("존재하는 ID로 피처 조회 시 피처 정보가 반환된다")
    void getFeature_WithExistingId_ReturnsFeature() {
        // given
        String featureId = UUID.randomUUID().toString();
        Spatial position = Spatial.builder().x(1.0).y(2.0).z(3.0).build();
        Spatial rotation = Spatial.builder().x(0.0).y(90.0).z(0.0).build();
        Spatial scale = Spatial.builder().x(1.0).y(1.0).z(1.0).build();
        
        Asset asset = createAndSaveTestAsset();
        Station facility = createAndSaveTestFacility();
        Long floorId = 1L;
        
        Feature feature = Feature.builder()
                .id(featureId)
                .position(position)
                .rotation(rotation)
                .scale(scale)
                .asset(asset)
                .facility(facility)
                .floorId(floorId)
                .build();
        
        featureRepository.save(feature);
        
        // when
        FeatureResponse response = featureService.getFeature(featureId);
        
        // then
        assertNotNull(response);
        assertEquals(featureId, response.id());
        assertEquals(position.getX(), response.position().getX());
        assertEquals(position.getY(), response.position().getY());
        assertEquals(position.getZ(), response.position().getZ());
        assertEquals(rotation.getX(), response.rotation().getX());
        assertEquals(rotation.getY(), response.rotation().getY());
        assertEquals(rotation.getZ(), response.rotation().getZ());
        assertEquals(scale.getX(), response.scale().getX());
        assertEquals(scale.getY(), response.scale().getY());
        assertEquals(scale.getZ(), response.scale().getZ());
        assertNotNull(response.asset());
        assertNotNull(response.facilityId());
        assertEquals(floorId, response.floorId());
    }
    
    @Test
    @DisplayName("존재하지 않는 ID로 피처 조회 시 예외가 발생한다")
    void getFeature_WithNonExistingId_ThrowsException() {
        // given
        String nonExistingId = "non-existing-feature-uuid";
        
        // when & then
        CustomException exception = assertThrows(CustomException.class, 
                () -> featureService.getFeature(nonExistingId));
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("Feature not found", exception.getCodeName());
    }
    
//    @Test
//    @DisplayName("모든 피처 조회 시 피처 목록이 반환된다")
//    void getFeatures_ReturnsListOfFeatures() {
//        // given
//        Asset asset = createAndSaveTestAsset();
//        Station facility = createAndSaveTestFacility();
//        Long floorId = 1L;
//
//        Feature feature1 = Feature.builder()
//                .id(UUID.randomUUID().toString())
//                .position(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
//                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
//                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
//                .asset(asset)
//                .facility(facility)
//                .floorId(floorId)
//                .build();
//
//        Feature feature2 = Feature.builder()
//                .id(UUID.randomUUID().toString())
//                .position(Spatial.builder().x(2.0).y(2.0).z(2.0).build())
//                .rotation(Spatial.builder().x(90.0).y(0.0).z(0.0).build())
//                .scale(Spatial.builder().x(2.0).y(2.0).z(2.0).build())
//                .asset(asset)
//                .facility(facility)
//                .floorId(floorId)
//                .build();
//
//        featureRepository.save(feature1);
//        featureRepository.save(feature2);
//
//        // when
//        List<FeatureResponse> responses = featureService.getFeatures();
//
//        // then
//        assertEquals(2, responses.size());
//
//        FeatureResponse firstResponse = responses.stream()
//                .filter(r -> r.position().getX() == 1.0)
//                .findFirst()
//                .orElse(null);
//
//        FeatureResponse secondResponse = responses.stream()
//                .filter(r -> r.position().getX() == 2.0)
//                .findFirst()
//                .orElse(null);
//
//        assertNotNull(firstResponse);
//        assertNotNull(secondResponse);
//
//        assertEquals(1.0, firstResponse.scale().getX());
//        assertEquals(2.0, secondResponse.scale().getX());
//        assertNotNull(firstResponse.asset());
//        assertNotNull(firstResponse.facilityId());
//        assertEquals(floorId, firstResponse.floorId());
//    }
    
    @Test
    @DisplayName("피처가 없는 경우 빈 목록이 반환된다")
    void getFeatures_WithNoFeatures_ReturnsEmptyList() {
        // given
        featureRepository.deleteAll();
        
        // when
        List<FeatureResponse> responses = featureService.getFeatures();
        
        // then
        assertTrue(responses.isEmpty());
    }
    
    @Test
    @DisplayName("유효한 요청으로 피처 업데이트 시 피처 정보가 업데이트된다")
    void updateFeature_WithValidRequest_UpdatesFeature() {
        // given
        String featureId = UUID.randomUUID().toString();
        Asset testAsset = createAndSaveTestAsset();
        Station facility = createAndSaveTestFacility();
        Long floorId = 1L;

        Spatial originalPosition = Spatial.builder().x(1.0).y(1.0).z(1.0).build();
        Spatial originalRotation = Spatial.builder().x(0.0).y(0.0).z(0.0).build();
        Spatial originalScale = Spatial.builder().x(1.0).y(1.0).z(1.0).build();
        
        Feature featureToSave = Feature.builder()
                .id(featureId)
                .position(originalPosition)
                .rotation(originalRotation)
                .scale(originalScale)
                .asset(testAsset)
                .facility(facility)
                .floorId(floorId)
                .build();
        
        Feature savedFeature = featureRepository.save(featureToSave);
        assertEquals(featureId, savedFeature.getId(), "ID mismatch after initial save. Feature ID should be manually set.");

        Spatial newPosition = Spatial.builder().x(2.0).y(2.0).z(2.0).build();
        Spatial newRotation = Spatial.builder().x(90.0).y(0.0).z(0.0).build();

        FeatureUpdateRequest request = new FeatureUpdateRequest(newPosition, newRotation, null);
        
        // when
        FeatureResponse response = featureService.updateFeature(savedFeature.getId(), request);
        
        // then
        assertNotNull(response);
        assertEquals(featureId, response.id(), "ID mismatch in FeatureResponse after update.");
        assertEquals(newPosition.getX(), response.position().getX());
        assertEquals(newPosition.getY(), response.position().getY());
        assertEquals(newPosition.getZ(), response.position().getZ());
        assertEquals(newRotation.getX(), response.rotation().getX());
        assertEquals(newRotation.getY(), response.rotation().getY());
        assertEquals(newRotation.getZ(), response.rotation().getZ());
        assertEquals(originalScale.getX(), response.scale().getX());
        assertEquals(originalScale.getY(), response.scale().getY());
        assertEquals(originalScale.getZ(), response.scale().getZ());
        assertNotNull(response.asset());
        assertNotNull(response.facilityId());
        assertEquals(floorId, response.floorId());
        
        Feature updatedFeatureFromDb = featureRepository.findById(savedFeature.getId()).orElseThrow();
        assertEquals(featureId, updatedFeatureFromDb.getId(), "ID mismatch in DB after update.");
        assertEquals(newPosition.getX(), updatedFeatureFromDb.getPosition().getX());
        assertEquals(newRotation.getY(), updatedFeatureFromDb.getRotation().getY());
        assertEquals(originalScale.getZ(), updatedFeatureFromDb.getScale().getZ());
        assertNotNull(updatedFeatureFromDb.getAsset());
        assertNotNull(updatedFeatureFromDb.getFacility());
        assertEquals(floorId, updatedFeatureFromDb.getFloorId());
    }
    
    @Test
    @DisplayName("부분 업데이트 요청 시 해당 필드만 업데이트된다")
    void updateFeature_WithPartialRequest_UpdatesOnlySpecifiedFields() {
        // given
        String featureId = UUID.randomUUID().toString();
        Asset testAsset = createAndSaveTestAsset();
        Station facility = createAndSaveTestFacility();
        Long floorId = 1L;
        
        Spatial originalPosition = Spatial.builder().x(1.0).y(1.0).z(1.0).build();
        Spatial originalRotation = Spatial.builder().x(0.0).y(0.0).z(0.0).build();
        Spatial originalScale = Spatial.builder().x(1.0).y(1.0).z(1.0).build();
        
        Feature feature = Feature.builder()
                .id(featureId)
                .position(originalPosition)
                .rotation(originalRotation)
                .scale(originalScale)
                .asset(testAsset)
                .facility(facility)
                .floorId(floorId)
                .build();
        
        featureRepository.save(feature);
        
        Spatial newScale = Spatial.builder().x(2.0).y(2.0).z(2.0).build();
        
        FeatureUpdateRequest request = new FeatureUpdateRequest(null, null, newScale);
        
        // when
        FeatureResponse response = featureService.updateFeature(featureId, request);
        
        // then
        assertNotNull(response);
        assertEquals(featureId, response.id());
        
        assertEquals(originalPosition.getX(), response.position().getX());
        assertEquals(originalRotation.getY(), response.rotation().getY());
        assertEquals(newScale.getZ(), response.scale().getZ());
        assertNotNull(response.asset());
        assertNotNull(response.facilityId());
        assertEquals(floorId, response.floorId());
        
        Feature updatedFeature = featureRepository.findById(featureId).orElse(null);
        assertNotNull(updatedFeature);
        assertEquals(originalPosition.getX(), updatedFeature.getPosition().getX());
        assertEquals(originalRotation.getY(), updatedFeature.getRotation().getY());
        assertEquals(newScale.getZ(), updatedFeature.getScale().getZ());
        assertNotNull(updatedFeature.getAsset());
        assertNotNull(updatedFeature.getFacility());
    }
    
    @Test
    @DisplayName("존재하지 않는 ID로 피처 업데이트 시 예외가 발생한다")
    void updateFeature_WithNonExistingId_ThrowsException() {
        // given
        String nonExistingId = "non-existing-feature-uuid";
        FeatureUpdateRequest request = new FeatureUpdateRequest(null, null, null);
        
        // when & then
        CustomException exception = assertThrows(CustomException.class, 
                () -> featureService.updateFeature(nonExistingId, request));
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("Feature not found", exception.getCodeName());
    }
    
    @Test
    @DisplayName("존재하는 ID로 피처 삭제 시 피처가 삭제된다")
    void deleteFeature_WithExistingId_DeletesFeature() {
        // given
        String featureId = UUID.randomUUID().toString();
        Asset asset = createAndSaveTestAsset();
        Station facility = createAndSaveTestFacility();
        Long floorId = 1L;
        
        Feature feature = Feature.builder()
                .id(featureId)
                .asset(asset)
                .facility(facility)
                .floorId(floorId)
                .build();
                
        featureRepository.save(feature);
        
        // when
        featureService.deleteFeature(featureId);
        
        // then
        assertFalse(featureRepository.findById(featureId).isPresent());
    }
    
    @Test
    @DisplayName("존재하지 않는 ID로 피처 삭제 시 예외가 발생한다")
    void deleteFeature_WithNonExistingId_ThrowsException() {
        // given
        String nonExistingId = "non-existing-feature-uuid";
        
        // when & then
        CustomException exception = assertThrows(CustomException.class, 
                () -> featureService.deleteFeature(nonExistingId));
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
        assertEquals("Feature not found", exception.getCodeName());
    }
    
    @Test
    @DisplayName("3D 공간 속성 테스트")
    void testSpatialProperties() {
        // given
        Spatial origin = Spatial.builder().x(0.0).y(0.0).z(0.0).build();
        Spatial position = Spatial.builder().x(1.0).y(2.0).z(3.0).build();
        Spatial defaultScale = Spatial.builder().x(1.0).y(1.0).z(1.0).build();
        
        // when & then
        assertEquals(0.0, origin.getX());
        assertEquals(0.0, origin.getY());
        assertEquals(0.0, origin.getZ());
        
        assertEquals(1.0, position.getX());
        assertEquals(2.0, position.getY());
        assertEquals(3.0, position.getZ());
        
        assertEquals(1.0, defaultScale.getX());
        assertEquals(1.0, defaultScale.getY());
        assertEquals(1.0, defaultScale.getZ());
    }
    
    @Test
    @DisplayName("Feature와 Asset의 양방향 연관관계 설정 테스트")
    void testFeatureAssetBidirectionalRelationship() {
        // given
        String featureId = UUID.randomUUID().toString();
        Asset asset = createAndSaveTestAsset();
        Long assetId = asset.getId();
        Station facility = createAndSaveTestFacility();
        Long floorId = 1L;

        Feature feature = Feature.builder()
                .id(featureId)
                .position(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .facility(facility)
                .floorId(floorId)
                .build();

        // when
        feature.changeAsset(asset);
        featureRepository.save(feature);

        // then
        Feature savedFeature = featureRepository.findById(featureId).orElse(null);
        assertNotNull(savedFeature);
        assertNotNull(savedFeature.getAsset());
        assertEquals(assetId, savedFeature.getAsset().getId());

        Asset retrievedAsset = assetRepository.findById(assetId).orElse(null);
        assertNotNull(retrievedAsset);
    }
    
    @Test
    @DisplayName("Asset을 가진 Feature 생성 테스트")
    void createFeature_WithAsset_CreatesFeatureWithAsset() {
        // given
        String featureId = UUID.randomUUID().toString();
        Asset testAsset = createAndSaveTestAsset();
        Long assetId = testAsset.getId();
        
        Station testStation = createAndSaveTestFacility();
        Long facilityId = testStation.getId();
        Long floorId = 1L;

        Spatial position = Spatial.builder().x(1.0).y(1.0).z(1.0).build();
        Spatial rotation = Spatial.builder().x(0.0).y(0.0).z(0.0).build();
        Spatial scale = Spatial.builder().x(1.0).y(1.0).z(1.0).build();

        FeatureCreateRequest request = new FeatureCreateRequest(
                featureId, position, rotation, scale, assetId, facilityId, floorId);

        // when
        FeatureResponse response = featureService.createFeature(request);

        // then
        assertNotNull(response);
        assertEquals(featureId, response.id());
        assertNotNull(response.asset());
        assertEquals(assetId, response.asset().id());
        assertEquals("Test Asset", response.asset().name());
        assertNotNull(response.facilityId());
        assertEquals(facilityId, response.facilityId().id());
        assertEquals(floorId, response.floorId());

        Feature savedFeature = featureRepository.findById(featureId).orElse(null);
        assertNotNull(savedFeature);
        assertNotNull(savedFeature.getAsset());
        assertEquals(assetId, savedFeature.getAsset().getId());
        assertNotNull(savedFeature.getFacility());
        assertEquals(facilityId, savedFeature.getFacility().getId());
        assertEquals(floorId, savedFeature.getFloorId());
    }

    // assignAssetToFeature 테스트 케이스들
    @Test
    @DisplayName("assignAssetToFeature: 유효한 ID로 Feature에 Asset 할당 성공")
    void assignAssetToFeature_WithValidIds_AssignsAssetToFeature() {
        // given
        String featureId = UUID.randomUUID().toString();
        Station facility = createAndSaveTestFacility();
        Long floorId = 1L;
        
        Feature feature = Feature.builder()
                .id(featureId)
                .facility(facility)
                .floorId(floorId)
                .build();
                
        Feature savedFeature = featureRepository.save(feature);
        
        Asset asset = createAndSaveTestAsset();
        Long assetId = asset.getId();

        // when
        FeatureResponse response = featureService.assignAssetToFeature(savedFeature.getId(), assetId);

        // then
        assertNotNull(response);
        assertNotNull(response.asset());
        assertEquals(assetId, response.asset().id());
        assertNotNull(response.facilityId());
        assertEquals(floorId, response.floorId());

        Feature updatedFeature = featureRepository.findById(savedFeature.getId()).orElseThrow();
        assertNotNull(updatedFeature.getAsset());
        assertEquals(assetId, updatedFeature.getAsset().getId());

        Asset updatedAsset = assetRepository.findById(assetId).orElseThrow();
        assertTrue(updatedAsset.getFeatures().stream().anyMatch(f -> f.getId().equals(savedFeature.getId())));
    }

    @Test
    @DisplayName("assignAssetToFeature: 존재하지 않는 Feature ID로 요청 시 예외 발생")
    void assignAssetToFeature_WithNonExistingFeatureId_ThrowsNotFoundException() {
        // given
        String nonExistingFeatureId = "non-existing-feature-uuid";
        Asset asset = createAndSaveTestAsset();
        Long assetId = asset.getId();

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> featureService.assignAssetToFeature(nonExistingFeatureId, assetId));
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @Test
    @DisplayName("assignAssetToFeature: 존재하지 않는 Asset ID로 요청 시 예외 발생")
    void assignAssetToFeature_WithNonExistingAssetId_ThrowsNotFoundException() {
        // given
        String featureId = UUID.randomUUID().toString();
        Station facility = createAndSaveTestFacility();
        Long floorId = 1L;
        
        Feature feature = featureRepository.save(Feature.builder()
                .id(featureId)
                .facility(facility)
                .floorId(floorId)
                .build());
                
        Long nonExistingAssetId = 9999L;

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> featureService.assignAssetToFeature(feature.getId(), nonExistingAssetId));
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @Test
    @DisplayName("assignAssetToFeature: Feature에 이미 다른 Asset이 할당된 경우 새 Asset으로 변경")
    void assignAssetToFeature_WhenFeatureAlreadyHasAsset_ChangesToNewAsset() {
        // given
        String featureId = UUID.randomUUID().toString();
        Station facility = createAndSaveTestFacility();
        Long floorId = 1L;
        
        Feature feature = featureRepository.save(Feature.builder()
                .id(featureId)
                .facility(facility)
                .floorId(floorId)
                .build());
        
        Asset oldAsset = createAndSaveTestAsset(); // code T01
        feature.changeAsset(oldAsset); // 초기 Asset 할당 (엔티티 메서드 직접 사용)
        featureRepository.save(feature); // feature와 oldAsset 관계 저장

        Asset newAsset = assetRepository.save(Asset.builder().name("New Asset").code("N01").build());
        Long newAssetId = newAsset.getId();

        // when
        FeatureResponse response = featureService.assignAssetToFeature(feature.getId(), newAssetId);

        // then
        assertNotNull(response.asset());
        assertEquals(newAssetId, response.asset().id());
        assertNotNull(response.facilityId());
        assertEquals(floorId, response.floorId());

        Feature updatedFeature = featureRepository.findById(feature.getId()).orElseThrow();
        assertNotNull(updatedFeature.getAsset());
        assertEquals(newAssetId, updatedFeature.getAsset().getId());

        Asset retrievedOldAsset = assetRepository.findById(oldAsset.getId()).orElseThrow();
        assertTrue(retrievedOldAsset.getFeatures().stream().noneMatch(f -> f.getId().equals(feature.getId())), "Old asset should not contain the feature anymore");

        Asset retrievedNewAsset = assetRepository.findById(newAssetId).orElseThrow();
        assertTrue(retrievedNewAsset.getFeatures().stream().anyMatch(f -> f.getId().equals(feature.getId())), "New asset should contain the feature");
    }

    // removeAssetFromFeature 테스트 케이스들
//    @Test
//    @DisplayName("removeAssetFromFeature: 할당된 Asset 제거 성공")
//    void removeAssetFromFeature_WhenAssetAssigned_RemovesAssetFromFeature() {
//        // given
//        String featureId = UUID.randomUUID().toString();
//        Station facility = createAndSaveTestFacility();
//        Long floorId = 1L;
//
//        Feature feature = Feature.builder()
//                .id(featureId)
//                .facility(facility)
//                .floorId(floorId)
//                .build();
//
//        featureRepository.save(feature);
//
//        Asset asset = createAndSaveTestAsset();
//        Long assetId = asset.getId();
//
//        feature.changeAsset(asset); // Asset 할당 (엔티티 메서드 직접 사용)
//        featureRepository.save(feature); // 관계 저장
//
//        // when
//        FeatureResponse response = featureService.removeAssetFromFeature(feature.getId());
//
//        // then
//        assertNotNull(response);
//        assertNull(response.asset());
//        assertNotNull(response.facilityId());
//        assertEquals(floorId, response.floorId());
//
//        Feature updatedFeature = featureRepository.findById(feature.getId()).orElseThrow();
//        assertNull(updatedFeature.getAsset());
//
//        Asset originalAsset = assetRepository.findById(assetId).orElseThrow();
//        assertTrue(originalAsset.getFeatures().stream().noneMatch(f -> f.getId().equals(feature.getId())));
//    }

    @Test
    @DisplayName("removeAssetFromFeature: 존재하지 않는 Feature ID로 요청 시 예외 발생")
    void removeAssetFromFeature_WithNonExistingFeatureId_ThrowsNotFoundException() {
        // given
        String nonExistingFeatureId = "non-existing-feature-uuid";

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> featureService.removeAssetFromFeature(nonExistingFeatureId));
        assertEquals(HttpStatus.NOT_FOUND, exception.getHttpStatus());
    }

    @Test
    @DisplayName("removeAssetFromFeature: Asset이 할당되지 않은 Feature에 요청 시 예외 발생")
    void removeAssetFromFeature_WhenNoAssetAssigned_ThrowsBadRequestException() {
        // given
        String featureId = UUID.randomUUID().toString();
        Station facility = createAndSaveTestFacility();
        Long floorId = 1L;
        
        Feature feature = featureRepository.save(Feature.builder()
                .id(featureId)
                .facility(facility)
                .floorId(floorId)
                .build()); // Asset 없이 저장

        // when & then
        CustomException exception = assertThrows(CustomException.class,
                () -> featureService.removeAssetFromFeature(feature.getId()));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertTrue(exception.getMessage().contains("할당된 에셋이 없습니다"));
    }
}