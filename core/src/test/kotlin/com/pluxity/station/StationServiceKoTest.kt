package com.pluxity.station

import com.pluxity.facility.Facility
import com.pluxity.facility.FacilityService
import com.pluxity.facility.strategy.FloorService
import com.pluxity.file.service.FileService
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.label3d.Label3DRepository
import com.pluxity.station.dto.dummyCreateStationRequest
import com.pluxity.station.dto.dummyUpdateStationRequest
import com.pluxity.station.entity.dummyLine
import com.pluxity.station.entity.dummyStation
import facility.floor.dummyFloorResponse
import file.dummyFileResponse
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.IsolationMode
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import jakarta.persistence.EntityManager
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull

class StationServiceKoTest :
    BehaviorSpec({
        isolationMode = IsolationMode.InstancePerLeaf // 추가
        val fileService: FileService = mockk()
        val facilityService: FacilityService = mockk()
        val floorService: FloorService = mockk()
        val stationRepository: StationRepository = mockk()
        val lineService: LineService = mockk()
        val label3DRepository: Label3DRepository = mockk()
        val stationCodeService: StationCodeService = mockk()
        val stationLineService: StationLineService = mockk()
        val em: EntityManager = mockk()

        val stationService =
            StationService(
                fileService,
                facilityService,
                floorService,
                stationRepository,
                lineService,
                label3DRepository,
                stationCodeService,
                stationLineService,
                em,
            )

        Given("Station 생성을 진행할 때") {
            When("유효한 요청으로 Station 생성 요청") {
                val createRequest = dummyCreateStationRequest()
                val saved = dummyStation()
                val line = dummyLine()

                every { facilityService.save(any<Facility>(), createRequest.facility) } returns saved
                every { floorService.save(any(), any()) } just runs
                every { lineService.findLineById(any()) } returns line
                every { stationLineService.save(any(), any()) } just runs
                every { stationCodeService.save(any(), any()) } just runs

                Then("성공") {
                    val saveId = stationService.save(createRequest)
                    saveId shouldBe saved.id
                }
            }
        }

        Given("Station 목록 조회를 진행할 때") {
            When("정상 요청이 오면") {
                val station = dummyStation()
                every {
                    stationRepository.findAll(any<Sort>())
                } returns mutableListOf(station)

                every {
                    fileService.getFiles(any())
                } returns mutableListOf(dummyFileResponse())

                every {
                    floorService.findAllByFacilities(any<List<Facility>>())
                } returns mapOf(station to mutableListOf(dummyFloorResponse()))

                every {
                    stationCodeService.findCodeMapByStationIds(any())
                } returns mapOf(station to listOf("code"))

                every {
                    stationLineService.findLineMapByStationIds(any())
                } returns mapOf(station to listOf(1L))

                Then("정상 조회") {
                    stationService.findAll().size shouldBe 1
                }
            }
        }

        Given("Station 상세 조회를 진행할 때") {
            When("유효한 아이디로 조회 요청") {
                val station = dummyStation()
                every {
                    facilityService.findById(any())
                } returns station

                every {
                    floorService.findAllByFacility(any())
                } returns mutableListOf(dummyFloorResponse())

                every {
                    fileService.getFileResponse(any<Long>())
                } returns dummyFileResponse()

                every {
                    stationLineService.findLinesByStation(any())
                } returns listOf(1L)

                every {
                    stationCodeService.findCodesByStation(any())
                } returns listOf("code")

                Then("정상 조회") {
                    val res = stationService.findById(station.id)
                    res.facility.name shouldBe station.name
                    res.floors.size shouldBe 1
                }
            }

            When("없는 아이디로 조회 요청") {
                Then("NOT_FOUND_FACILITY 예외 발생") {
                    val searchId = 1L
                    every { facilityService.findById(searchId) } throws
                        CustomException(ErrorCode.NOT_FOUND_FACILITY, searchId)

                    shouldThrowExactly<CustomException> {
                        stationService.findById(searchId)
                    }.message shouldBe ErrorCode.NOT_FOUND_FACILITY.getMessage().format(searchId)
                }
            }
        }

        Given("Station 수정을 진행할 때") {
            When("정상 수정 요청") {
                val updateRequest = dummyUpdateStationRequest()
                val station = dummyStation(name = updateRequest.facility.name!!)
                val line = dummyLine()
                every {
                    stationRepository.findByIdOrNull(any())
                } returns station

                every {
                    facilityService.putUpdate(any(), any())
                } just runs

                every {
                    floorService.update(any(), any())
                } just runs

                every {
                    stationLineService.deleteByStation(any())
                } just runs
                every { lineService.findLineById(any()) } returns line
                every { stationLineService.save(any(), any()) } just runs
                every { stationCodeService.deleteByStation(any()) } just runs
                every { stationCodeService.save(any(), any()) } just runs

                Then("정상 수정") {
                    stationService.putUpdate(station.id, updateRequest)
                    station.name shouldBe updateRequest.facility.name
                }
            }
        }

        Given("Station 삭제를 진행할 때") {
            When("정상 삭제 요청") {
                val station = dummyStation()
                every {
                    stationRepository.findByIdOrNull(any())
                } returns station
                val slot = slot<Long>()
                every {
                    floorService.delete(any())
                } just runs
                every {
                    stationLineService.deleteByStation(any())
                } just runs
                every {
                    stationCodeService.deleteByStation(any())
                } just runs
                every {
                    em.flush()
                } just runs
                every {
                    em.clear()
                } just runs
                every {
                    facilityService.deleteFacility(capture(slot))
                } just runs

                Then("정상 삭제") {
                    stationService.delete(station.id)
                    verify(exactly = 1) { facilityService.deleteFacility(any()) }
                    slot.captured shouldBe station.id
                }
            }
        }

        Given("Station에 노선을 추가할 때") {
            val station = dummyStation()
            every {
                stationRepository.findByIdOrNull(any())
            } returns station

            val line = dummyLine()
            every { lineService.findLineById(any()) } returns line

            When("정상 추가 요청") {
                Then("정상 추가") {
                    every {
                        stationLineService.checkAlreadyConnect(any(), any())
                    } returns false

                    every {
                        stationLineService.save(station, line)
                    } just runs
                    stationService.addLineToStation(station.id, line.id!!)
                    verify(exactly = 1) { stationLineService.save(station, line) }
                }
            }

            When("이미 추가 되어있을때 요청") {
                Then("저장 요청 안함") {
                    every {
                        stationLineService.checkAlreadyConnect(any(), any())
                    } returns true

                    stationService.addLineToStation(station.id, line.id!!)
                    verify(exactly = 0) { stationLineService.save(any(), any()) }
                }
            }
        }

        Given("Station에서 노선 제거를 진행할 때") {
            When("정상 삭제 요청") {
                val station = dummyStation()
                every {
                    stationRepository.findByIdOrNull(any())
                } returns station

                val line = dummyLine()
                every { lineService.findLineById(any()) } returns line

                val stationSlot = slot<Station>()
                val lineSlot = slot<Line>()

                every {
                    stationLineService.deleteStationLine(capture(stationSlot), capture(lineSlot))
                } just runs

                Then("정상 삭제") {
                    stationService.removeLineFromStation(station.id, line.id!!)
                    verify(exactly = 1) { stationLineService.deleteStationLine(any(), any()) }
                    stationSlot.captured.id shouldBe station.id
                    lineSlot.captured.id shouldBe line.id
                }
            }
        }
    })
