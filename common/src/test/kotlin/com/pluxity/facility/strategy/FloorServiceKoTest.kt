package com.pluxity.facility.strategy

import com.pluxity.facility.floor.Floor
import com.pluxity.facility.floor.FloorRepository
import com.pluxity.facility.floor.dto.FloorRequest
import facility.dummyFacility
import facility.dummyFloor
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import io.mockk.verifyOrder

class FloorServiceKoTest :
    BehaviorSpec({

        val floorRepository = mockk<FloorRepository>()
        val floorService = FloorService(floorRepository)

        Given("Floor 저장 요청을 할 때") {
            val validFacility = dummyFacility()

            When("유효한 Facility와 FloorRequest 리스트가 제공되면") {

                val validRequests =
                    listOf(
                        FloorRequest(name = "1층", floorId = "1F"),
                        FloorRequest(name = "2층", floorId = "2F"),
                    )
                every { floorRepository.saveAll(any<List<Floor>>()) } returns listOf()

                floorService.save(validFacility, validRequests)

                Then("모든 Floor를 저장하고 repository.saveAll이 호출된다") {
                    verify(exactly = 1) { floorRepository.saveAll(any<List<Floor>>()) }
                }
            }

            When("FloorRequest가 빈 리스트면") {
                floorService.save(validFacility, emptyList())

                Then("저장하지 않고 repository.saveAll이 호출되지 않는다") {
                    verify(exactly = 0) { floorRepository.saveAll(any<List<Floor>>()) }
                }
            }

            When("FloorRequest가 null이면") {
                floorService.save(validFacility, null)

                Then("저장하지 않고 repository.saveAll이 호출되지 않는다") {
                    verify(exactly = 0) { floorRepository.saveAll(any<List<Floor>>()) }
                }
            }
        }

        Given("Floor 업데이트 요청을 할 때") {
            val validFacility = dummyFacility()

            When("유효한 Facility와 FloorRequest 리스트가 제공되면") {
                val validUpdateRequests =
                    listOf(
                        FloorRequest(name = "1층", floorId = "1F"),
                        FloorRequest(name = "2층", floorId = "2F"),
                    )

                every { floorRepository.deleteByFacility(any()) } just runs
                every { floorRepository.saveAll(any<List<Floor>>()) } returns listOf()

                floorService.update(validFacility, validUpdateRequests)

                Then("기존 Floor를 삭제하고 새로운 Floor를 저장한다") {
                    verifyOrder {
                        floorRepository.deleteByFacility(validFacility)
                        floorRepository.saveAll(any<List<Floor>>())
                    }
                }
            }

            When("FloorRequest가 빈 리스트면") {
                every { floorRepository.deleteByFacility(validFacility) } just runs
                floorService.update(validFacility, emptyList())

                Then("기존 Floor만 삭제하고 새로 저장하지 않는다") {
                    verify(exactly = 1) { floorRepository.deleteByFacility(validFacility) }
                    verify(exactly = 0) { floorRepository.saveAll(any<List<Floor>>()) }
                }
            }

            When("FloorRequest가 null이면") {
                every { floorRepository.deleteByFacility(any()) } just runs
                floorService.update(validFacility, null)

                Then("기존 Floor만 삭제하고 새로 저장하지 않는다") {
                    verify(exactly = 1) { floorRepository.deleteByFacility(validFacility) }
                    verify(exactly = 0) { floorRepository.saveAll(any<List<Floor>>()) }
                }
            }
        }

        Given("특정 Facility의 Floor 조회 요청을 할 때") {
            val validFacility = dummyFacility()

            When("해당 Facility에 Floor가 존재하면") {
                val floors =
                    listOf(
                        dummyFloor(id = 1L, floorId = "1F", name = "1층"),
                        dummyFloor(id = 2L, floorId = "2F", name = "2층"),
                    )

                every { floorRepository.findAllByFacility(validFacility) } returns floors

                val result = floorService.findAllByFacility(validFacility)

                Then("모든 Floor를 FloorResponse로 변환하여 반환한다") {
                    result.size shouldBe 2
                    result[0].floorId shouldBe "1F"
                    result[1].floorId shouldBe "2F"
                }
            }
        }

        Given("여러 Facility의 Floor 조회 요청을 할 때") {
            When("Facility 리스트가 비어있으면") {
                val result = floorService.findAllByFacilities(emptyList())

                Then("빈 Map을 반환한다") {
                    result shouldBe emptyMap()
                }
            }

            When("유효한 Facility 리스트가 제공되면") {
                val facility1 = dummyFacility(id = 1L)
                val facility2 = dummyFacility(id = 2L)
                val facilities = listOf(facility1, facility2)

                val floor1 = dummyFloor(id = 1L, facility = facility1, floorId = "1F")
                val floor2 = dummyFloor(id = 2L, facility = facility2, floorId = "B1")

                every { floorRepository.findAllByFacilities(facilities) } returns listOf(floor1, floor2)

                val result = floorService.findAllByFacilities(facilities)

                Then("각 Facility별로 Floor 리스트를 그룹화한 Map을 반환한다") {
                    result.size shouldBe 2
                    result[facility1]?.size shouldBe 1
                    result[facility2]?.size shouldBe 1
                }
            }

            When("일부 Facility는 Floor가 있고 일부는 없으면") {
                val facility1 = dummyFacility(id = 1L)
                val facility2 = dummyFacility(id = 2L)
                val facilities = listOf(facility1, facility2)

                val floor1 = dummyFloor(id = 1L, facility = facility1, floorId = "1F")
                val floor2 = dummyFloor(id = 2L, facility = facility1, floorId = "B1")
                every { floorRepository.findAllByFacilities(facilities) } returns listOf(floor1, floor2)

                val result = floorService.findAllByFacilities(facilities)
                Then("Floor가 있는 Facility만 Map에 포함된다") {
                    result.size shouldBe 1
                    result[facility1]?.size shouldBe 2
                    result[facility2] shouldBe null
                }
            }

            When("Floor의 facility가 null이면") {
                val facilities = listOf(dummyFacility(id = 1L))
                val floorWithNullFacility = dummyFloor(id = 1L, facility = null)

                every { floorRepository.findAllByFacilities(facilities) } returns listOf(floorWithNullFacility)

                val exception =
                    shouldThrow<IllegalStateException> {
                        floorService.findAllByFacilities(facilities)
                    }

                Then("IllegalStateException이 발생한다") {
                    exception.message shouldBe "Floor(id=${floorWithNullFacility.id}) must have a facility"
                }
            }
        }
        Given("Floor 삭제 요청을 할 때") {
            When("유효한 Facility가 제공되면") {
                val facility = dummyFacility(id = 1L)

                every { floorRepository.deleteByFacility(facility) } just runs

                floorService.delete(facility)

                Then("해당 Facility의 모든 Floor를 삭제한다") {
                    verify(exactly = 1) { floorRepository.deleteByFacility(facility) }
                }
            }
        }
    })
