package com.pluxity.onboarding

import com.pluxity.station.Line
import com.pluxity.station.LineRepository
import com.pluxity.station.Station
import com.pluxity.station.StationLine
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
import org.springframework.dao.InvalidDataAccessResourceUsageException
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
@DisplayName("@SoftDelete와 @Inheritance(JOINED) 문제 재현 테스트")
class SoftDeleteJoinedInheritanceTest
    @Autowired
    constructor(
        private val stationRepository: StationRepository,
        private val lineRepository: LineRepository,
        private val stationLineRepository: StationLineRepository,
        private val entityManager: EntityManager,
    ) {
        private lateinit var station: Station
        private lateinit var line: Line
        private lateinit var stationLine: StationLine

        @BeforeEach
        fun setUp() {
            // Station 생성
            station = stationRepository.save(Station(name = "테스트역", description = "테스트용 역"))
            entityManager.flush()
            entityManager.clear()

            // Line 생성
            line = lineRepository.save(Line(name = "1호선", color = "#0052A4"))
            entityManager.flush()
            entityManager.clear()

            // StationLine 생성
            stationLine =
                stationLineRepository.save(
                    StationLine(
                        station = station,
                        line = line,
                    ),
                )
            entityManager.flush()
            entityManager.clear()
        }

        @Test
        @DisplayName("existsByStationId()는 부모 테이블 JOIN 누락으로 예외 발생")
        fun existsByStationId_whenUsingId_thenMissingFromClauseError() {
            // When & Then: ID로 조회 시 SQL 오류 발생
            assertThrows<InvalidDataAccessResourceUsageException> {
                stationLineRepository.existsByStationId(station.id!!)
            }.also { exception ->
                // ERROR: missing FROM-clause entry for table "s1_1"
                assertThat(exception.message).contains("missing FROM-clause")
            }
        }

        @Test
        @DisplayName("existsByStation()는 엔티티 기반 조회로 정상 동작")
        fun existsByStation_whenUsingEntity_thenWorksWithJoin() {
            // When: 엔티티 객체로 조회
            val station = stationRepository.findById(this.station.id!!).get()
            val exists = stationLineRepository.existsByStation(station)

            // Then: 정상 작동
            assertThat(exists).isTrue()
        }

        @Test
        @DisplayName("findByStationId()도 부모 테이블 JOIN 누락으로 예외 발생")
        fun findByStationId_whenUsingId_thenMissingFromClauseError() {
            assertThrows<InvalidDataAccessResourceUsageException> {
                stationLineRepository.findByStationId(station.id!!)
            }.also { exception ->
                assertThat(exception.message).contains("missing FROM-clause")
            }
        }

        @Test
        @DisplayName("findByStationIdWithQuery()는 JPQL 명시적 JOIN으로 정상 동작")
        fun findByStationIdWithQuery_whenUsingExplicitJoin_thenSuccess() {
            val result = stationLineRepository.findByStationIdWithQuery(station.id!!)
            assertThat(result).isNotNull
        }
    }
