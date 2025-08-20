package com.pluxity.station

import com.pluxity.station.entity.dummyStation
import com.pluxity.station.entity.dummyStationCode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify

class StationCodeServiceKoTest :
    BehaviorSpec({

        val stationCodeRepository: StationCodeRepository = mockk()
        val stationCodeService = StationCodeService(stationCodeRepository)

        Given("StationCode 생성을 진행할 때") {
            When("유효한 요청으로 StationCode 생성 요청") {
                val station = dummyStation()
                val stationCope = dummyStationCode()

                every { stationCodeRepository.save(any()) } returns stationCope

                Then("성공") {
                    stationCodeService.save(station, "code")
                    verify(exactly = 1) { stationCodeRepository.save(any()) }
                }
            }
        }

        Given("Station에 등록된 코드를 조회할 때") {
            When("유효한 요청으로 조회 요청") {
                val station = dummyStation()
                val stationCode = dummyStationCode()

                every { stationCodeRepository.findByStationOrderByCreatedAtDesc(any()) } returns listOf(stationCode)

                Then("성공") {
                    val res = stationCodeService.findCodesByStation(station)
                    res.size shouldBe 1
                    res.first() shouldBe stationCode.code
                }
            }
        }

        Given("Station에 등록된 코드 전체를 삭제할 때") {
            When("유효한 요청으로 삭제 요청") {
                val station = dummyStation()

                every { stationCodeRepository.deleteByStation(any()) } just runs

                Then("성공") {
                    stationCodeService.deleteByStation(station)
                    verify(exactly = 1) { stationCodeRepository.deleteByStation(any()) }
                }
            }
        }

        Given("Station에 등록된 코드 정보 조회를 station별로 진행할 때") {
            When("Station 값이 없을때") {
                Then("Empty Map 반환") {
                    val res = stationCodeService.findCodeMapByStationIds(emptyList())
                    res shouldBe emptyMap()
                }
            }

            When("유효한 요청으로 조회 요청") {
                val station = dummyStation()
                val stationCode = dummyStationCode(station)

                every { stationCodeRepository.findByStationInOrderByCreatedAtDesc(any()) } returns listOf(stationCode)

                Then("성공") {
                    val res = stationCodeService.findCodeMapByStationIds(listOf(station))
                    res[station]?.size shouldBe 1
                    res[station]?.first() shouldBe stationCode.code
                }
            }
        }
    })
