package com.pluxity.feature.service;

import static org.junit.jupiter.api.Assertions.*;

import com.pluxity.asset.entity.Asset;
import com.pluxity.asset.repository.AssetRepository;
import com.pluxity.device.entity.Device;
import com.pluxity.facility.FacilityRepository;
import com.pluxity.feature.dto.FeatureCreateRequest;
import com.pluxity.feature.dto.FeatureResponse;
import com.pluxity.feature.dto.FeatureUpdateRequest;
import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.entity.Spatial;
import com.pluxity.feature.repository.FeatureRepository;
import com.pluxity.global.exception.CustomException;
import com.pluxity.station.Station;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class FeatureServiceTest {

    @Autowired
    private FeatureRepository featureRepository;
    
    @Autowired
    private AssetRepository assetRepository;
    
    @Autowired
    private FacilityRepository facilityRepository;

    @Autowired
    private FeatureService featureService;

    @Autowired
    private EntityManager em;

    private Asset createAndSaveTestAsset() {
        Asset asset = Asset.builder().name("Test Asset").code("T01").build();
        return assetRepository.save(asset);
    }
    
    private Station createAndSaveTestFacility() {
        // Station 클래스 사용하여 Facility 생성
        Station station = Station.builder()
                .name("Test Station")
                .description("Test Description")
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
        String floorId = "1";

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
        assertEquals(floorId, response.floorId());
        
        // 저장된 피처 확인
        Feature savedFeature = featureRepository.findById(featureId).orElse(null);
        assertNotNull(savedFeature);
        assertEquals(featureId, savedFeature.getId());
        assertNotNull(savedFeature.getAssetId());
        assertEquals(assetId, savedFeature.getAssetId());
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
        String floorId = "1";
        
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
        assertEquals(floorId, response.floorId());

        Feature savedFeature = featureRepository.findById(featureId).orElse(null);
        assertNotNull(savedFeature);
        assertNotNull(savedFeature.getAssetId());
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
        String floorId = "1";
        
        Feature feature = Feature.builder()
                .id(featureId)
                .position(position)
                .rotation(rotation)
                .scale(scale)
                .assetId(asset.getId())
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
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getErrorCode().getHttpStatus());
        assertEquals("NOT_FOUND", exception.getErrorCode().getStatusName());
    }
    
    @Test
    @DisplayName("피처 목록 조회 시 피처 목록이 반환된다")
    void getFeatures_ReturnsListOfFeatures() {
//        // given
//        String featureId1 = UUID.randomUUID().toString();
//        String featureId2 = UUID.randomUUID().toString();
//
//        Asset asset = createAndSaveTestAsset();
//        Station facility = createAndSaveTestFacility();
//        String floorId = "1";
//
//        Feature feature1 = Feature.builder()
//                .id(featureId1)
//                .position(new Spatial(1.0, 1.0, 1.0))
//                .rotation(new Spatial(0.0, 0.0, 0.0))
//                .scale(new Spatial(1.0, 1.0, 1.0))
//                .asset(asset)
//                .facility(facility)
//                .floorId(floorId)
//                .build();
//
//        Feature feature2 = Feature.builder()
//                .id(featureId2)
//                .position(new Spatial(2.0, 2.0, 2.0))
//                .rotation(new Spatial(90.0, 0.0, 0.0))
//                .scale(new Spatial(2.0, 2.0, 2.0))
//                .asset(asset)
//                .facility(facility)
//                .floorId(floorId)
//                .build();
//
//        featureRepository.saveAll(List.of(feature1, feature2));
//
//        // when
//        List<FeatureResponse> featureIds = featureService.getFeatures();
//
//        // then
//        assertFalse(featureIds.isEmpty());
//        assertEquals(2, featureIds.size());
//
//        FeatureResponse firstResponse = featureIds.get(0);
//        assertEquals(featureId1, firstResponse.id());
//        assertEquals(floorId, firstResponse.floorId());
    }
    
    @Test
    @DisplayName("피처가 없는 경우 빈 목록이 반환된다")
    void getFeatures_WithNoFeatures_ReturnsEmptyList() {
        // given
        featureRepository.deleteAll();
        Station facility = createAndSaveTestFacility();
        // when
        List<FeatureResponse> responses = featureService.getFeatures(facility.getId());
        
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
        String floorId = "1";

        Spatial originalPosition = Spatial.builder().x(1.0).y(1.0).z(1.0).build();
        Spatial originalRotation = Spatial.builder().x(0.0).y(0.0).z(0.0).build();
        Spatial originalScale = Spatial.builder().x(1.0).y(1.0).z(1.0).build();
        
        Feature featureToSave = Feature.builder()
                .id(featureId)
                .position(originalPosition)
                .rotation(originalRotation)
                .scale(originalScale)
                .assetId(testAsset.getId())
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
        assertEquals(floorId, response.floorId());
        
        Feature updatedFeatureFromDb = featureRepository.findById(savedFeature.getId()).orElseThrow();
        assertEquals(featureId, updatedFeatureFromDb.getId(), "ID mismatch in DB after update.");
        assertEquals(newPosition.getX(), updatedFeatureFromDb.getPosition().getX());
        assertEquals(newRotation.getY(), updatedFeatureFromDb.getRotation().getY());
        assertEquals(originalScale.getZ(), updatedFeatureFromDb.getScale().getZ());
        assertNotNull(updatedFeatureFromDb.getAssetId());
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
        String floorId = "1";
        
        Spatial originalPosition = Spatial.builder().x(1.0).y(1.0).z(1.0).build();
        Spatial originalRotation = Spatial.builder().x(0.0).y(0.0).z(0.0).build();
        Spatial originalScale = Spatial.builder().x(1.0).y(1.0).z(1.0).build();
        
        Feature feature = Feature.builder()
                .id(featureId)
                .position(originalPosition)
                .rotation(originalRotation)
                .scale(originalScale)
                .assetId(testAsset.getId())
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
        assertEquals(floorId, response.floorId());
        
        Feature updatedFeature = featureRepository.findById(featureId).orElse(null);
        assertNotNull(updatedFeature);
        assertEquals(originalPosition.getX(), updatedFeature.getPosition().getX());
        assertEquals(originalRotation.getY(), updatedFeature.getRotation().getY());
        assertEquals(newScale.getZ(), updatedFeature.getScale().getZ());
        assertNotNull(updatedFeature.getAssetId());
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
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getErrorCode().getHttpStatus());
        assertEquals("NOT_FOUND", exception.getErrorCode().getStatusName());
    }
    
    @Test
    @DisplayName("존재하는 ID로 피처 삭제 시 피처가 삭제된다")
    void deleteFeature_WithExistingId_DeletesFeature() {
        // given
        String featureId = UUID.randomUUID().toString();
        Asset asset = createAndSaveTestAsset();
        Station facility = createAndSaveTestFacility();
        String floorId = "1";
        
        Feature feature = Feature.builder()
                .id(featureId)
                .assetId(asset.getId())
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
        
        assertEquals(HttpStatus.NOT_FOUND, exception.getErrorCode().getHttpStatus());
        assertEquals("NOT_FOUND", exception.getErrorCode().getStatusName());
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
    @DisplayName("Asset을 가진 Feature 생성 테스트")
    void createFeature_WithAsset_CreatesFeatureWithAsset() {
        // given
        String featureId = UUID.randomUUID().toString();
        Asset testAsset = createAndSaveTestAsset();
        Long assetId = testAsset.getId();
        
        Station testStation = createAndSaveTestFacility();
        Long facilityId = testStation.getId();
        String floorId = "1";

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
        assertEquals(floorId, response.floorId());

        Feature savedFeature = featureRepository.findById(featureId).orElse(null);
        assertNotNull(savedFeature);
        assertNotNull(savedFeature.getAssetId());
        assertEquals(assetId, savedFeature.getAssetId());
        assertNotNull(savedFeature.getFacility());
        assertEquals(facilityId, savedFeature.getFacility().getId());
        assertEquals(floorId, savedFeature.getFloorId());
    }


//     removeAssetFromFeature 테스트 케이스들
//    @Test
//    @DisplayName("removeAssetFromFeature: 할당된 Asset 제거 성공")
//    void removeAssetFromFeature_WhenAssetAssigned_RemovesAssetFromFeature() {
//        // given
//        String featureIds = UUID.randomUUID().toString();
//        Station facility = createAndSaveTestFacility();
//        Long floorId = 1L;
//
//        Feature feature = Feature.builder()
//                .id(featureIds)
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
    @DisplayName("피처의 모든 관계 제거 후 삭제 테스트")
    void deleteFeature_WithAllRelations_RemovesAllRelationsBeforeDelete() {
        // given
        String featureId = UUID.randomUUID().toString();
        Asset asset = createAndSaveTestAsset();
        Station facility = createAndSaveTestFacility();
        
        // 1. 디바이스 생성 - 모킹을 통해 구현
        Device device = Mockito.mock(Device.class);
        
        // 2. 피처 생성 및 Asset, Facility 관계 설정
        Feature feature = Feature.builder()
                .id(featureId)
                .position(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .rotation(Spatial.builder().x(0.0).y(0.0).z(0.0).build())
                .scale(Spatial.builder().x(1.0).y(1.0).z(1.0).build())
                .assetId(asset.getId())
                .facility(facility)
                .floorId("1")
                .build();
                
        // 피처 저장
        Feature savedFeature = featureRepository.save(feature);
        
        // 피처와 디바이스 간의 양방향 관계 설정
        ReflectionTestUtils.setField(savedFeature, "device", device);
        Mockito.when(device.getFeature()).thenReturn(savedFeature);
        
        // 관계가 모두 설정되었는지 확인
        savedFeature = featureRepository.findById(featureId).orElseThrow();
        assertNotNull(savedFeature.getAssetId());
        assertNotNull(savedFeature.getFacility());
        assertNotNull(savedFeature.getDevice());
        
        // when
        featureService.deleteFeature(featureId);
        
        // then
        // 1. 피처가 삭제되었는지 확인
        assertFalse(featureRepository.findById(featureId).isPresent());
        em.flush();
        // 2. Asset에서 관계가 제거되었는지 확인
        Asset updatedAsset = assetRepository.findById(asset.getId()).orElseThrow();
        assertFalse(featureService.findFeatureIdsByAssetId(updatedAsset.getId()).stream()
                .anyMatch(f -> f.equals(featureId)));
                
    }

}