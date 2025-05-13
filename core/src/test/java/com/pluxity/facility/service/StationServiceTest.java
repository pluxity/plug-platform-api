package com.pluxity.facility.service;

import com.pluxity.facility.dto.*;
import com.pluxity.facility.repository.StationRepository;
import com.pluxity.file.service.FileService;
import com.pluxity.global.exception.CustomException;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
class StationServiceTest {

    @Autowired
    StationService stationService;

    @Autowired
    StationRepository stationRepository;

    @Autowired
    FileService fileService;

    @Autowired
    FacilityService facilityService;

    private Long drawingFileId;
    private Long thumbnailFileId;
    private StationCreateRequest createRequest;

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
                "테스트 스테이션", 
                "테스트 스테이션 설명", 
                drawingFileId, 
                thumbnailFileId
        );
        
        List<FloorRequest> floorRequests = new ArrayList<>();
        floorRequests.add(new FloorRequest(
                "1층", 
                1
        ));
        
        createRequest = new StationCreateRequest(
                facilityRequest,
                floorRequests
        );
    }

    @Test
    @DisplayName("유효한 요청으로 스테이션 생성 시 스테이션과 층이 저장된다")
    void save_WithValidRequest_SavesStationAndFloors() {
        // when
        Long id = stationService.save(createRequest);

        // then
        assertThat(id).isNotNull();
        
        // 저장된 스테이션 확인
        StationResponse savedStation = stationService.findById(id);
        assertThat(savedStation).isNotNull();
        assertThat(savedStation.facility().name()).isEqualTo("테스트 스테이션");
        assertThat(savedStation.facility().description()).isEqualTo("테스트 스테이션 설명");
        assertThat(savedStation.floors()).isNotEmpty();
    }

    @Test
    @DisplayName("모든 스테이션 조회 시 스테이션 목록이 반환된다")
    void findAll_ReturnsListOfStationResponses() {
        // given
        Long id = stationService.save(createRequest);
        
        // when
        List<StationResponse> responses = stationService.findAll();

        // then
        assertThat(responses).isNotEmpty();
        assertThat(responses.get(0).facility().name()).isEqualTo("테스트 스테이션");
        assertThat(responses.get(0).facility().description()).isEqualTo("테스트 스테이션 설명");
    }

    @Test
    @DisplayName("ID로 스테이션 조회 시 스테이션 정보가 반환된다")
    void findById_WithExistingId_ReturnsStationResponse() {
        // given
        Long id = stationService.save(createRequest);

        // when
        StationResponse response = stationService.findById(id);

        // then
        assertThat(response).isNotNull();
        assertThat(response.facility().name()).isEqualTo("테스트 스테이션");
        assertThat(response.facility().description()).isEqualTo("테스트 스테이션 설명");
        assertThat(response.floors()).isNotEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 ID로 스테이션 조회 시 예외가 발생한다")
    void findById_WithNonExistingId_ThrowsCustomException() {
        // given
        Long nonExistingId = 9999L;

        // when & then
        assertThrows(CustomException.class, () -> stationService.findById(nonExistingId));
    }

    @Test
    @DisplayName("유효한 요청으로 스테이션 정보 수정 시 스테이션 정보가 업데이트된다")
    void update_WithValidRequest_UpdatesStation() {
        // given
        Long id = stationService.save(createRequest);
        StationUpdateRequest updateRequest = new StationUpdateRequest(
                "수정된 스테이션",
                "수정된 스테이션 설명",
                drawingFileId,
                thumbnailFileId
        );

        // when
        stationService.update(id, updateRequest);

        // then
        StationResponse updatedStation = stationService.findById(id);
        assertThat(updatedStation.facility().name()).isEqualTo("수정된 스테이션");
        assertThat(updatedStation.facility().description()).isEqualTo("수정된 스테이션 설명");
    }

    @Test
    @DisplayName("스테이션 삭제 시 모든 이력이 삭제된다")
    void delete_WithExistingId_DeletesStation() {
        // given
        Long id = stationService.save(createRequest);
        
        // when
        StationResponse response = stationService.findById(id);
        assertThat(response).isNotNull();
        
        // then
        stationService.delete(id);
        
        // 삭제 후에는 해당 ID로 스테이션을 찾을 수 없어야 함
        assertThrows(CustomException.class, () -> stationService.findById(id));
    }
    
    @Test
    @DisplayName("스테이션 이력 조회 시 이력 목록이 반환된다")
    void findFacilityHistories_WithExistingId_ReturnsFacilityHistoryResponses() {
        // given
        Long id = stationService.save(createRequest);
        
        // when
        List<FacilityHistoryResponse> historyResponses = stationService.findFacilityHistories(id);
        
        // then
        assertThat(historyResponses).isNotNull();
    }
}