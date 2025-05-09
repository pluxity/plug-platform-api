package com.pluxity;

import com.pluxity.asset.constant.AssetType;
import com.pluxity.asset.dto.AssetCreateRequest;
import com.pluxity.asset.dto.AssetUpdateRequest;
import com.pluxity.asset.entity.Asset;
import com.pluxity.facility.dto.*;
import com.pluxity.feature.dto.FeatureCreateRequest;
import com.pluxity.feature.dto.FeatureUpdateRequest;
import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.entity.Spatial;
import com.pluxity.file.service.FileService;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public abstract class CoreApplicationTest {

    // 공통 상수값 정의
    private static final String TEST_IMAGE_PATH = "temp/temp.png";
    private static final String IMAGE_CONTENT_TYPE = "image/png";
    
    // Asset 테스트 상수
    private static final String DEFAULT_ASSET_NAME = "테스트 에셋";
    private static final String DEFAULT_UPDATED_ASSET_NAME = "수정된 에셋";
    private static final String DEFAULT_ASSET_NAME_1 = "에셋 1";
    private static final String DEFAULT_ASSET_NAME_2 = "에셋 2";
    
    // Building 테스트 상수
    private static final String DEFAULT_BUILDING_NAME = "테스트 건물";
    private static final String DEFAULT_BUILDING_DESCRIPTION = "테스트 건물 설명";
    private static final String DEFAULT_UPDATED_BUILDING_NAME = "수정된 건물";
    private static final String DEFAULT_UPDATED_BUILDING_DESCRIPTION = "수정된 건물 설명";
    private static final String DEFAULT_DRAWING_FILE_NAME = "drawing.png";
    private static final String DEFAULT_THUMBNAIL_FILE_NAME = "thumbnail.png";
    private static final String DEFAULT_FLOOR_NAME = "1층";
    private static final int DEFAULT_FLOOR_LEVEL = 1;
    
    // Panorama 테스트 상수
    private static final String DEFAULT_PANORAMA_NAME = "테스트 파노라마";
    private static final String DEFAULT_PANORAMA_DESCRIPTION = "테스트 파노라마 설명";
    private static final String DEFAULT_UPDATED_PANORAMA_NAME = "수정된 파노라마";
    private static final String DEFAULT_UPDATED_PANORAMA_DESCRIPTION = "수정된 파노라마 설명";
    private static final String DEFAULT_PANORAMA_ADDRESS = "서울시 강남구";
    private static final double DEFAULT_LATITUDE = 37.5665;
    private static final double DEFAULT_LONGITUDE = 126.9780;
    private static final double DEFAULT_ALTITUDE = 0.0;
    
    // Feature 테스트 상수
    private static final double DEFAULT_ORIGIN_X = 0.0;
    private static final double DEFAULT_ORIGIN_Y = 0.0;
    private static final double DEFAULT_ORIGIN_Z = 0.0;
    private static final double DEFAULT_POSITION_X = 1.0;
    private static final double DEFAULT_POSITION_Y = 2.0;
    private static final double DEFAULT_POSITION_Z = 3.0;
    private static final double DEFAULT_ROTATION_X = 0.0;
    private static final double DEFAULT_ROTATION_Y = 90.0;
    private static final double DEFAULT_ROTATION_Z = 0.0;
    private static final double DEFAULT_SCALE_X = 1.0;
    private static final double DEFAULT_SCALE_Y = 1.0;
    private static final double DEFAULT_SCALE_Z = 1.0;
    private static final double DEFAULT_UPDATED_POSITION_X = 2.0;
    private static final double DEFAULT_UPDATED_POSITION_Y = 2.0;
    private static final double DEFAULT_UPDATED_POSITION_Z = 2.0;
    private static final double DEFAULT_UPDATED_ROTATION_X = 90.0;
    private static final double DEFAULT_UPDATED_ROTATION_Y = 0.0;
    private static final double DEFAULT_UPDATED_ROTATION_Z = 0.0;
    private static final double DEFAULT_UPDATED_SCALE_X = 2.0;
    private static final double DEFAULT_UPDATED_SCALE_Y = 2.0;
    private static final double DEFAULT_UPDATED_SCALE_Z = 2.0;
    
    public static final Long DEFAULT_NON_EXISTING_ID = 999L;

    @Autowired
    protected FileService fileService;

    /* ------------------------------ 유틸리티 메서드 ------------------------------ */
    
    /**
     * 테스트용 MockMultipartFile 생성
     */
    protected MockMultipartFile createMockMultipartFile(String fileName, String contentType, byte[] content) {
        return new MockMultipartFile(
                fileName,
                fileName,
                contentType,
                content
        );
    }
    
    /**
     * 테스트 이미지 파일 콘텐츠 로드
     */
    protected byte[] loadTestImageContent() {
        try {
            ClassPathResource resource = new ClassPathResource(TEST_IMAGE_PATH);
            return Files.readAllBytes(Path.of(resource.getURI()));
        } catch (IOException e) {
            throw new RuntimeException("테스트 이미지 로드 실패", e);
        }
    }
    
    /**
     * 테스트용 이미지 파일 업로드 후 파일 ID 반환
     */
    protected Long uploadTestFile(String fileName) {
        byte[] fileContent = loadTestImageContent();
        MockMultipartFile multipartFile = createMockMultipartFile(
                fileName,
                IMAGE_CONTENT_TYPE, 
                fileContent
        );
        return fileService.initiateUpload(multipartFile);
    }

    /* ------------------------------ Asset 팩토리 메서드 ------------------------------ */
    
    /**
     * 기본 Asset 엔티티 생성
     */
    protected Asset createAsset() {
        return createAsset(DEFAULT_ASSET_NAME, AssetType.TWO_DIMENSION);
    }
    
    /**
     * Asset 엔티티 생성
     */
    protected Asset createAsset(String name, AssetType type) {
        return Asset.builder()
                .type(type)
                .name(name)
                .build();
    }
    
    /**
     * 기본 AssetCreateRequest 생성 (파일 없음)
     */
    protected AssetCreateRequest createAssetRequest() {
        return new AssetCreateRequest(
                AssetType.TWO_DIMENSION.name(),
                DEFAULT_ASSET_NAME,
                null
        );
    }
    
    /**
     * 파일이 포함된 AssetCreateRequest 생성
     */
    protected AssetCreateRequest createAssetRequestWithFile() {
        Long fileId = uploadTestFile("temp.png");
        return new AssetCreateRequest(
                AssetType.TWO_DIMENSION.name(),
                DEFAULT_ASSET_NAME,
                fileId
        );
    }
    
    /**
     * 커스텀 AssetCreateRequest 생성
     */
    protected AssetCreateRequest createAssetRequest(String name, String type, Long fileId) {
        return new AssetCreateRequest(
                type,
                name,
                fileId
        );
    }
    
    /**
     * 기본 AssetUpdateRequest 생성 (파일 없음)
     */
    protected AssetUpdateRequest createAssetUpdateRequest() {
        return new AssetUpdateRequest(
                AssetType.THREE_DIMENSION.name(),
                DEFAULT_UPDATED_ASSET_NAME,
                null
        );
    }
    
    /**
     * 파일이 포함된 AssetUpdateRequest 생성
     */
    protected AssetUpdateRequest createAssetUpdateRequestWithFile() {
        Long fileId = uploadTestFile("temp.png");
        return new AssetUpdateRequest(
                AssetType.THREE_DIMENSION.name(),
                DEFAULT_UPDATED_ASSET_NAME,
                fileId
        );
    }
    
    /**
     * 커스텀 AssetUpdateRequest 생성
     */
    protected AssetUpdateRequest createAssetUpdateRequest(String name, String type, Long fileId) {
        return new AssetUpdateRequest(
                type,
                name,
                fileId
        );
    }

    /* ------------------------------ Building 팩토리 메서드 ------------------------------ */
    
    /**
     * 기본 BuildingCreateRequest 생성
     */
    protected BuildingCreateRequest createBuildingRequest() {
        Long drawingFileId = uploadTestFile(DEFAULT_DRAWING_FILE_NAME);
        Long thumbnailFileId = uploadTestFile(DEFAULT_THUMBNAIL_FILE_NAME);
        
        FacilityCreateRequest facilityRequest = new FacilityCreateRequest(
                DEFAULT_BUILDING_NAME,
                DEFAULT_BUILDING_DESCRIPTION,
                drawingFileId,
                thumbnailFileId
        );
        
        List<FloorRequest> floorRequests = new ArrayList<>();
        floorRequests.add(new FloorRequest(DEFAULT_FLOOR_NAME, DEFAULT_FLOOR_LEVEL));
        
        return new BuildingCreateRequest(
                facilityRequest,
                floorRequests
        );
    }
    
    /**
     * 커스텀 BuildingCreateRequest 생성
     */
    protected BuildingCreateRequest createBuildingRequest(String name, String description, List<FloorRequest> floors) {
        Long drawingFileId = uploadTestFile(DEFAULT_DRAWING_FILE_NAME);
        Long thumbnailFileId = uploadTestFile(DEFAULT_THUMBNAIL_FILE_NAME);
        
        FacilityCreateRequest facilityRequest = new FacilityCreateRequest(
                name,
                description,
                drawingFileId,
                thumbnailFileId
        );
        
        return new BuildingCreateRequest(
                facilityRequest,
                floors
        );
    }
    
    /**
     * 기본 BuildingUpdateRequest 생성
     */
    protected BuildingUpdateRequest createBuildingUpdateRequest() {
        Long drawingFileId = uploadTestFile(DEFAULT_DRAWING_FILE_NAME);
        Long thumbnailFileId = uploadTestFile(DEFAULT_THUMBNAIL_FILE_NAME);
        
        return new BuildingUpdateRequest(
                DEFAULT_UPDATED_BUILDING_NAME,
                DEFAULT_UPDATED_BUILDING_DESCRIPTION,
                drawingFileId,
                thumbnailFileId
        );
    }
    
    /**
     * 커스텀 BuildingUpdateRequest 생성
     */
    protected BuildingUpdateRequest createBuildingUpdateRequest(String name, String description) {
        Long drawingFileId = uploadTestFile(DEFAULT_DRAWING_FILE_NAME);
        Long thumbnailFileId = uploadTestFile(DEFAULT_THUMBNAIL_FILE_NAME);
        
        return new BuildingUpdateRequest(
                name,
                description,
                drawingFileId,
                thumbnailFileId
        );
    }
    
    /**
     * 기본 FloorRequest 생성
     */
    protected FloorRequest createFloorRequest() {
        return new FloorRequest(DEFAULT_FLOOR_NAME, DEFAULT_FLOOR_LEVEL);
    }
    
    /**
     * 커스텀 FloorRequest 생성
     */
    protected FloorRequest createFloorRequest(String name, int level) {
        return new FloorRequest(name, level);
    }

    /* ------------------------------ Panorama 팩토리 메서드 ------------------------------ */
    
    /**
     * 기본 LocationRequest 생성
     */
    protected LocationRequest createLocationRequest() {
        return new LocationRequest(
                DEFAULT_LATITUDE,
                DEFAULT_LONGITUDE,
                DEFAULT_ALTITUDE
        );
    }
    
    /**
     * 커스텀 LocationRequest 생성
     */
    protected LocationRequest createLocationRequest(double latitude, double longitude, double altitude) {
        return new LocationRequest(
                latitude,
                longitude,
                altitude
        );
    }
    
    /**
     * 기본 PanoramaCreateRequest 생성
     */
    protected PanoramaCreateRequest createPanoramaRequest() {
        Long drawingFileId = uploadTestFile(DEFAULT_DRAWING_FILE_NAME);
        Long thumbnailFileId = uploadTestFile(DEFAULT_THUMBNAIL_FILE_NAME);
        
        FacilityCreateRequest facilityRequest = new FacilityCreateRequest(
                DEFAULT_PANORAMA_NAME,
                DEFAULT_PANORAMA_DESCRIPTION,
                drawingFileId,
                thumbnailFileId
        );
        
        LocationRequest locationRequest = createLocationRequest();
        
        return new PanoramaCreateRequest(
                facilityRequest,
                locationRequest,
                DEFAULT_PANORAMA_ADDRESS,
                drawingFileId,
                thumbnailFileId
        );
    }
    
    /**
     * 커스텀 PanoramaCreateRequest 생성
     */
    protected PanoramaCreateRequest createPanoramaRequest(String name, String description, String address, LocationRequest location) {
        Long drawingFileId = uploadTestFile(DEFAULT_DRAWING_FILE_NAME);
        Long thumbnailFileId = uploadTestFile(DEFAULT_THUMBNAIL_FILE_NAME);
        
        FacilityCreateRequest facilityRequest = new FacilityCreateRequest(
                name,
                description,
                drawingFileId,
                thumbnailFileId
        );
        
        return new PanoramaCreateRequest(
                facilityRequest,
                location,
                address,
                drawingFileId,
                thumbnailFileId
        );
    }
    
    /**
     * 기본 PanoramaUpdateRequest 생성
     */
    protected PanoramaUpdateRequest createPanoramaUpdateRequest() {
        Long drawingFileId = uploadTestFile(DEFAULT_DRAWING_FILE_NAME);
        Long thumbnailFileId = uploadTestFile(DEFAULT_THUMBNAIL_FILE_NAME);
        
        LocationRequest locationRequest = createLocationRequest();
        
        return new PanoramaUpdateRequest(
                locationRequest,
                DEFAULT_UPDATED_PANORAMA_NAME,
                DEFAULT_UPDATED_PANORAMA_DESCRIPTION,
                drawingFileId,
                thumbnailFileId
        );
    }
    
    /**
     * 커스텀 PanoramaUpdateRequest 생성
     */
    protected PanoramaUpdateRequest createPanoramaUpdateRequest(String name, String description, LocationRequest location) {
        Long drawingFileId = uploadTestFile(DEFAULT_DRAWING_FILE_NAME);
        Long thumbnailFileId = uploadTestFile(DEFAULT_THUMBNAIL_FILE_NAME);
        
        return new PanoramaUpdateRequest(
                location,
                name,
                description,
                drawingFileId,
                thumbnailFileId
        );
    }

    /* ------------------------------ Feature 팩토리 메서드 ------------------------------ */
    
    /**
     * 기본 Spatial(위치) 객체 생성
     */
    protected Spatial createDefaultPosition() {
        return Spatial.builder()
                .x(DEFAULT_ORIGIN_X)
                .y(DEFAULT_ORIGIN_Y)
                .z(DEFAULT_ORIGIN_Z)
                .build();
    }
    
    /**
     * 기본 Spatial(회전) 객체 생성
     */
    protected Spatial createDefaultRotation() {
        return Spatial.builder()
                .x(DEFAULT_ORIGIN_X)
                .y(DEFAULT_ORIGIN_Y)
                .z(DEFAULT_ORIGIN_Z)
                .build();
    }
    
    /**
     * 기본 Spatial(크기) 객체 생성
     */
    protected Spatial createDefaultScale() {
        return Spatial.builder()
                .x(DEFAULT_SCALE_X)
                .y(DEFAULT_SCALE_Y)
                .z(DEFAULT_SCALE_Z)
                .build();
    }
    
    /**
     * 커스텀 Spatial 객체 생성
     */
    protected Spatial createSpatial(double x, double y, double z) {
        return Spatial.builder()
                .x(x)
                .y(y)
                .z(z)
                .build();
    }
    
    /**
     * 기본 테스트용 포지션 생성
     */
    protected Spatial createTestPosition() {
        return Spatial.builder()
                .x(DEFAULT_POSITION_X)
                .y(DEFAULT_POSITION_Y)
                .z(DEFAULT_POSITION_Z)
                .build();
    }
    
    /**
     * 기본 테스트용 회전 생성
     */
    protected Spatial createTestRotation() {
        return Spatial.builder()
                .x(DEFAULT_ROTATION_X)
                .y(DEFAULT_ROTATION_Y)
                .z(DEFAULT_ROTATION_Z)
                .build();
    }
    
    /**
     * 기본 테스트용 크기 생성
     */
    protected Spatial createTestScale() {
        return Spatial.builder()
                .x(DEFAULT_SCALE_X)
                .y(DEFAULT_SCALE_Y)
                .z(DEFAULT_SCALE_Z)
                .build();
    }
    
    /**
     * 기본 업데이트용 포지션 생성
     */
    protected Spatial createUpdatedPosition() {
        return Spatial.builder()
                .x(DEFAULT_UPDATED_POSITION_X)
                .y(DEFAULT_UPDATED_POSITION_Y)
                .z(DEFAULT_UPDATED_POSITION_Z)
                .build();
    }
    
    /**
     * 기본 업데이트용 회전 생성
     */
    protected Spatial createUpdatedRotation() {
        return Spatial.builder()
                .x(DEFAULT_UPDATED_ROTATION_X)
                .y(DEFAULT_UPDATED_ROTATION_Y)
                .z(DEFAULT_UPDATED_ROTATION_Z)
                .build();
    }
    
    /**
     * 기본 업데이트용 크기 생성
     */
    protected Spatial createUpdatedScale() {
        return Spatial.builder()
                .x(DEFAULT_UPDATED_SCALE_X)
                .y(DEFAULT_UPDATED_SCALE_Y)
                .z(DEFAULT_UPDATED_SCALE_Z)
                .build();
    }
    
    /**
     * 기본 Feature 엔티티 생성
     */
    protected Feature createFeature() {
        return Feature.builder()
                .position(createTestPosition())
                .rotation(createTestRotation())
                .scale(createTestScale())
                .build();
    }
    
    /**
     * 커스텀 Feature 엔티티 생성
     */
    protected Feature createFeature(Spatial position, Spatial rotation, Spatial scale) {
        return Feature.builder()
                .position(position)
                .rotation(rotation)
                .scale(scale)
                .build();
    }
    
    /**
     * 기본 FeatureCreateRequest 생성
     */
    protected FeatureCreateRequest createFeatureRequest() {
        return FeatureCreateRequest.builder()
                .position(createTestPosition())
                .rotation(createTestRotation())
                .scale(createTestScale())
                .build();
    }
    
    /**
     * 커스텀 FeatureCreateRequest 생성
     */
    protected FeatureCreateRequest createFeatureRequest(Spatial position, Spatial rotation, Spatial scale) {
        return FeatureCreateRequest.builder()
                .position(position)
                .rotation(rotation)
                .scale(scale)
                .build();
    }
    
    /**
     * 기본 FeatureUpdateRequest 생성
     */
    protected FeatureUpdateRequest createFeatureUpdateRequest() {
        return FeatureUpdateRequest.builder()
                .position(createUpdatedPosition())
                .rotation(createUpdatedRotation())
                .build();
    }
    
    /**
     * 커스텀 FeatureUpdateRequest 생성
     */
    protected FeatureUpdateRequest createFeatureUpdateRequest(Spatial position, Spatial rotation, Spatial scale) {
        return FeatureUpdateRequest.builder()
                .position(position)
                .rotation(rotation)
                .scale(scale)
                .build();
    }
    
    /**
     * 부분 FeatureUpdateRequest 생성 (크기만 업데이트)
     */
    protected FeatureUpdateRequest createScaleOnlyUpdateRequest() {
        return FeatureUpdateRequest.builder()
                .scale(createUpdatedScale())
                .build();
    }
}