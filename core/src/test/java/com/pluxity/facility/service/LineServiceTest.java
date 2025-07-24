package com.pluxity.facility.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.pluxity.global.exception.CustomException;
import com.pluxity.station.*;
import com.pluxity.station.dto.LineCreateRequest;
import com.pluxity.station.dto.LineResponse;
import com.pluxity.station.dto.LineUpdateRequest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class LineServiceTest {

    @Autowired
    private LineService lineService;

    @Autowired
    private LineRepository lineRepository;

    @Autowired
    private StationRepository stationRepository;

    @Autowired
    private StationService stationService;

    @Autowired
    private StationLineService stationLineService;

    private LineCreateRequest lineCreateRequest;
    private Station testStation;

    @BeforeEach
    void setUp() {
        // 테스트용 LineRequest 생성
        lineCreateRequest = new LineCreateRequest("테스트 노선", "#FF0000");
        
        // 테스트용 Station 생성 - 실제 저장은 필요한 테스트에서만 수행
        testStation = Station.builder()
                .name("테스트 역")
                .description("테스트 역 설명")
                .build();
    }

    @Test
    @DisplayName("유효한 요청으로 호선 생성 시 호선이 저장된다")
    void save_WithValidRequest_SavesLine() {
        // when
        Long id = lineService.save(lineCreateRequest);

        // then
        assertThat(id).isNotNull();
        
        // 저장된 호선 확인
        LineResponse savedLine = lineService.findById(id);
        assertThat(savedLine).isNotNull();
        assertThat(savedLine.name()).isEqualTo("테스트 노선");
        assertThat(savedLine.color()).isEqualTo("#FF0000");
        assertThat(savedLine.stationIds()).isEmpty();
    }

    @Test
    @DisplayName("여러 개의 호선을 생성 및 조회할 수 있다")
    void save_MultipleLines_AllLinesAreStored() {
        // given
        int count = 5;
        List<Long> lineIds = new ArrayList<>();
        
        // when
        for (int i = 0; i < count; i++) {
            LineCreateRequest request = new LineCreateRequest("테스트 노선 " + i, "#" + i + "00000");
            Long id = lineService.save(request);
            lineIds.add(id);
        }
        
        // then
        List<LineResponse> responses = lineService.findAll();
        
        // 적어도 우리가 저장한 개수만큼은 있어야 함
        assertThat(responses.size()).isGreaterThanOrEqualTo(count);
        
        // 우리가 저장한 ID들이 모두 있는지 확인
        for (Long id : lineIds) {
            assertThat(responses.stream().anyMatch(line -> line.id().equals(id))).isTrue();
        }
    }

    @Test
    @DisplayName("동일한 이름의 호선 생성 시 중복 검증을 통과한다 (구현에 따라 다를 수 있음)")
    void save_WithDuplicateName_BehaviorAsExpected() {
        // given
        Long id1 = lineService.save(lineCreateRequest);
        LineCreateRequest duplicateNameRequest = new LineCreateRequest(lineCreateRequest.name(), "#00FF00");
        
        // when & then
        // 현재 구현에서는 중복 검사가 없으므로 성공해야 함
        assertThrows(CustomException.class, () -> lineService.save(duplicateNameRequest));
//        Long id2 = lineService.save(duplicateNameRequest);
//        assertThat(id2).isNotNull();
//        assertThat(id2).isNotEqualTo(id1);
//
//        // 두 노선 모두 조회 가능해야 함
//        LineResponse line1 = lineService.findById(id1);
//        LineResponse line2 = lineService.findById(id2);
//
//        assertThat(line1.name()).isEqualTo(line2.name());
//        assertThat(line1.color()).isNotEqualTo(line2.color());
    }

    @Test
    @DisplayName("모든 호선 조회 시 호선 목록이 반환된다")
    void findAll_ReturnsListOfLineResponses() {
        // given
        Long id = lineService.save(lineCreateRequest);
        
        // when
        List<LineResponse> responses = lineService.findAll();

        // then
        assertThat(responses).isNotEmpty();
        assertThat(responses.stream().anyMatch(line -> line.name().equals("테스트 노선"))).isTrue();
    }

    @Test
    @DisplayName("ID로 호선 조회 시 호선 정보가 반환된다")
    void findById_WithExistingId_ReturnsLineResponse() {
        // given
        Long id = lineService.save(lineCreateRequest);

        // when
        LineResponse response = lineService.findById(id);

        // then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo("테스트 노선");
        assertThat(response.color()).isEqualTo("#FF0000");
    }

    @Test
    @DisplayName("존재하지 않는 ID로 호선 조회 시 예외가 발생한다")
    void findById_WithNonExistingId_ThrowsCustomException() {
        // given
        Long nonExistingId = 9999L;

        // when & then
        assertThrows(CustomException.class, () -> lineService.findById(nonExistingId));
    }

    @Test
    @DisplayName("유효한 요청으로 호선 정보 수정 시 호선 정보가 업데이트된다")
    void update_WithValidRequest_UpdatesLine() {
        // given
        Long id = lineService.save(lineCreateRequest);
        LineUpdateRequest updateRequest = new LineUpdateRequest("수정된 노선", "#00FFFF");

        // when
        lineService.update(id, updateRequest);

        // then
        LineResponse updatedLine = lineService.findById(id);
        assertThat(updatedLine.name()).isEqualTo("수정된 노선");
        assertThat(updatedLine.color()).isEqualTo("#00FFFF");
    }

    @Test
    @DisplayName("호선 삭제 시 데이터가 삭제된다")
    void delete_WithExistingId_DeletesLine() {
        // given
        Long id = lineService.save(lineCreateRequest);
        
        // when
        LineResponse response = lineService.findById(id);
        assertThat(response).isNotNull();
        
        // then
        lineService.delete(id);
        
        // 삭제 후에는 해당 ID로 호선을 찾을 수 없어야 함
        assertThrows(CustomException.class, () -> lineService.findById(id));
    }

    @Test
    @DisplayName("역을 호선에서 제거하면 관계가 해제된다")
    void removeStationFromLine_RemovesRelationship() {
        // given
        Long lineId = lineService.save(lineCreateRequest);
        Station savedStation = stationRepository.save(testStation);
        stationService.addLineToStation(savedStation.getId(), lineId);

        // 관계 설정 확인
        Line line = lineRepository.findById(lineId).orElseThrow();
        Station station = stationRepository.findById(savedStation.getId()).orElseThrow();
        List<Station> stations = line.getStations();
        List<Long> linesByStation = stationLineService.findLinesByStation(station);
        assertThat(stations).contains(station);
        assertThat(linesByStation.stream()
                .anyMatch(sl -> sl.equals(lineId)))
                .isTrue();

        // when
        stationService.removeLineFromStation(savedStation.getId(), lineId);

        // then
        // 라인을 다시 조회하여 스테이션이 없는지 확인
        line = lineRepository.findById(lineId).orElseThrow();
        station = stationRepository.findById(savedStation.getId()).orElseThrow();
        List<Long> lines = stationLineService.findLinesByStation(station);

        assertThat(line.getStations()).doesNotContain(station);
        assertThat(lines).isEmpty();

        // 서비스 메서드를 통한 조회 테스트
        List<Long> stationIds = lineService.findStationsByLineId(lineId);
        assertThat(stationIds).doesNotContain(savedStation.getId());
    }

    @Test
    @DisplayName("호선을 삭제하면 연결된 역의 관계도 해제된다")
    void deleteLine_RelatedStationsRelationshipUpdated() {
        // given
        Long lineId = lineService.save(lineCreateRequest);
        Station savedStation = stationRepository.save(testStation);
        stationService.addLineToStation(savedStation.getId(), lineId);

        // 관계 설정 확인
        Line line = lineRepository.findById(lineId).orElseThrow();
        Station station = stationRepository.findById(savedStation.getId()).orElseThrow();
        assertThat(line.getStations()).contains(station);
        List<Long> linesByStation = stationLineService.findLinesByStation(station);
        assertThat(linesByStation.stream()
                .anyMatch(sl -> sl.equals(lineId)))
                .isTrue();

        // when
        lineService.delete(lineId);

        // then
        // 호선은 삭제되었으므로 조회 시 예외 발생
        assertThrows(CustomException.class, () -> lineService.findById(lineId));

        // 역은 여전히 존재하지만 라인 관계는 제거되어야 함
        station = stationRepository.findById(savedStation.getId()).orElseThrow();
        List<Long> lines = stationLineService.findLinesByStation(station);
        assertThat(lines).isEmpty();
    }
} 