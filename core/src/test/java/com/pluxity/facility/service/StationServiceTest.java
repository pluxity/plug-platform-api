package com.pluxity.facility.service;

import com.pluxity.facility.dto.*;
import com.pluxity.facility.entity.Line;
import com.pluxity.facility.entity.Station;
import com.pluxity.facility.repository.LineRepository;
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
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    LineRepository lineRepository;

    @Autowired
    LineService lineService;

    @Autowired
    FileService fileService;

    @Autowired
    FacilityService facilityService;

    private Long drawingFileId;
    private Long thumbnailFileId;
    private StationCreateRequest createRequest;
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
                floorRequests,
                null
        );
        
        // 테스트 Line 생성
        testLine = Line.create("#FF0000", "테스트 호선", "asdfee");
        testLine = lineRepository.save(testLine);
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
        assertThat(savedStation.lineId()).isNull(); // Line 없이 생성했으므로 null
    }

    @Test
    @DisplayName("여러 개의 층을 가진 스테이션 생성 시 모든 층이 저장된다")
    void save_WithMultipleFloors_SavesAllFloors() {
        // given
        int floorCount = 5;
        List<FloorRequest> multipleFloors = IntStream.range(1, floorCount + 1)
                .mapToObj(i -> new FloorRequest(i + "층", i))
                .collect(Collectors.toList());
        
        StationCreateRequest requestWithMultipleFloors = new StationCreateRequest(
                createRequest.facility(),
                multipleFloors,
                null
        );
        
        // when
        Long id = stationService.save(requestWithMultipleFloors);
        
        // then
        StationResponse savedStation = stationService.findById(id);
        assertThat(savedStation).isNotNull();
        assertThat(savedStation.floors()).hasSize(floorCount);
        
        // 층 순서와 이름 확인
        for (int i = 0; i < floorCount; i++) {
            int floorNumber = i + 1;
            assertThat(savedStation.floors().stream()
                    .anyMatch(floor -> 
                        floor.name().equals(floorNumber + "층") && 
                        floor.groupId() == floorNumber))
                    .isTrue();
        }
    }

    @Test
    @DisplayName("Line을 지정하여 스테이션 생성 시 관계가 설정된다")
    void save_WithLine_SetsRelationship() {
        // given
        StationCreateRequest requestWithLine = new StationCreateRequest(
                createRequest.facility(),
                createRequest.floors(),
                testLine.getId()
        );
        
        // when
        Long id = stationService.save(requestWithLine);
        
        // then
        // 데이터베이스에서 직접 Station과 Line을 조회하여 관계 확인
        Station station = stationRepository.findById(id).orElseThrow();
        Line savedLine = lineRepository.findById(testLine.getId()).orElseThrow();
        
        assertThat(station.getLine()).isEqualTo(savedLine);
        assertThat(savedLine.getStations()).contains(station);
        
        // 응답에서도 lineId가 설정되어 있는지 확인
        StationResponse stationResponse = stationService.findById(id);
        assertThat(stationResponse.lineId()).isEqualTo(testLine.getId());
    }
    

    
    @Test
    @DisplayName("존재하지 않는 Line ID로 스테이션 생성 시 예외가 발생한다")
    void save_WithNonExistingLineId_ThrowsCustomException() {
        // given
        Long nonExistingLineId = 9999L;
        StationCreateRequest requestWithInvalidLine = new StationCreateRequest(
                createRequest.facility(),
                createRequest.floors(),
                nonExistingLineId
        );
        
        // when & then
        assertThrows(CustomException.class, () -> stationService.save(requestWithInvalidLine));
    }

    @Test
    @DisplayName("스테이션 업데이트 시 Line 관계가 변경된다")
    void update_WithLine_UpdatesRelationship() {
        // given
        // 먼저 Line 없이 스테이션 생성
        Long id = stationService.save(createRequest);
        
        // 업데이트 요청 준비 (Line 포함)
        StationUpdateRequest updateRequest = new StationUpdateRequest(
                "수정된 스테이션",
                "수정된 스테이션 설명",
                drawingFileId,
                thumbnailFileId,
                testLine.getId()
        );
        
        // when
        stationService.update(id, updateRequest);
        
        // then
        Station updatedStation = stationRepository.findById(id).orElseThrow();
        Line savedLine = lineRepository.findById(testLine.getId()).orElseThrow();
        
        assertThat(updatedStation.getLine()).isEqualTo(savedLine);
        assertThat(savedLine.getStations()).contains(updatedStation);
        
        // 응답에서도 lineId가 설정되어 있는지 확인
        StationResponse stationResponse = stationService.findById(id);
        assertThat(stationResponse.lineId()).isEqualTo(testLine.getId());
    }
    
    @Test
    @DisplayName("스테이션의 Line을 다른 Line으로 변경하면 관계가 업데이트된다")
    void update_WithDifferentLine_UpdatesRelationship() {
        // given
        // 먼저 첫 번째 Line과 함께 스테이션 생성
        StationCreateRequest requestWithLine = new StationCreateRequest(
                createRequest.facility(),
                createRequest.floors(),
                testLine.getId()
        );
        Long id = stationService.save(requestWithLine);
        
        // 두 번째 Line 생성
        Line newLine = Line.create("#00FF00", "새 테스트 호선", "asdfbb");
        newLine = lineRepository.save(newLine);
        
        // 업데이트 요청 준비 (두 번째 Line으로 변경)
        StationUpdateRequest updateRequest = new StationUpdateRequest(
                "수정된 스테이션",
                "수정된 스테이션 설명",
                drawingFileId,
                thumbnailFileId,
                newLine.getId()
        );
        
        // when
        stationService.update(id, updateRequest);
        
        // then
        Station updatedStation = stationRepository.findById(id).orElseThrow();
        Line oldLine = lineRepository.findById(testLine.getId()).orElseThrow();
        Line savedNewLine = lineRepository.findById(newLine.getId()).orElseThrow();
        
        // 이전 관계가 제거되었는지 확인
        assertThat(oldLine.getStations()).doesNotContain(updatedStation);
        
        // 새 관계가 설정되었는지 확인
        assertThat(updatedStation.getLine()).isEqualTo(savedNewLine);
        assertThat(savedNewLine.getStations()).contains(updatedStation);
        
        // 응답에서도 새 lineId가 설정되어 있는지 확인
        StationResponse stationResponse = stationService.findById(id);
        assertThat(stationResponse.lineId()).isEqualTo(newLine.getId());
    }
    
    @Test
    @DisplayName("스테이션 이름과 설명만 업데이트 시 Line 관계는 유지된다")
    void update_OnlyNameAndDescription_MaintainsLineRelationship() {
        // given
        // 먼저 Line과 함께 스테이션 생성
        StationCreateRequest requestWithLine = new StationCreateRequest(
                createRequest.facility(),
                createRequest.floors(),
                testLine.getId()
        );
        Long id = stationService.save(requestWithLine);
        
        // 업데이트 요청 준비 (이름과 설명만 변경, Line 유지)
        StationUpdateRequest updateRequest = new StationUpdateRequest(
                "수정된 스테이션",
                "수정된 스테이션 설명",
                drawingFileId,
                thumbnailFileId,
                testLine.getId()
        );
        
        // when
        stationService.update(id, updateRequest);
        
        // then
        Station updatedStation = stationRepository.findById(id).orElseThrow();
        Line savedLine = lineRepository.findById(testLine.getId()).orElseThrow();
        
        // 기본 정보가 업데이트되었는지 확인
        assertThat(updatedStation.getName()).isEqualTo("수정된 스테이션");
        assertThat(updatedStation.getDescription()).isEqualTo("수정된 스테이션 설명");
        
        // 관계는 유지되어야 함
        assertThat(updatedStation.getLine()).isEqualTo(savedLine);
        assertThat(savedLine.getStations()).contains(updatedStation);
        
        StationResponse stationResponse = stationService.findById(id);
        assertThat(stationResponse.lineId()).isEqualTo(testLine.getId());
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
        assertThat(responses.stream()
                    .anyMatch(station -> 
                        station.facility().name().equals("테스트 스테이션")))
                    .isTrue();
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
        assertThat(response.floors()).hasSize(1);
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
                thumbnailFileId,
                null
        );

        // when
        stationService.update(id, updateRequest);

        // then
        StationResponse updatedStation = stationService.findById(id);
        assertThat(updatedStation.facility().name()).isEqualTo("수정된 스테이션");
        assertThat(updatedStation.facility().description()).isEqualTo("수정된 스테이션 설명");
    }
    
    @Test
    @DisplayName("파일 ID만 업데이트할 수 있다")
    void update_OnlyFileIds_UpdatesFileIdsOnly() {
        // given
        Long id = stationService.save(createRequest);
        
        // 새 파일 IDs 생성
        Long newDrawingFileId = drawingFileId + 100;
        Long newThumbnailFileId = thumbnailFileId + 100;
        
        StationUpdateRequest updateRequest = new StationUpdateRequest(
                "수정된 스테이션",
                "수정된 스테이션 설명",
                newDrawingFileId,
                newThumbnailFileId,
                null
        );
        
        // when
        stationService.update(id, updateRequest);
        
        // then
        Station updatedStation = stationRepository.findById(id).orElseThrow();
        assertThat(updatedStation.getDrawingFileId()).isEqualTo(newDrawingFileId);
        assertThat(updatedStation.getThumbnailFileId()).isEqualTo(newThumbnailFileId);
    }

    @Test
    @DisplayName("스테이션 삭제 시 데이터가 삭제된다")
    void delete_WithExistingId_DeletesStation() {
        // given
        Long id = stationService.save(createRequest);
        
        // when
        stationService.delete(id);
        
        // then
        // 데이터베이스에서 해당 ID로 스테이션을 찾을 수 없어야 함
        assertThrows(CustomException.class, () -> facilityService.findById(id));
    }
    
    @Test
    @DisplayName("Line이 설정된 스테이션 삭제 시 라인 관계가 업데이트된다")
    void delete_StationWithLine_UpdatesLineRelationship() {
        // given
        StationCreateRequest requestWithLine = new StationCreateRequest(
                createRequest.facility(),
                createRequest.floors(),
                testLine.getId()
        );
        Long id = stationService.save(requestWithLine);
        
        // 관계 설정 확인
        Line line = lineRepository.findById(testLine.getId()).orElseThrow();
        Station station = stationRepository.findById(id).orElseThrow();
        assertThat(line.getStations()).contains(station);
        
        // when
        stationService.delete(id);
        
        // then
        // 라인은 여전히 존재해야 함
        Line refreshedLine = lineRepository.findById(testLine.getId()).orElseThrow();
        
        // 스테이션이 삭제되어 더 이상 라인에서 조회되지 않아야 함
        assertThat(refreshedLine.getStations().stream()
                .anyMatch(s -> s.getId().equals(id)))
                .isFalse();
    }
}