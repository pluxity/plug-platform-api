package com.pluxity.facility.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.pluxity.global.exception.CustomException;
import com.pluxity.station.*;
import com.pluxity.station.dto.LineCreateRequest;
import com.pluxity.station.dto.LineResponse;
import com.pluxity.station.dto.LineUpdateRequest;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@DisplayName("LineService 통합 테스트")
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

    @BeforeEach
    void setUp() {
        lineCreateRequest = new LineCreateRequest("1호선", "#0052A4");
    }


        @Test
        @DisplayName("유효한 요청으로 호선 생성 시 모든 필드가 정확히 저장된다")
        void save_WithValidRequest_SavesLineAndReturnsResponse() {
            // when
            Long id = lineService.save(lineCreateRequest);

            // then
            assertThat(id).isNotNull();
            LineResponse savedLine = lineService.findById(id);

            assertThat(savedLine.id()).isEqualTo(id);
            assertThat(savedLine.name()).isEqualTo("1호선");
            assertThat(savedLine.color()).isEqualTo("#0052A4");
            assertThat(savedLine.stationIds()).isNotNull().isEmpty();
            assertThat(savedLine.baseResponse().createdAt()).isNotNull();
            assertThat(savedLine.baseResponse().updatedAt()).isNotNull();
        }

        @Test
        @DisplayName("색상(color) 필드가 null 이어도 호선 생성이 성공한다")
        void save_WithNullColor_SavesSuccessfully() {
            // given
            LineCreateRequest requestWithNullColor = new LineCreateRequest("분당선", null);

            // when
            Long id = lineService.save(requestWithNullColor);

            // then
            assertThat(id).isNotNull();
            LineResponse savedLine = lineService.findById(id);
            assertThat(savedLine.name()).isEqualTo("분당선");
            assertThat(savedLine.color()).isNull();
        }

        @Test
        @DisplayName("이미 존재하는 이름으로 호선 생성 시 중복 예외가 발생한다")
        void save_WithDuplicateName_ThrowsCustomException() {
            // given
            lineService.save(lineCreateRequest); // "1호선" 미리 저장
            LineCreateRequest duplicateNameRequest = new LineCreateRequest("1호선", "#FFFFFF");

            // when & then
            assertThrows(CustomException.class, () -> lineService.save(duplicateNameRequest));
        }

        @Test
        @DisplayName("ID로 호선 조회 시 정확한 정보가 반환된다")
        void findById_WithExistingId_ReturnsCorrectLineResponse() {
            // given
            Long id = lineService.save(lineCreateRequest);

            // when
            LineResponse response = lineService.findById(id);

            // then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(id);
            assertThat(response.name()).isEqualTo("1호선");
            assertThat(response.color()).isEqualTo("#0052A4");
            assertThat(response.stationIds()).isEmpty();
        }

        @Test
        @DisplayName("연결된 역이 있는 호선 조회 시 역 ID 목록이 포함되어 반환된다")
        void findById_WithAssociatedStations_ReturnsLineWithStationIds() {
            // given
            Long lineId = lineService.save(lineCreateRequest);
            Station station1 = stationRepository.save(Station.builder().name("서울역").build());
            Station station2 = stationRepository.save(Station.builder().name("시청역").build());

            stationService.addLineToStation(station1.getId(), lineId);
            stationService.addLineToStation(station2.getId(), lineId);

            // when
            LineResponse response = lineService.findById(lineId);

            // then
            assertThat(response.stationIds()).hasSize(2)
                    .containsExactlyInAnyOrder(station1.getId(), station2.getId());
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
        @DisplayName("모든 호선 조회 시 전체 목록이 반환된다")
        void findAll_ReturnsListOfAllLineResponses() {
            // given
            lineService.save(new LineCreateRequest("1호선", "#0052A4"));
            lineService.save(new LineCreateRequest("2호선", "#00A84D"));

            // when
            List<LineResponse> responses = lineService.findAll();

            // then
            assertThat(responses).hasSize(2);
            assertTrue(responses.stream().anyMatch(line -> line.name().equals("1호선")));
            assertTrue(responses.stream().anyMatch(line -> line.name().equals("2호선")));
        }

        @Test
        @DisplayName("호선 데이터가 없을 때 전체 조회 시 빈 리스트가 반환된다")
        void findAll_WhenEmpty_ReturnsEmptyList() {
            // when
            List<LineResponse> responses = lineService.findAll();

            // then
            assertThat(responses).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("유효한 요청으로 호선 정보 전체 수정 시 모든 정보가 업데이트된다")
        void update_WithValidRequest_UpdatesAllFields() {
            // given
            Long id = lineService.save(lineCreateRequest);
            LineUpdateRequest updateRequest = new LineUpdateRequest("신분당선", "#D4003B");

            // when
            lineService.update(id, updateRequest);

            // then
            LineResponse updatedLine = lineService.findById(id);
            assertThat(updatedLine.name()).isEqualTo("신분당선");
            assertThat(updatedLine.color()).isEqualTo("#D4003B");
        }

        @Test
        @DisplayName("부분 업데이트: 이름만 변경 시 다른 필드는 유지된다")
        void update_Partial_OnlyName() {
            // given
            Long id = lineService.save(lineCreateRequest); // name: 1호선, color: #0052A4
            LineUpdateRequest updateRequest = new LineUpdateRequest("경의중앙선", null);

            // when
            lineService.update(id, updateRequest);

            // then
            LineResponse updatedLine = lineService.findById(id);
            assertThat(updatedLine.name()).isEqualTo("경의중앙선");
            assertThat(updatedLine.color()).isEqualTo("#0052A4");
        }

        @Test
        @DisplayName("부분 업데이트: 색상만 변경 시 다른 필드는 유지된다")
        void update_Partial_OnlyColor() {
            // given
            Long id = lineService.save(lineCreateRequest); // name: 1호선, color: #0052A4
            LineUpdateRequest updateRequest = new LineUpdateRequest(null, "#747F00");

            // when
            lineService.update(id, updateRequest);

            // then
            LineResponse updatedLine = lineService.findById(id);
            assertThat(updatedLine.name()).isEqualTo("1호선");
            assertThat(updatedLine.color()).isEqualTo("#747F00");
        }

        @Test
        @DisplayName("다른 호선에 이미 존재하는 이름으로 수정 시 예외가 발생한다")
        void update_ToDuplicateName_ThrowsCustomException() {
            // given
            lineService.save(new LineCreateRequest("2호선", "#00A84D"));
            Long idToUpdate = lineService.save(lineCreateRequest); // 1호선
            LineUpdateRequest updateRequest = new LineUpdateRequest("2호선", "#FFFFFF");

            // when & then
            assertThrows(CustomException.class, () -> lineService.update(idToUpdate, updateRequest));
        }

        @Test
        @DisplayName("존재하지 않는 ID의 호선 수정 시 예외가 발생한다")
        void update_WithNonExistingId_ThrowsCustomException() {
            // given
            Long nonExistingId = 9999L;
            LineUpdateRequest updateRequest = new LineUpdateRequest("없는 노선", "#000000");

            // when & then
            assertThrows(CustomException.class, () -> lineService.update(nonExistingId, updateRequest));
        }

        @Test
        @DisplayName("ID로 호선 삭제 시 데이터가 삭제되고 더 이상 조회되지 않는다")
        void delete_WithExistingId_DeletesLine() {
            // given
            Long id = lineService.save(lineCreateRequest);
            assertThat(lineRepository.existsById(id)).isTrue();

            // when
            lineService.delete(id);

            // then
            assertThat(lineRepository.existsById(id)).isFalse();
            assertThrows(CustomException.class, () -> lineService.findById(id));
        }

        @Test
        @DisplayName("존재하지 않는 ID로 호선 삭제 시 예외가 발생한다")
        void delete_WithNonExistingId_ThrowsCustomException() {
            // given
            Long nonExistingId = 9999L;
            assertThat(lineRepository.existsById(nonExistingId)).isFalse();

            // when & then
            assertThrows(CustomException.class, () -> lineService.delete(nonExistingId));
        }

        @Test
        @DisplayName("호선을 삭제하면 연결된 역과의 관계(StationLine)도 모두 해제된다")
        void deleteLine_DeletesAllRelatedStationLines() {
            // given
            Long lineId = lineService.save(lineCreateRequest);
            Station station = stationRepository.save(Station.builder().name("테스트역").build());
            stationService.addLineToStation(station.getId(), lineId);

            // 관계 설정 확인
            List<Long> linesByStation = stationLineService.findLinesByStation(station);
            assertThat(linesByStation).contains(lineId);

            // when
            lineService.delete(lineId);

            // then
            // 호선 삭제 확인
            assertThrows(CustomException.class, () -> lineService.findById(lineId));

            // 역은 여전히 존재하지만 라인 관계는 제거되어야 함
            Station stillExistingStation = stationRepository.findById(station.getId()).orElseThrow();
            List<Long> linesAfterDelete = stationLineService.findLinesByStation(stillExistingStation);
            assertThat(linesAfterDelete).isEmpty();
    }
}