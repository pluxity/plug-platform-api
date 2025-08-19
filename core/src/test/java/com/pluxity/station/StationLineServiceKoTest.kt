package com.pluxity.station

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.station.entity.dummyLine
import com.pluxity.station.entity.dummyStation
import com.pluxity.station.entity.dummyStationLine
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify

class StationLineServiceKoTest : BehaviorSpec({

    val stationLineRepository: StationLineRepository = mockk()
    val stationLineService = StationLineService(stationLineRepository)

    Given("StationLine 생성을 진행할 때") {
        When("유효한 요청으로 StationLine 생성 요청") {
            val station = dummyStation()
            val line = dummyLine()
            val stationLine = dummyStationLine()

            every { stationLineRepository.save(any()) } returns stationLine

            Then("성공") {
                stationLineService.save(station, line)
                verify(exactly = 1) { stationLineRepository.save(any()) }
            }
        }
    }

    Given("Station에 연결된 노선 조회를 진행할 때") {
        When("유효한 요청으로 연결된 노선 조회 요청") {
            val station = dummyStation()
            val stationLine = dummyStationLine()

            every { stationLineRepository.findByStationOrderByCreatedAtDesc(any()) } returns listOf(stationLine)

            Then("성공") {
                val res = stationLineService.findLinesByStation(station)
                res.size shouldBe 1
                res.first() shouldBe stationLine.line.id
            }
        }
    }

    Given("Station에 연결된 노선 전체 삭제를 진행할 때") {
        When("유효한 요청으로 연결된 노선 삭제 요청") {
            val station = dummyStation()

            every { stationLineRepository.deleteByStation(any()) } just runs

            Then("성공") {
                stationLineService.deleteByStation(station)
                verify(exactly = 1) { stationLineRepository.deleteByStation(any()) }
            }
        }
    }

    Given("Station에 연결된 노선 정보 조회를 station별로 진행할 때") {
        When("Station 값이 없을때") {
            Then("Empty Map 반환") {
                val res = stationLineService.findLineMapByStationIds(listOf())
                res shouldBe emptyMap()
            }
        }

        When("유효한 요청으로 연결된 노선 조회 요청") {
            val station = dummyStation()
            val stationLine = dummyStationLine(station = station)

            every { stationLineRepository.findByStationInOrderByCreatedAtDesc(any()) } returns listOf(stationLine)

            Then("성공") {
                val res = stationLineService.findLineMapByStationIds(listOf(station))
                res[station]?.size shouldBe 1
                res[station]?.first() shouldBe stationLine.line.id
            }
        }
    }

    Given("Station에 연결된 노선 체크를 진행할 때") {
        When("이미 연결된 요청 일때") {
            val station = dummyStation()
            val line = dummyLine()

            every { stationLineRepository.existsByStationAndLine(any(), any()) } returns true

            Then("true 리턴") {
                val res = stationLineService.checkAlreadyConnect(station, line)
                res shouldBe true
            }
        }

        When("연결되지 않은 요청 일때") {
            val station = dummyStation()
            val line = dummyLine()

            every { stationLineRepository.existsByStationAndLine(any(), any()) } returns false

            Then("false 리턴") {
                val res = stationLineService.checkAlreadyConnect(station, line)
                res shouldBe false
            }
        }
    }

    Given("Station에 연결된 노선 삭제를 진행할 때") {
        When("유효한 요청으로 연결된 노선 삭제 요청") {
            val station = dummyStation()
            val line = dummyLine()
            val stationLine = dummyStationLine()

            every { stationLineRepository.findByStationAndLine(any(), any()) } returns stationLine
            every { stationLineRepository.delete(any()) } just runs

            Then("성공") {
                stationLineService.deleteStationLine(station, line)
                verify(exactly = 1) { stationLineRepository.delete(any()) }
            }
        }

        When("잘못된 요청으로 연결된 노선 삭제 요청") {
            val station = dummyStation()
            val line = dummyLine()

            every {
                stationLineRepository.findByStationAndLine(any(), any())
            } throws CustomException(ErrorCode.NOT_FOUND_STATION_LINE, station.id, line.id)

            Then("실패") {
                shouldThrowExactly<CustomException> {
                    stationLineService.deleteStationLine(station, line)
                }.message shouldBe ErrorCode.NOT_FOUND_STATION_LINE.message.format(station.id, line.id)
            }
        }
    }
})
