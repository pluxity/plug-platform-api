package com.pluxity.domains.station;

import com.pluxity.SasangApplication;
import com.pluxity.domains.station.dto.SasangStationCreateRequest;
import com.pluxity.domains.station.dto.SasangStationResponse;
import com.pluxity.domains.station.dto.SasangStationUpdateRequest;
import com.pluxity.facility.facility.FacilityService;
import com.pluxity.facility.facility.dto.FacilityCreateRequest;
import com.pluxity.facility.floor.dto.FloorRequest;
import com.pluxity.facility.line.Line;
import com.pluxity.facility.line.LineRepository;
import com.pluxity.facility.line.LineService;
import com.pluxity.facility.station.StationService;
import com.pluxity.facility.station.dto.StationCreateRequest;
import com.pluxity.facility.station.dto.StationResponseWithFeature;
import com.pluxity.facility.station.dto.StationUpdateRequest;
import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.entity.Spatial;
import com.pluxity.feature.repository.FeatureRepository;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(classes = SasangApplication.class)
@Transactional
class SasangStationServiceTest {

    @Autowired
    SasangStationService sasangStationService;

    @Autowired
    SasangStationRepository sasangStationRepository;

    @Autowired
    StationService stationService;

    @Autowired
    LineRepository lineRepository;

    @Autowired
    LineService lineService;

    @Autowired
    FileService fileService;

    @Autowired
    FacilityService facilityService;

    @Autowired
    EntityManager em;

    @Autowired
    FeatureRepository featureRepository;

    private Long drawingFileId;
    private Long thumbnailFileId;
    private SasangStationCreateRequest createRequest;
    private Line testLine;

    @BeforeEach
    void setUp() throws IOException {
        // 테스트 이미지 파일 준비
        ClassPathResource resource = new ClassPathResource("temp/temp.png");
        byte[] fileContent = Files.readAllBytes(Path.of(resource.getURI()));
        
        // MockMultipartFile 생성
        MultipartFile drawingFile = new MockMultipartFile(
                "drawing.png", "drawing.png", "image/png", fileContent);
        MultipartFile thumbnailFile = new MockMultipartFile(
                "thumbnail.png", "thumbnail.png", "image/png", fileContent);
        
        // 파일 업로드 초기화
        drawingFileId = fileService.initiateUpload(drawingFile);
        thumbnailFileId = fileService.initiateUpload(thumbnailFile);
        
        // 테스트 데이터 준비
        FacilityCreateRequest facilityRequest = new FacilityCreateRequest(
                "테스트 사상역", 
                "ST001",
                "테스트 사상역 설명",
                drawingFileId, 
                thumbnailFileId
        );
        
        List<FloorRequest> floorRequests = new ArrayList<>();
        floorRequests.add(new FloorRequest(
                "1층", 
                "1"
        ));
        
        StationCreateRequest stationRequest = new StationCreateRequest(
                facilityRequest,
                floorRequests,
                Collections.emptyList(),
                "route"
        );
        
        createRequest = new SasangStationCreateRequest(
                stationRequest,
                "EXT001"
        );
        
        // 테스트 Line 생성
        testLine = Line.builder()
                .name("테스트 호선")
                .color("#FF0000")
                .build();
        testLine = lineRepository.save(testLine);
    }

    @Test
    @DisplayName("유효한 요청으로 사상역 생성 시 사상역과 층이 저장된다")
    void save_WithValidRequest_SavesSasangStationAndFloors() {
        // when
        Long id = sasangStationService.save(createRequest);

        // then
        assertThat(id).isNotNull();
        
        // 저장된 사상역 확인
        SasangStationResponse savedStation = sasangStationService.findById(id);
        assertThat(savedStation).isNotNull();
        assertThat(savedStation.stationResponse().facility().name()).isEqualTo("테스트 사상역");
        assertThat(savedStation.stationResponse().facility().description()).isEqualTo("테스트 사상역 설명");
        assertThat(savedStation.stationResponse().floors()).isNotEmpty();
        assertThat(savedStation.stationResponse().lineIds()).isEmpty(); // Line 없이 생성했으므로 빈 리스트
        assertThat(savedStation.externalCode()).isEqualTo("EXT001");
    }

//    @Test
//    @DisplayName("코드로 사상역 조회 시 사상역 정보가 반환된다")
//    void findByCode_WithExistingCode_ReturnsSasangStationResponse() {
//        // given
//        Long id = sasangStationService.save(createRequest);
//
//        // 생성된 사상역의 코드가 올바르게 설정되었는지 확인
//        SasangStation createdStation = sasangStationRepository.findById(id).orElseThrow();
//        assertThat(createdStation.getCode()).isEqualTo("ST001");
//
//        em.flush();
//        em.clear();
//
//        // when
//        SasangStationResponse response = sasangStationService.findByCode("ST001");
//
//        // then
//        assertThat(response).isNotNull();
//        assertThat(response.stationResponse().facility().name()).isEqualTo("테스트 사상역");
//        assertThat(response.code()).isEqualTo("ST001");
//    }

    @Test
    @DisplayName("외부 코드로 사상역 조회 시 사상역 정보가 반환된다")
    void findByExternalCode_WithExistingExternalCode_ReturnsSasangStationResponse() {
        // given
        Long id = sasangStationService.save(createRequest);
        
        // 생성된 사상역의 외부 코드가 올바르게 설정되었는지 확인
        SasangStation createdStation = sasangStationRepository.findById(id).orElseThrow();
        assertThat(createdStation.getExternalCode()).isEqualTo("EXT001");
        
        em.flush();
        em.clear();

        // when
        SasangStationResponse response = sasangStationService.findByExternalCode("EXT001");

        // then
        assertThat(response).isNotNull();
        assertThat(response.stationResponse().facility().name()).isEqualTo("테스트 사상역");
        assertThat(response.externalCode()).isEqualTo("EXT001");
    }

    @Test
    @DisplayName("유효한 요청으로 사상역 정보 수정 시 사상역 정보가 업데이트된다")
    void update_WithValidRequest_UpdatesSasangStation() {
        // given
        Long id = sasangStationService.save(createRequest);
        
        StationUpdateRequest stationUpdateRequest = new StationUpdateRequest(
                "수정된 사상역",
                "수정된 사상역 설명",
                drawingFileId,
                thumbnailFileId,
                Collections.emptyList(),
                "수정된 경로"
        );
        
        SasangStationUpdateRequest updateRequest = new SasangStationUpdateRequest(
                stationUpdateRequest,
                "EXT002"
        );

        // when
        sasangStationService.update(id, updateRequest);

        // then
        SasangStationResponse updatedStation = sasangStationService.findById(id);
        assertThat(updatedStation.stationResponse().facility().name()).isEqualTo("수정된 사상역");
        assertThat(updatedStation.stationResponse().facility().description()).isEqualTo("수정된 사상역 설명");
        assertThat(updatedStation.externalCode()).isEqualTo("EXT002");
    }

    @Test
    @DisplayName("존재하지 않는 외부 코드로 사상역 조회 시 예외가 발생한다")
    void findByExternalCode_WithNonExistingExternalCode_ThrowsCustomException() {
        // given
        String nonExistingExternalCode = "NONEXIST";

        // when & then
        assertThrows(CustomException.class, () -> sasangStationService.findByExternalCode(nonExistingExternalCode));
    }

    @Test
    @DisplayName("스테이션과 피처 간의 양방향 연관관계가 올바르게 설정된다")
    void stationAndFeature_BidirectionalRelationship_IsSetCorrectly() {
        // given
        Long stationId = sasangStationService.save(createRequest);
        SasangStation station = sasangStationRepository.findById(stationId).orElseThrow();
        
        // Feature 생성
        Feature feature = Feature.builder()
                .id(UUID.randomUUID().toString())
                .position(new Spatial(10.0, 20.0, 30.0))
                .rotation(new Spatial(0.0, 0.0, 0.0))
                .scale(new Spatial(1.0, 1.0, 1.0))
                .floorId(UUID.randomUUID().toString())
                .build();
        
        // when
        // Feature에서 Station 설정 (양방향 연관관계 설정)
        feature.changeFacility(station);
        featureRepository.save(feature);
        
        em.flush();
        em.clear();
        
        // then
        // 다시 로드해서 연관관계 확인
        SasangStation foundStation = sasangStationRepository.findById(stationId).orElseThrow();
        Feature foundFeature = featureRepository.findById(feature.getId()).orElseThrow();
        
        // Station에서 Feature로의 참조 확인
        assertThat(foundStation.getFeatures()).hasSize(1);
        assertThat(foundStation.getFeatures().get(0).getId()).isEqualTo(feature.getId());
        
        // Feature에서 Station으로의 참조 확인
        assertThat(foundFeature.getFacility()).isNotNull();
        assertThat(foundFeature.getFacility().getId()).isEqualTo(stationId);
    }
    
    @Test
    @DisplayName("스테이션의 피처 목록을 조회할 수 있다")
    void findStationWithFeatures_ReturnsStationWithFeatures() {
        // given
        Long stationId = sasangStationService.save(createRequest);
        SasangStation station = sasangStationRepository.findById(stationId).orElseThrow();
        
        // 여러 Feature 생성 및 연결
        for (int i = 0; i < 3; i++) {
            Feature feature = Feature.builder()
                    .id(UUID.randomUUID().toString())
                    .position(new Spatial(i * 10.0, i * 20.0, i * 30.0))
                    .rotation(new Spatial(0.0, 0.0, 0.0))
                    .scale(new Spatial(1.0, 1.0, 1.0))
                    .floorId(UUID.randomUUID().toString())
                    .build();
            
            feature.changeFacility(station);
            featureRepository.save(feature);
        }
        
        em.flush();
        em.clear();
        
        // when
        StationResponseWithFeature response = sasangStationService.findStationWithFeatures(stationId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.features()).hasSize(3);
        assertThat(response.facility().id()).isEqualTo(stationId);
    }
} 