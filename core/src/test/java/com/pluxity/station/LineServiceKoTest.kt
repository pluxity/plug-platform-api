package com.pluxity.station

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.station.dto.dummyLineCreateRequest
import com.pluxity.station.dto.dummyLineUpdateRequest
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
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull

class LineServiceKoTest : BehaviorSpec({
    val lineRepository: LineRepository = mockk()
    val stationLineService: StationLineService = mockk()
    val lineService = LineService(lineRepository, stationLineService)

    Given("Line 생성을 진행할 때") {
        When("동일한 이름으로 Line 생성 요청") {
            val createRequest = dummyLineCreateRequest()
            every { lineRepository.findByName(any()) } returns dummyLine()

            Then("예외 발생") {
                shouldThrowExactly<CustomException> {
                    lineService.save(createRequest)
                }.message shouldBe ErrorCode.DUPLICATE_LINE_NAME.message.format(createRequest.name)
            }
        }

        When("유효한 요청으로 Line 생성 요청") {
            val createRequest = dummyLineCreateRequest()
            val line = dummyLine()

            every { lineRepository.findByName(any()) } returns null
            every { lineRepository.save(any()) } returns line

            Then("성공") {
                val res = lineService.save(createRequest)
                res shouldBe line.id
            }
        }
    }

    Given("Line 전체 목록을 조회할 때") {
        When("유효한 요청으로 조회 요청") {
            val line = dummyLine()
            every { lineRepository.findAll(any<Sort>()) } returns listOf(line)

            Then("성공") {
                val res = lineService.findAll()
                res.size shouldBe 1
                res.first().name shouldBe line.name
            }
        }
    }

    Given("Line 상세 조회할 때") {
        When("잘못된 아이디로 조회 요청") {
            every { lineRepository.findByIdOrNull(any()) } returns null

            Then("예외발생") {
                val id = 1L
                shouldThrowExactly<CustomException> {
                    lineService.findLineById(id)
                }.message shouldBe ErrorCode.NOT_FOUND_LINE.message.format(id)
            }
        }

        When("유효한 요청으로 조회 요청") {
            val line = dummyLine()
            every { lineRepository.findByIdOrNull(any()) } returns line

            Then("성공") {
                val res = lineService.findLineById(line.id!!)
                res.name shouldBe line.name
                res.id shouldBe line.id
            }
        }
    }

    Given("Line에 속한 역목록 조회할 때") {
        When("잘못된 아이디로 조회 요청") {
            every { lineRepository.findByIdOrNull(any()) } returns null

            Then("예외발생") {
                val id = 1L
                shouldThrowExactly<CustomException> {
                    lineService.findStationsByLineId(id)
                }.message shouldBe ErrorCode.NOT_FOUND_LINE.message.format(id)
            }
        }

        When("유효한 요청으로 조회 요청") {
            val station = dummyStation()
            val line = dummyLine()
            line.addStationLine(dummyStationLine(station = station))
            every { lineRepository.findByIdOrNull(any()) } returns line

            Then("성공") {
                val res = lineService.findStationsByLineId(line.id!!)
                res.size shouldBe 1
                res.first() shouldBe station.id
            }
        }
    }

    Given("Line 수정을 진행할 때") {
        val updateRequest = dummyLineUpdateRequest()
        val line = dummyLine()
        every { lineRepository.findByIdOrNull(any()) } returns line

        When("동일한 이름으로 Line 수정 요청") {
            every { lineRepository.findByNameAndIdNot(any(), any()) } returns line

            Then("예외 발생") {
                shouldThrowExactly<CustomException> {
                    lineService.update(line.id!!, updateRequest)
                }.message shouldBe ErrorCode.DUPLICATE_LINE_NAME.message.format(updateRequest.name)
            }
        }

        When("유효한 요청으로 Line 수정 요청") {
            every { lineRepository.findByNameAndIdNot(any(), any()) } returns null

            Then("성공") {
                lineService.update(line.id!!, updateRequest)
            }
        }
    }

    Given("Line 삭제를 진행할 때") {

        When("유효한 요청으로 Line 삭제 요청") {
            val line = dummyLine()
            line.addStationLine(dummyStationLine(station = dummyStation()))

            every { lineRepository.findByIdOrNull(any()) } returns line
            every { stationLineService.deleteStationLine(any(), any()) } just runs
            every { lineRepository.delete(any()) } just runs

            Then("성공") {
                lineService.delete(line.id!!)
                verify(exactly = 1) { lineRepository.delete(any()) }
            }
        }
    }
})
