package com.pluxity.facility.service;

import com.pluxity.facility.facility.FacilityService;
import com.pluxity.facility.facility.dto.FacilityCreateRequest;
import com.pluxity.facility.floor.dto.FloorRequest;
import com.pluxity.facility.line.Line;
import com.pluxity.facility.line.LineRepository;
import com.pluxity.facility.line.LineService;
import com.pluxity.facility.station.Station;
import com.pluxity.facility.station.StationRepository;
import com.pluxity.facility.station.StationService;
import com.pluxity.facility.station.dto.StationCreateRequest;
import com.pluxity.facility.station.dto.StationResponse;
import com.pluxity.facility.station.dto.StationUpdateRequest;
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

    @Autowired
    EntityManager em;

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
                "ST001",
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
                Collections.emptyList(),
                "route"
        );

        // 테스트 Line 생성
        testLine = Line.builder()
                .name("테스트 호선")
                .color("#FF0000")
                .build();
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
        assertThat(savedStation.lineIds()).isEmpty(); // Line 없이 생성했으므로 빈 리스트
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
                Collections.emptyList(),
                "route"
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
                                    floor.floorId() == floorNumber))
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
                List.of(testLine.getId()),
                "route"
        );

        // when
        Long id = stationService.save(requestWithLine);

        // then
        // 데이터베이스에서 직접 Station과 Line을 조회하여 관계 확인
        Station station = stationRepository.findById(id).orElseThrow();

        // 관계 확인
        assertThat(station.getStationLines()).isNotEmpty();
        assertThat(station.getStationLines().get(0).getLine().getId()).isEqualTo(testLine.getId());

        // 응답에서도 lineIds가 설정되어 있는지 확인
        StationResponse stationResponse = stationService.findById(id);
        assertThat(stationResponse.lineIds()).contains(testLine.getId());
    }

    @Test
    @DisplayName("존재하지 않는 Line ID로 스테이션 생성 시 예외가 발생한다")
    void save_WithNonExistingLineId_ThrowsCustomException() {
        // given
        StationCreateRequest requestWithInvalidLine = new StationCreateRequest(
                createRequest.facility(),
                createRequest.floors(),
                List.of(9999L), // 존재하지 않는 Line ID
                "route"
        );

        // when & then
        assertThrows(CustomException.class, () -> stationService.save(requestWithInvalidLine));
    }

    @Test
    @DisplayName("스테이션 업데이트 시 Line 관계가 변경된다")
    void update_WithLine_UpdatesRelationship() {
        // given
        Long id = stationService.save(createRequest); // Line 없이 생성

        // Line이 있는 업데이트 요청 준비
        StationUpdateRequest updateRequest = new StationUpdateRequest(
                "수정된 스테이션",
                "수정된 스테이션 설명",
                drawingFileId,
                thumbnailFileId,
                List.of(testLine.getId()),
                "수정된 경로"
        );

        // when
        stationService.update(id, updateRequest);

        // then
        // 데이터베이스에서 직접 Station과 Line을 조회하여 관계 확인
        Station station = stationRepository.findById(id).orElseThrow();

        // 관계가 설정되었는지 확인
        assertThat(station.getStationLines()).isNotEmpty();
        assertThat(station.getStationLines().get(0).getLine().getId()).isEqualTo(testLine.getId());

        // 응답에서도 lineIds가 설정되어 있는지 확인
        StationResponse stationResponse = stationService.findById(id);
        assertThat(stationResponse.lineIds()).contains(testLine.getId());
    }

    @Test
    @DisplayName("스테이션에 다른 라인 추가 및 제거 기능이 작동한다")
    void addAndRemoveLineToStation_Works() {
        // given
        Long id = stationService.save(createRequest); // Line 없이 생성

        // when - 라인 추가
        stationService.addLineToStation(id, testLine.getId());

        // then - 라인이 추가되었는지 확인
        StationResponse stationWithLine = stationService.findById(id);
        assertThat(stationWithLine.lineIds()).contains(testLine.getId());

        // when - 라인 제거
        stationService.removeLineFromStation(id, testLine.getId());

        // then - 라인이 제거되었는지 확인
        StationResponse stationWithoutLine = stationService.findById(id);
        assertThat(stationWithoutLine.lineIds()).doesNotContain(testLine.getId());
    }

    @Test
    @DisplayName("스테이션 이름과 설명만 업데이트 시 Line 관계는 유지된다")
    void update_OnlyNameAndDescription_MaintainsLineRelationship() {
        // given
        // 1. Line이 있는 스테이션 생성
        StationCreateRequest requestWithLine = new StationCreateRequest(
                createRequest.facility(),
                createRequest.floors(),
                List.of(testLine.getId()),
                "route"
        );
        Long id = stationService.save(requestWithLine);

        // 2. Line ID 없이 업데이트 요청 준비 (null로 설정)
        StationUpdateRequest updateRequest = new StationUpdateRequest(
                "수정된 스테이션",
                "수정된 스테이션 설명",
                null, // 변경 없음
                null, // 변경 없음
                null, // 변경 없음 - Line 관계 유지
                null  // 변경 없음
        );

        // when
        stationService.update(id, updateRequest);

        // then
        StationResponse updatedStation = stationService.findById(id);
        assertThat(updatedStation.facility().name()).isEqualTo("수정된 스테이션");
        assertThat(updatedStation.facility().description()).isEqualTo("수정된 스테이션 설명");

        // Line 관계가 유지되었는지 확인
        assertThat(updatedStation.lineIds()).contains(testLine.getId());
    }

    @Test
    @DisplayName("모든 스테이션 조회 시 스테이션 목록이 반환된다")
    void findAll_ReturnsListOfStationResponses() {
        // given

        int count = 1;
        stationService.save(createRequest);

        // when
        List<StationResponse> responses = stationService.findAll();

        // then
        assertThat(responses).isNotEmpty();
        assertThat(responses.size()).isGreaterThanOrEqualTo(count);
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
                Collections.emptyList(),
                "수정된 경로"
        );

        // when
        stationService.update(id, updateRequest);

        // then
        StationResponse updatedStation = stationService.findById(id);
        assertThat(updatedStation.facility().name()).isEqualTo("수정된 스테이션");
        assertThat(updatedStation.facility().description()).isEqualTo("수정된 스테이션 설명");
        assertThat(updatedStation.facility().drawing()).isNotNull();
        assertThat(updatedStation.facility().thumbnail()).isNotNull();
    }

    @Test
    @DisplayName("파일 ID만 업데이트할 수 있다")
    void update_OnlyFileIds_UpdatesFileIdsOnly() {
        // given
        Long id = stationService.save(createRequest);

        // 새로운 파일 ID 준비
        Long newDrawingFileId = drawingFileId + 1; // 테스트용 가상 ID
        Long newThumbnailFileId = thumbnailFileId + 1; // 테스트용 가상 ID

        StationUpdateRequest updateRequest = new StationUpdateRequest(
                null, // 변경 없음
                null, // 변경 없음
                newDrawingFileId,
                newThumbnailFileId,
                null, // 변경 없음
                null  // 변경 없음
        );

        // when
        stationService.update(id, updateRequest);

        // then
        Station updatedStation = stationRepository.findById(id).orElseThrow();
        assertThat(updatedStation.getDrawingFileId()).isEqualTo(newDrawingFileId);
        assertThat(updatedStation.getThumbnailFileId()).isEqualTo(newThumbnailFileId);

        // 이름과 설명은 변경되지 않았는지 확인
        assertThat(updatedStation.getName()).isEqualTo("테스트 스테이션");
        assertThat(updatedStation.getDescription()).isEqualTo("테스트 스테이션 설명");
    }

    @Test
    @DisplayName("스테이션 삭제 시 데이터가 삭제된다")
    void delete_WithExistingId_DeletesStation() {
        // given
        Long id = stationService.save(createRequest);

        // when
        stationService.delete(id);

        // then
        assertThat(stationRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("Line이 설정된 스테이션 삭제 시 라인 관계가 업데이트된다")
    void delete_StationWithLine_UpdatesLineRelationship() {
        // given
        StationCreateRequest requestWithLine = new StationCreateRequest(
                createRequest.facility(),
                createRequest.floors(),
                List.of(testLine.getId()),
                "route"
        );
        Long id = stationService.save(requestWithLine);

        // 스테이션과 라인 관계가 설정되었는지 확인
        Station station = stationRepository.findById(id).orElseThrow();
        assertThat(station.getStationLines()).isNotEmpty();

        // when
        stationService.delete(id);

        // then
        // 스테이션이 삭제되었는지 확인
        assertThat(stationRepository.findById(id)).isEmpty();

        // 라인은 여전히 존재하는지 확인
        Line line = lineRepository.findById(testLine.getId()).orElseThrow();
        assertThat(line).isNotNull();
    }
}