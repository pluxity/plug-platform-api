package com.pluxity.onboarding

import com.pluxity.global.exception.CustomException
import com.pluxity.onboarding.service.OnboardingLineService
import com.pluxity.station.Line
import com.pluxity.station.LineRepository
import com.pluxity.station.Station
import com.pluxity.station.StationLineRepository
import com.pluxity.station.StationRepository
import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class OnboardingStep8ComplexDomainTest
    @Autowired
    constructor(
        private val onboardingLineService: OnboardingLineService,
        private val stationRepository: StationRepository,
        private val lineRepository: LineRepository,
        private val stationLineRepository: StationLineRepository,
        private val entityManager: EntityManager,
    ) {
        private lateinit var station1: Station
        private lateinit var station2: Station
        private lateinit var line1: Line
        private lateinit var line2: Line
        private lateinit var line3: Line

        @BeforeEach
        fun setUp() {
            // 테스트 데이터 준비
            station1 = stationRepository.save(Station(name = "서울역", description = "테스트용"))
            station2 = stationRepository.save(Station(name = "시청역", description = "테스트용"))

            line1 = lineRepository.save(Line(name = "1호선", color = "#0052A4"))
            line2 = lineRepository.save(Line(name = "2호선", color = "#00A84D"))
            line3 = lineRepository.save(Line(name = "3호선", color = "#CE7C00"))

            entityManager.flush()
            entityManager.clear()
        }

        // ========== putStationLine 테스트 ==========

        @Test
        @DisplayName("유효한 요청으로 역에 노선 추가 시 정상적으로 연결된다")
        fun putStationLine_WithValidRequest_ConnectsLinesToStation() {
            // when
            val result = onboardingLineService.putStationLine(station1.id!!, listOf(line1.id!!, line2.id!!))

            // then
            assertThat(result).isEqualTo(station1.id)
            assertThat(stationLineRepository.findByStation(station1)).hasSize(2)
        }

        @Test
        @DisplayName("존재하지 않는 역 ID로 노선 추가 시 예외가 발생한다")
        fun putStationLine_WithNonExistingStationId_ThrowsCustomException() {
            // given
            val nonExistingStationId = 9999L

            // when & then
            val exception =
                assertThrows<CustomException> {
                    onboardingLineService.putStationLine(nonExistingStationId, listOf(line1.id!!))
                }
            assertThat(exception.errorCode.name).isEqualTo("NOT_FOUND_STATION")
        }

        @Test
        @DisplayName("존재하지 않는 노선 ID가 포함된 경우 예외가 발생한다")
        fun putStationLine_WithNonExistingLineId_ThrowsCustomException() {
            val nonExistingLineId = 9999L
            val exception =
                assertThrows<CustomException> {
                    onboardingLineService.putStationLine(station1.id!!, listOf(line1.id!!, nonExistingLineId))
                }
            assertThat(exception.errorCode.name).isEqualTo("NOT_FOUND_LINE")
        }

        @Test
        @DisplayName("이미 연결된 노선을 다시 추가하려고 하면 예외가 발생한다")
        fun putStationLine_WithAlreadyConnectedLine_ThrowsCustomException() {
            // given
            onboardingLineService.putStationLine(station1.id!!, listOf(line1.id!!))
            entityManager.flush()
            entityManager.clear()
            // when & then
            val exception =
                assertThrows<CustomException> {
                    onboardingLineService.putStationLine(station1.id!!, listOf(line1.id!!))
                }

            assertThat(exception.errorCode.name).isEqualTo("ALREADY_CONNECTED_LINE")
        }

        // ========== deleteStationLine 테스트 ==========

        @Test
        @DisplayName("유효한 요청으로 역에서 노선 삭제 시 정상적으로 제거된다")
        fun deleteStationLine_WithValidRequest_RemovesConnection() {
            // given
            onboardingLineService.putStationLine(station1.id!!, listOf(line1.id!!))
            entityManager.flush()
            entityManager.clear()

            // when
            onboardingLineService.deleteStationLine(station1.id!!, line1.id!!)

            // then
            assertThat(stationLineRepository.findByStation(station1)).isEmpty()
        }

        @Test
        @DisplayName("존재하지 않는 역 ID로 노선 삭제 시 예외가 발생한다")
        fun deleteStationLine_WithNonExistingStationId_ThrowsCustomException() {
            val nonExistingStationId = 9999L

            val exception =
                assertThrows<CustomException> {
                    onboardingLineService.deleteStationLine(nonExistingStationId, line1.id!!)
                }

            assertThat(exception.errorCode.name).isEqualTo("NOT_FOUND_STATION")
        }

        @Test
        @DisplayName("존재하지 않는 노선 ID로 삭제 시 예외가 발생한다")
        fun deleteStationLine_WithNonExistingLineId_ThrowsCustomException() {
            val nonexistingLineId = 9999L

            val exception =
                assertThrows<CustomException> {
                    onboardingLineService.deleteStationLine(station1.id!!, nonexistingLineId)
                }
            assertThat(exception.errorCode.name).isEqualTo("NOT_FOUND_LINE")
        }

        @Test
        @DisplayName("연결되지 않은 역-노선 관계를 삭제하려고 하면 예외가 발생한다")
        fun deleteStationLine_WithNonExistingConnection_ThrowsCustomException() {
            // when & then
            val exception =
                assertThrows<CustomException> {
                    onboardingLineService.deleteStationLine(station1.id!!, line1.id!!)
                }
            assertThat(exception.errorCode.name).isEqualTo("NOT_FOUND_STATION_LINE")
        }

        // ========== findStationTwoLine 테스트 ==========

        @Test
        @DisplayName("환승역이 있을 때 2개 이상의 노선이 연결된 역 목록이 반환된다")
        fun findStationTwoLine_WithTransferStations_ReturnsTransferStations() {
            // given
            onboardingLineService.putStationLine(station1.id!!, listOf(line1.id!!, line2.id!!))
            onboardingLineService.putStationLine(station2.id!!, listOf(line1.id!!))
            entityManager.flush()
            entityManager.clear()

            // when
            val result = onboardingLineService.findStationTwoLine()

            // then
            assertThat(result).hasSize(1) // station1만 환승역
            assertThat(result[0].name).isEqualTo("서울역")
            assertThat(result[0].lines).hasSize(2)
        }

        @Test
        @DisplayName("환승역이 없을 때 빈 리스트가 반환된다")
        fun findStationTwoLine_WithoutTransferStations_ReturnsEmptyList() {
            // given
            // 모든 역에 노선을 1개씩만 연결 (환승역 없음)
            onboardingLineService.putStationLine(station1.id!!, listOf(line1.id!!))
            onboardingLineService.putStationLine(station2.id!!, listOf(line2.id!!))
            entityManager.flush()
            entityManager.clear()

            // when
            val result = onboardingLineService.findStationTwoLine()

            // then
            assertThat(result).isEmpty()
        }
    }
