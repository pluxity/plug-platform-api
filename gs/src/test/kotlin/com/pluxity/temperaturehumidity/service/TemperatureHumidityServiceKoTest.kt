package com.pluxity.temperaturehumidity.service

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.temperaturehumidity.dto.TemperatureHumidityCreateRequest
import com.pluxity.temperaturehumidity.dto.TemperatureHumidityUpdateRequest
import com.pluxity.temperaturehumidity.entity.dummyTemperatureHumidity
import com.pluxity.temperaturehumidity.repository.TemperatureHumidityRepository
import entity.dummyFeature
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import java.util.UUID

class TemperatureHumidityServiceKoTest :
    BehaviorSpec({

        val temperatureHumidityRepository: TemperatureHumidityRepository = mockk()

        val temperatureHumidityService =
            TemperatureHumidityService(
                temperatureHumidityRepository,
            )

        Given("온습도계 생성을 진행할 때") {
            When("유효한 요청으로 온습도계 생성 요청") {
                val id = UUID.randomUUID().toString()
                val createRequest = TemperatureHumidityCreateRequest(id, "th-name")

                every {
                    temperatureHumidityRepository.save(any())
                } returns dummyTemperatureHumidity(id = id)
                Then("성공") {
                    val saveId = temperatureHumidityService.save(createRequest)
                    saveId shouldBe createRequest.id
                }
            }
        }

        Given("온습도계 목록 조회를 진행할 때") {
            When("정상 요청이 오면") {
                every {
                    temperatureHumidityRepository.findAllByFacilityIdIfPresent(any())
                } returns mutableListOf(dummyTemperatureHumidity(feature = dummyFeature()))

                Then("정상 조회") {
                    temperatureHumidityService.findAll().size shouldBe 1
                }
            }
        }

        Given("온습도계 상세 조회를 진행할 때") {
            When("유효한 아이디로 조회 요청") {
                val th = dummyTemperatureHumidity()
                every {
                    temperatureHumidityRepository.findByIdOrNullCustom(any())
                } returns th
                Then("정상 조회") {
                    val res = temperatureHumidityService.findById(th.id)
                    res.id shouldBe th.id
                    res.name shouldBe th.name
                }
            }

            When("없는 아이디로 조회 요청") {
                every {
                    temperatureHumidityRepository.findByIdOrNullCustom(any())
                } returns null
                Then("NOT_FOUND_DEVICE 예외 발생") {
                    val searchId = UUID.randomUUID().toString()
                    shouldThrowExactly<CustomException> {
                        temperatureHumidityService.findById(searchId)
                    }.message shouldBe ErrorCode.NOT_FOUND_DEVICE.getMessage().format(searchId)
                }
            }
        }

        Given("온습도계 수정을 진행할 때") {
            When("정상 수정 요청") {
                val th = dummyTemperatureHumidity()
                every {
                    temperatureHumidityRepository.findByIdOrNullCustom(any())
                } returns th
                Then("정상 수정") {
                    val updateName = "updated TemperatureHumidity"
                    temperatureHumidityService.putUpdate(th.id, TemperatureHumidityUpdateRequest(updateName))
                    th.name shouldBe updateName
                }
            }
        }

        Given("온습도계 삭제를 진행할 때") {
            When("정상 삭제 요청") {
                val th = dummyTemperatureHumidity()
                val slot = slot<String>()
                every {
                    temperatureHumidityRepository.findByIdOrNullCustom(any())
                } returns th
                every {
                    temperatureHumidityRepository.deleteById(capture(slot))
                } just runs
                Then("정상 삭제") {
                    temperatureHumidityService.delete(th.id)
                    verify(exactly = 1) { temperatureHumidityRepository.deleteById(any()) }
                    slot.captured shouldBe th.id
                }
            }
        }
    })
