package com.pluxity.facility.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pluxity.facility.dto.FacilityCreateRequest;
import com.pluxity.facility.dto.FacilityUpdateRequest;
import com.pluxity.facility.floor.FloorRepository;
import com.pluxity.facility.floor.dto.FloorRequest;
import com.pluxity.feature.repository.FeatureRepository;
import com.pluxity.global.exception.CustomException;
import com.pluxity.label3d.Label3DRepository;
import com.pluxity.station.*;
import com.pluxity.station.dto.*;
import com.pluxity.util.TestFileUploader;
import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class StationServiceTest {

    @Autowired private StationService stationService;
    @Autowired private StationRepository stationRepository;
    @Autowired private FloorRepository floorRepository;
    @Autowired private LineRepository lineRepository;
    @Autowired private StationLineRepository stationLineRepository;
    @Autowired private StationCodeRepository stationCodeRepository;
    @Autowired private FeatureRepository featureRepository;
    @Autowired private Label3DRepository label3DRepository;
    @Autowired private TestFileUploader testFileUploader;
    @Autowired private LineService lineService;

    // --- Create(save) Test ---

    @Test
    @DisplayName("성공: 모든 필드(노선, 역코드 포함)를 포함한 요청으로 역을 생성하고, 모든 응답 필드와 DB 상태를 상세히 검증한다")
    void save_WithValidRequest_SavesStationAndAllRelations() {
        // GIVEN: 역 생성에 필요한 모든 데이터 준비 (파일, 층, 노선, 역코드)
        Long drawingFileId = testFileUploader.initiateTestFileUpload("station.dwg");
        Long thumbnailFileId = testFileUploader.initiateTestFileUpload("thumbnail.png");
        Long line2Id = createAndSaveLine("2호선", "LINE_2").getId();
        Long lineSinbundangId = createAndSaveLine("신분당선", "LINE_SIN").getId();

        StationCreateRequest request =
                new StationCreateRequest(
                        new FacilityCreateRequest(
                                "강남역",
                                "GANGNAM_ST",
                                "2호선, 신분당선 환승역",
                                drawingFileId,
                                thumbnailFileId,
                                127.0276,
                                37.4979,
                                "{\"congestion\":\"high\"}"),
                        List.of(new FloorRequest("B1층", "-1"), new FloorRequest("B2층", "-2")),
                        List.of(line2Id, lineSinbundangId),
                        List.of("222", "D07"));

        // WHEN: 역 생성
        Long createdStationId = stationService.save(request);

        // THEN: 응답 DTO 검증
        StationResponse response = stationService.findById(createdStationId);
        assertThat(response.facility().id()).isEqualTo(createdStationId);
        assertThat(response.facility().name()).isEqualTo("강남역");
        assertThat(response.facility().code()).isEqualTo("GANGNAM_ST");
        assertThat(response.facility().description()).isEqualTo("2호선, 신분당선 환승역");
        assertThat(response.facility().drawing().id()).isEqualTo(drawingFileId);
        assertThat(response.facility().thumbnail().originalFileName()).isEqualTo("thumbnail.png");
        assertThat(response.facility().lon()).isEqualTo(127.0276);
        assertThat(response.facility().locationMeta()).isEqualTo("{\"congestion\":\"high\"}");
        assertThat(response.floors()).hasSize(2).extracting("name").containsExactly("B1층", "B2층");
        assertThat(response.stationInfo().lineIds())
                .containsExactlyInAnyOrder(line2Id, lineSinbundangId);
        assertThat(response.stationInfo().stationCodes()).containsExactlyInAnyOrder("222", "D07");

        // THEN: 데이터베이스 최종 상태 직접 검증
        Station savedStation = stationRepository.findById(createdStationId).orElseThrow();
        assertThat(savedStation.getName()).isEqualTo("강남역");
        assertThat(floorRepository.findAllByFacility(savedStation)).hasSize(2);
        List<StationLine> stationLines =
                stationLineRepository.findAll().stream()
                        .filter(e -> Objects.equals(e.getStation().getId(), savedStation.getId()))
                        .toList();
        assertThat(stationLines).hasSize(2);
        List<StationCode> stationCodes =
                stationCodeRepository.findAll().stream()
                        .filter(e -> Objects.equals(e.getStation().getId(), savedStation.getId()))
                        .toList();
        assertThat(stationCodes)
                .hasSize(2)
                .extracting(StationCode::getCode)
                .containsExactlyInAnyOrder("222", "D07");
    }

    @Test
    @DisplayName("성공: 선택적 필드(노선, 역코드, 층)가 null이거나 비어있을 때 역 생성이 성공한다")
    void save_WithNullOrEmptyOptionalFields_Succeeds() {
        // GIVEN: 필수 필드만 채운 요청
        StationCreateRequest request =
                new StationCreateRequest(
                        new FacilityCreateRequest("단일역", "SINGLE_ST", null, null, null, null, null, null),
                        List.of(),
                        null,
                        List.of() // 층, 노선, 역코드 정보 없음
                        );

        // WHEN: 역 생성
        Long createdStationId = stationService.save(request);

        // THEN: 생성된 역 정보 검증
        StationResponse response = stationService.findById(createdStationId);
        assertThat(response.facility().name()).isEqualTo("단일역");
        assertThat(response.floors()).isNotNull().isEmpty();
        assertThat(response.stationInfo().lineIds()).isNotNull().isEmpty();
        assertThat(response.stationInfo().stationCodes()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("실패: 중복된 코드로 역 생성 시 예외가 발생한다")
    void save_WithDuplicateCode_ThrowsCustomException() {
        // GIVEN: 기준 역 생성
        stationService.save(
                new StationCreateRequest(
                        new FacilityCreateRequest("기준역", "DUPE_CODE", null, null, null, null, null, null),
                        List.of(),
                        List.of(),
                        List.of()));

        StationCreateRequest duplicateRequest =
                new StationCreateRequest(
                        new FacilityCreateRequest("다른역", "DUPE_CODE", null, null, null, null, null, null),
                        List.of(),
                        List.of(),
                        List.of());

        // WHEN & THEN: 동일한 코드로 생성 시도 시 예외 발생
        assertThrows(CustomException.class, () -> stationService.save(duplicateRequest));
    }

    // --- Read Test ---

    @Test
    @DisplayName("성공: 전체 역 조회 시 상세 정보가 포함된 목록을 반환한다")
    void findAll_ReturnsListOfDetailedResponses() {
        // GIVEN: 2개의 서로 다른 역 생성
        Long line1Id = createAndSaveLine("1호선", "L1").getId();
        stationService.save(
                new StationCreateRequest(
                        new FacilityCreateRequest("역A", "STA_A", null, null, null, null, null, null),
                        List.of(),
                        List.of(line1Id),
                        List.of("133")));
        stationService.save(
                new StationCreateRequest(
                        new FacilityCreateRequest("역B", "STA_B", null, null, null, null, null, null),
                        List.of(),
                        List.of(),
                        List.of()));

        // WHEN: 전체 역 조회
        List<StationResponse> responses = stationService.findAll();

        // THEN: 목록 및 포함된 내용 검증
        assertThat(responses).hasSize(2);
        StationResponse stationA =
                responses.stream()
                        .filter(s -> s.facility().code().equals("STA_A"))
                        .findFirst()
                        .orElseThrow();
        assertThat(stationA.stationInfo().lineIds()).hasSize(1).contains(line1Id);
        assertThat(stationA.stationInfo().stationCodes()).hasSize(1).contains("133");
    }

    @Test
    @DisplayName("성공: 역이 없는 경우 전체 조회 시 빈 리스트를 반환한다")
    void findAll_WhenNoStationsExist_ReturnsEmptyList() {
        // GIVEN: 데이터 없음
        // WHEN
        List<StationResponse> responses = stationService.findAll();
        // THEN
        assertThat(responses).isNotNull().isEmpty();
    }

    // --- Update(putUpdate) Test ---

    @Test
    @DisplayName("성공(PUT): 모든 필드를 교체하는 수정 요청 시, null/빈리스트로 보낸 필드는 DB에서 삭제/초기화된다")
    void putUpdate_FullReplace_ReplacesAllFields() {
        // GIVEN: 원본 데이터 생성
        Long lineAId = createAndSaveLine("A노선", "LA").getId();
        Long lineBId = createAndSaveLine("B노선", "LB").getId();
        Long stationId =
                stationService.save(
                        new StationCreateRequest(
                                new FacilityCreateRequest("원본역", "ORI_ST", "설명", null, null, null, null, null),
                                List.of(new FloorRequest("1층", "1")),
                                List.of(lineAId),
                                List.of("A01")));
        Long lineCId = createAndSaveLine("C노선", "LC").getId();

        // GIVEN: 원본과 완전히 다른 교체 요청 (층, 노선, 역코드 모두 변경, 설명은 null로)
        StationUpdateRequest putRequest =
                new StationUpdateRequest(
                        new FacilityUpdateRequest("교체된역", "PUT_ST", null, null, null, null, null),
                        List.of(), // 층 정보 삭제
                        new StationUpdateInfo(List.of(lineBId, lineCId), List.of("B01", "C01")));

        // WHEN: PUT 업데이트 실행
        stationService.putUpdate(stationId, putRequest);

        // THEN: 응답 DTO 검증 (StationResponse 구조에 맞게 수정)
        StationResponse response = stationService.findById(stationId);
        assertThat(response.facility().name()).isEqualTo("교체된역");
        assertThat(response.facility().code()).isEqualTo("PUT_ST");
        assertThat(response.facility().description()).isNull(); // null로 교체됨
        assertThat(response.floors()).isEmpty(); // 빈 리스트로 교체됨
        assertThat(response.stationInfo().lineIds()).containsExactlyInAnyOrder(lineBId, lineCId);
        assertThat(response.stationInfo().stationCodes()).containsExactlyInAnyOrder("B01", "C01");

        // THEN: DB 직접 검증 (findAll + filter 방식으로 수정)
        assertThat(
                        floorRepository.findAll().stream()
                                .filter(f -> f.getFacility().getId().equals(stationId))
                                .toList())
                .isEmpty();

        List<StationLine> stationLines =
                stationLineRepository.findAll().stream()
                        .filter(sl -> sl.getStation().getId().equals(stationId))
                        .toList();
        assertThat(stationLines)
                .hasSize(2)
                .extracting(sl -> sl.getLine().getId())
                .containsExactlyInAnyOrder(lineBId, lineCId);
    }

    // --- Delete Test ---

    @Test
    @DisplayName("성공: 역을 삭제하면 해당 역과 하위 층, 노선/역코드 연결 정보가 모두 DB에서 삭제된다")
    void delete_RemovesStationAndAllAssociatedRelations() {
        // GIVEN: 층, 노선, 코드가 있는 역 생성
        Long lineId = createAndSaveLine("삭제될노선", "DEL_L").getId();
        Long stationId =
                stationService.save(
                        new StationCreateRequest(
                                new FacilityCreateRequest("삭제될역", "DEL_ST", null, null, null, null, null, null),
                                List.of(new FloorRequest("1층", "1")),
                                List.of(lineId),
                                List.of("DEL_C")));

        // GIVEN: DB에 관계 데이터가 있는지 확인 (findAll + filter 방식으로 수정)
        assertThat(stationRepository.findById(stationId)).isPresent();
        assertThat(
                        floorRepository.findAll().stream()
                                .anyMatch(f -> f.getFacility().getId().equals(stationId)))
                .isTrue();
        assertThat(
                        stationLineRepository.findAll().stream()
                                .anyMatch(sl -> sl.getStation().getId().equals(stationId)))
                .isTrue();
        assertThat(
                        stationCodeRepository.findAll().stream()
                                .anyMatch(sc -> sc.getStation().getId().equals(stationId)))
                .isTrue();

        // WHEN: 역 삭제
        stationService.delete(stationId);

        // THEN: 역과 모든 관계 데이터가 삭제되었는지 확인 (findAll + filter 방식으로 수정)
        assertThat(stationRepository.findById(stationId)).isEmpty();
        assertThat(
                        floorRepository.findAll().stream()
                                .noneMatch(f -> f.getFacility().getId().equals(stationId)))
                .isTrue();
        assertThat(
                        stationLineRepository.findAll().stream()
                                .noneMatch(sl -> sl.getStation().getId().equals(stationId)))
                .isTrue();
        assertThat(
                        stationCodeRepository.findAll().stream()
                                .noneMatch(sc -> sc.getStation().getId().equals(stationId)))
                .isTrue();
        assertThrows(CustomException.class, () -> stationService.findById(stationId));
    }

    // --- Relation Management Test (addLineToStation / removeLineFromStation) ---

    @Test
    @DisplayName("성공: 역에 노선을 추가하고, 중복 추가는 무시된다")
    void addLineToStation_AddsRelationAndIgnoresDuplicate() {
        // GIVEN
        Long stationId = createAndSaveSimpleStation("테스트역", "T_ST");
        Long lineId = createAndSaveLine("테스트노선", "T_L").getId();

        // WHEN: 첫 번째 추가
        stationService.addLineToStation(stationId, lineId);

        // THEN: 관계가 생성되었는지 확인 (findAll + filter 방식으로 수정)
        assertThat(
                        stationLineRepository.findAll().stream()
                                .filter(sl -> sl.getStation().getId().equals(stationId))
                                .toList())
                .hasSize(1);

        // WHEN: 두 번째 (중복) 추가
        stationService.addLineToStation(stationId, lineId);

        // THEN: 관계 개수가 변하지 않았는지 확인 (중복 무시)
        assertThat(
                        stationLineRepository.findAll().stream()
                                .filter(sl -> sl.getStation().getId().equals(stationId))
                                .toList())
                .hasSize(1);
    }

    @Test
    @DisplayName("성공: 역에서 노선을 제거한다")
    void removeLineFromStation_RemovesRelation() {
        // GIVEN: 노선이 연결된 역 생성
        Long lineId = createAndSaveLine("제거될노선", "REM_L").getId();
        Long stationId =
                stationService.save(
                        new StationCreateRequest(
                                new FacilityCreateRequest("역", "ST", null, null, null, null, null, null),
                                List.of(),
                                List.of(lineId),
                                List.of()));
        assertThat(
                        stationLineRepository.findAll().stream()
                                .anyMatch(sl -> sl.getStation().getId().equals(stationId)))
                .isTrue();

        // WHEN: 노선 제거
        stationService.removeLineFromStation(stationId, lineId);

        // THEN: 관계가 삭제되었는지 확인
        assertThat(
                        stationLineRepository.findAll().stream()
                                .noneMatch(sl -> sl.getStation().getId().equals(stationId)))
                .isTrue();
    }

    // --- Helper Methods ---

    private Line createAndSaveLine(String name, String color) {
        return lineRepository.save(Line.builder().name(name).color(color).build());
    }

    private Long createAndSaveSimpleStation(String name, String code) {
        return stationService.save(
                new StationCreateRequest(
                        new FacilityCreateRequest(name, code, null, null, null, null, null, null),
                        List.of(),
                        List.of(),
                        List.of()));
    }
}
