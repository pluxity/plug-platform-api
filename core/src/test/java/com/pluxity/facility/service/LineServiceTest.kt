package com.pluxity.facility.service

import com.pluxity.global.exception.CustomException
import com.pluxity.station.LineRepository
import com.pluxity.station.LineService
import com.pluxity.station.Station
import com.pluxity.station.StationLineService
import com.pluxity.station.StationRepository
import com.pluxity.station.StationService
import com.pluxity.station.dto.LineCreateRequest
import com.pluxity.station.dto.LineResponse
import com.pluxity.station.dto.LineUpdateRequest
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@DisplayName("LineService 통합 테스트")
internal class LineServiceTest {
    @Autowired
    lateinit var lineService: LineService

    @Autowired
    lateinit var lineRepository: LineRepository

    @Autowired
    lateinit var stationRepository: StationRepository

    @Autowired
    lateinit var stationService: StationService

    @Autowired
    lateinit var stationLineService: StationLineService

    lateinit var lineCreateRequest: LineCreateRequest

    @BeforeEach
    fun setUp() {
        lineCreateRequest = LineCreateRequest("1호선", "#0052A4")
    }

    @Test
    @DisplayName("유효한 요청으로 호선 생성 시 모든 필드가 정확히 저장된다")
    fun save_WithValidRequest_SavesLineAndReturnsResponse() {
        // when
        val id = lineService.save(lineCreateRequest)

        // then
        Assertions.assertThat(id).isNotNull()
        val savedLine = lineService.findById(id)

        Assertions.assertThat(savedLine.id).isEqualTo(id)
        Assertions.assertThat(savedLine.name).isEqualTo("1호선")
        Assertions.assertThat(savedLine.color).isEqualTo("#0052A4")
        Assertions.assertThat(savedLine.stationIds).isNotNull().isEmpty()
        Assertions.assertThat(savedLine.baseResponse.createdAt).isNotNull()
        Assertions.assertThat(savedLine.baseResponse.updatedAt).isNotNull()
    }

    @Test
    @DisplayName("색상(color) 필드가 null 이어도 호선 생성이 성공한다")
    fun save_WithNullColor_SavesSuccessfully() {
        // given
        val requestWithNullColor = LineCreateRequest("분당선", null)

        // when
        val id = lineService.save(requestWithNullColor)

        // then
        Assertions.assertThat(id).isNotNull()
        val savedLine = lineService.findById(id)
        Assertions.assertThat(savedLine.name).isEqualTo("분당선")
        Assertions.assertThat(savedLine.color).isNull()
    }

    @Test
    @DisplayName("이미 존재하는 이름으로 호선 생성 시 중복 예외가 발생한다")
    fun save_WithDuplicateName_ThrowsCustomException() {
        // given
        lineService.save(lineCreateRequest) // "1호선" 미리 저장
        val duplicateNameRequest = LineCreateRequest("1호선", "#FFFFFF")

        // when & then
        assertThatThrownBy {
            lineService.save(duplicateNameRequest)
        }.isInstanceOf(CustomException::class.java)
    }

    @Test
    @DisplayName("ID로 호선 조회 시 정확한 정보가 반환된다")
    fun findById_WithExistingId_ReturnsCorrectLineResponse() {
        // given
        val id = lineService.save(lineCreateRequest)

        // when
        val response = lineService.findById(id)

        // then
        Assertions.assertThat(response).isNotNull()
        Assertions.assertThat(response.id).isEqualTo(id)
        Assertions.assertThat(response.name).isEqualTo("1호선")
        Assertions.assertThat(response.color).isEqualTo("#0052A4")
        Assertions.assertThat(response.stationIds).isEmpty()
    }

    @Test
    @DisplayName("연결된 역이 있는 호선 조회 시 역 ID 목록이 포함되어 반환된다")
    fun findById_WithAssociatedStations_ReturnsLineWithStationIds() {
        // given
        val lineId = lineService.save(lineCreateRequest)
        val station1 = stationRepository.save(Station.builder().name("서울역").build())
        val station2 = stationRepository.save(Station.builder().name("시청역").build())

        stationService.addLineToStation(station1.id, lineId)
        stationService.addLineToStation(station2.id, lineId)

        // when
        val response = lineService.findById(lineId)

        // then
        Assertions.assertThat(response.stationIds)
            .hasSize(2)
            .containsExactlyInAnyOrder(station1.id, station2.id)
    }

    @Test
    @DisplayName("존재하지 않는 ID로 호선 조회 시 예외가 발생한다")
    fun findById_WithNonExistingId_ThrowsCustomException() {
        // given
        val nonExistingId = 9999L

        // when & then
        assertThatThrownBy {
            lineService.findById(nonExistingId)
        }.isInstanceOf(CustomException::class.java)
    }

    @Test
    @DisplayName("모든 호선 조회 시 전체 목록이 반환된다")
    fun findAll_ReturnsListOfAllLineResponses() {
        // given
        lineService.save(LineCreateRequest("1호선", "#0052A4"))
        lineService.save(LineCreateRequest("2호선", "#00A84D"))

        // when
        val responses: List<LineResponse> = lineService.findAll()

        // then
        Assertions.assertThat(responses).hasSize(2)
        org.junit.jupiter.api.Assertions.assertTrue(
            responses.stream().anyMatch { line: LineResponse -> line.name == "1호선" },
        )
        org.junit.jupiter.api.Assertions.assertTrue(
            responses.stream().anyMatch { line: LineResponse -> line.name == "2호선" },
        )
    }

    @Test
    @DisplayName("호선 데이터가 없을 때 전체 조회 시 빈 리스트가 반환된다")
    fun findAll_WhenEmpty_ReturnsEmptyList() {
        // when
        val responses: List<LineResponse> = lineService.findAll()

        // then
        Assertions.assertThat(responses).isNotNull().isEmpty()
    }

    @Test
    @DisplayName("유효한 요청으로 호선 정보 전체 수정 시 모든 정보가 업데이트된다")
    fun update_WithValidRequest_UpdatesAllFields() {
        // given
        val id = lineService.save(lineCreateRequest)
        val updateRequest = LineUpdateRequest("신분당선", "#D4003B")

        // when
        lineService.update(id, updateRequest)

        // then
        val updatedLine = lineService.findById(id)
        Assertions.assertThat(updatedLine.name).isEqualTo("신분당선")
        Assertions.assertThat(updatedLine.color).isEqualTo("#D4003B")
    }

    @Test
    @DisplayName("부분 업데이트: 이름만 변경 시 다른 필드는 유지된다")
    fun update_Partial_OnlyName() {
        // given
        val id = lineService.save(lineCreateRequest) // name: 1호선, color: #0052A4
        val updateRequest = LineUpdateRequest("경의중앙선", null)

        // when
        lineService.update(id, updateRequest)

        // then
        val updatedLine = lineService.findById(id)
        Assertions.assertThat(updatedLine.name).isEqualTo("경의중앙선")
        Assertions.assertThat(updatedLine.color).isEqualTo("#0052A4")
    }

    @Test
    @DisplayName("부분 업데이트: 색상만 변경 시 다른 필드는 유지된다")
    fun update_Partial_OnlyColor() {
        // given
        val id = lineService.save(lineCreateRequest) // name: 1호선, color: #0052A4
        val updateRequest = LineUpdateRequest(null, "#747F00")

        // when
        lineService.update(id, updateRequest)

        // then
        val updatedLine = lineService.findById(id)
        Assertions.assertThat(updatedLine.name).isEqualTo("1호선")
        Assertions.assertThat(updatedLine.color).isEqualTo("#747F00")
    }

    @Test
    @DisplayName("다른 호선에 이미 존재하는 이름으로 수정 시 예외가 발생한다")
    fun update_ToDuplicateName_ThrowsCustomException() {
        // given
        lineService.save(LineCreateRequest("2호선", "#00A84D"))
        val idToUpdate = lineService.save(lineCreateRequest) // 1호선
        val updateRequest = LineUpdateRequest("2호선", "#FFFFFF")

        // when & then
        assertThatThrownBy {
            lineService.update(idToUpdate, updateRequest)
        }.isInstanceOf(CustomException::class.java)
    }

    @Test
    @DisplayName("존재하지 않는 ID의 호선 수정 시 예외가 발생한다")
    fun update_WithNonExistingId_ThrowsCustomException() {
        // given
        val nonExistingId = 9999L
        val updateRequest = LineUpdateRequest("없는 노선", "#000000")

        // when & then
        assertThatThrownBy {
            lineService.update(nonExistingId, updateRequest)
        }.isInstanceOf(CustomException::class.java)
    }

    @Test
    @DisplayName("ID로 호선 삭제 시 데이터가 삭제되고 더 이상 조회되지 않는다")
    fun delete_WithExistingId_DeletesLine() {
        // given
        val id = lineService.save(lineCreateRequest)
        Assertions.assertThat(lineRepository.existsById(id)).isTrue()

        // when
        lineService.delete(id)

        // then
        Assertions.assertThat(lineRepository.existsById(id)).isFalse()
        assertThatThrownBy {
            lineService.findById(id)
        }.isInstanceOf(CustomException::class.java)
    }

    @Test
    @DisplayName("존재하지 않는 ID로 호선 삭제 시 예외가 발생한다")
    fun delete_WithNonExistingId_ThrowsCustomException() {
        // given
        val nonExistingId = 9999L
        Assertions.assertThat(lineRepository.existsById(nonExistingId)).isFalse()

        // when & then
        assertThatThrownBy {
            lineService.delete(nonExistingId)
        }.isInstanceOf(CustomException::class.java)
    }

    @Test
    @DisplayName("호선을 삭제하면 연결된 역과의 관계(StationLine)도 모두 해제된다")
    fun deleteLine_DeletesAllRelatedStationLines() {
        // given
        val lineId = lineService.save(lineCreateRequest)
        val station = stationRepository.save(Station.builder().name("테스트역").build())
        stationService.addLineToStation(station.id, lineId)

        // 관계 설정 확인
        val linesByStation = stationLineService.findLinesByStation(station)
        Assertions.assertThat(linesByStation).contains(lineId)

        // when
        lineService.delete(lineId)

        // then
        // 호선 삭제 확인
        assertThatThrownBy {
            lineService.findById(lineId)
        }.isInstanceOf(CustomException::class.java)

        // 역은 여전히 존재하지만 라인 관계는 제거되어야 함
        val stillExistingStation = stationRepository.findById(station.id).orElseThrow()
        val linesAfterDelete = stationLineService.findLinesByStation(stillExistingStation)
        Assertions.assertThat(linesAfterDelete).isEmpty()
    }
}
