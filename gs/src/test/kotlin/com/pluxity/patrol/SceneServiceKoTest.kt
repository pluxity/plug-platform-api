package com.pluxity.patrol

import com.pluxity.cctv.repository.CctvRepository
import com.pluxity.facility.FacilityRepository
import com.pluxity.feature.entity.Spatial
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.patrol.constant.DeviceAction
import com.pluxity.patrol.constant.DeviceType
import com.pluxity.patrol.dto.SceneCreateRequest
import com.pluxity.patrol.dto.SceneDeviceActionRequest
import com.pluxity.patrol.dto.SceneUpdateRequest
import com.pluxity.patrol.entity.dummyScene
import com.pluxity.patrol.entity.dummySceneDeviceAction
import com.pluxity.patrol.repository.SceneRepository
import com.pluxity.patrol.service.SceneService
import com.pluxity.temperaturehumidity.repository.TemperatureHumidityRepository
import facility.dummyFacility
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.repository.findByIdOrNull

class SceneServiceKoTest :
    BehaviorSpec({

        val facilityRepository: FacilityRepository = mockk()
        val sceneRepository: SceneRepository = mockk()
        val cctvRepository: CctvRepository = mockk()
        val temperatureHumidityRepository: TemperatureHumidityRepository = mockk()

        val sceneService = SceneService(facilityRepository, sceneRepository, cctvRepository, temperatureHumidityRepository)

        Given("Scene 생성을 진행할 때") {
            When("유효한 요청으로 생성") {
                val facilityId = 1L
                val facility = dummyFacility(facilityId)
                val scene = dummyScene(facility = facility)
                val createRequest =
                    SceneCreateRequest(
                        name = "테스트 씬",
                        description = "테스트 설명",
                        duration = 10.0,
                        position = Spatial(1.0, 2.0, 3.0),
                        rotation = Spatial(0.0, 0.0, 0.0),
                        sceneDeviceActionRequests = null,
                    )

                every { facilityRepository.findByIdOrNull(1L) } returns facility
                every { facility.requiredId } returns 1L
                every { sceneRepository.save(any()) } returns scene

                Then("정상 생성") {
                    val savedId = sceneService.createScene(facilityId, createRequest)
                    savedId shouldBe 1L
                }
            }

            When("존재하지 않는 facilityId로 생성 요청") {
                val invalidFacilityId = 999L
                val createRequest =
                    SceneCreateRequest(
                        name = "테스트 씬",
                        description = null,
                        duration = null,
                        position = null,
                        rotation = null,
                        sceneDeviceActionRequests = null,
                    )

                every { facilityRepository.findByIdOrNull(999L) } returns null

                Then("NOT_FOUND_FACILITY 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        sceneService.createScene(invalidFacilityId, createRequest)
                    }.errorCode shouldBe ErrorCode.NOT_FOUND_FACILITY
                }
            }

            When("sceneDeviceAction과 함께 생성") {
                val scene = dummyScene()
                val facilityId = 1L
                val facility = dummyFacility(facilityId)
                val createRequest =
                    SceneCreateRequest(
                        name = "테스트 씬",
                        description = null,
                        duration = null,
                        position = null,
                        rotation = null,
                        sceneDeviceActionRequests =
                            arrayListOf(
                                SceneDeviceActionRequest(
                                    sceneDeviceActionId = null,
                                    deviceId = "cctv-1",
                                    deviceAction = DeviceAction.VIEW,
                                    deviceType = DeviceType.CCTV,
                                    actionParam = null,
                                    executionOrder = 0,
                                ),
                            ),
                    )

                every { cctvRepository.existsById("cctv-1") } returns true
                every { facilityRepository.findByIdOrNull(1L) } returns facility
                every { sceneRepository.save(any()) } returns scene

                Then("Scene과 SceneDeviceAction 함께 생성") {
                    val savedId = sceneService.createScene(facilityId, createRequest)
                    savedId shouldBe 1L
                }
            }

            When("유효하지 않은 deviceAction으로 생성 요청") {
                val facilityId = 1L
                val facility = dummyFacility(facilityId)
                val createRequest =
                    SceneCreateRequest(
                        name = "테스트 씬",
                        description = null,
                        duration = null,
                        position = null,
                        rotation = null,
                        sceneDeviceActionRequests =
                            arrayListOf(
                                SceneDeviceActionRequest(
                                    sceneDeviceActionId = null,
                                    deviceId = "cctv-1",
                                    deviceAction = DeviceAction.TURN_ON, // CCTV는 VIEW만 가능
                                    deviceType = DeviceType.CCTV,
                                    actionParam = null,
                                    executionOrder = 0,
                                ),
                            ),
                    )

                every { cctvRepository.existsById("cctv-1") } returns true
                every { facilityRepository.findByIdOrNull(1L) } returns facility

                val exception =
                    shouldThrow<CustomException> {
                        sceneService.createScene(facilityId, createRequest)
                    }

                Then("INVALID_DEVICE_ACTION 예외 발생") {
                    exception.message shouldBe
                        ErrorCode.INVALID_DEVICE_ACTION.getMessage().format(
                            DeviceType.CCTV,
                            DeviceAction.TURN_ON,
                        )
                }
            }
        }

        Given("Scene 상세 조회를 진행할 때") {
            When("유효한 id로 조회") {
                val scene = dummyScene()
                val deviceAction = dummySceneDeviceAction(scene = scene)
                scene.sceneDeviceActions.add(deviceAction)

                every { scene.facility.id } returns 10L
                every { scene.facility.requiredId } returns 10L
                every { scene.facility.name } returns "test"
                every { sceneRepository.findByIdWithDetails(1L) } returns scene

                Then("정상 조회") {
                    val response = sceneService.getScene(10L, 1L)
                    response.id shouldBe 1L
                    response.name shouldBe "테스트 씬"
                    response.sceneDeviceActions.size shouldBe 1
                }
            }

            When("존재하지 않는 id로 조회") {
                every { sceneRepository.findByIdWithDetails(999L) } returns null

                Then("NOT_FOUND_SCENE 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        sceneService.getScene(1L, 999L)
                    }.errorCode shouldBe ErrorCode.NOT_FOUND_SCENE
                }
            }
        }

        Given("Scene 목록 조회를 진행할 때") {
            When("facilityId로 조회") {
                val scene1 = dummyScene(id = 1L, name = "씬1")
                val scene2 = dummyScene(id = 2L, name = "씬2")

                every { facilityRepository.existsById(1L) } returns true
                every { sceneRepository.findByFacilityId(1L) } returns listOf(scene1, scene2)

                Then("해당 facility의 Scene 목록 반환") {
                    val result = sceneService.getScenesByFacilityId(1L)
                    result.size shouldBe 2
                    result[0].name shouldBe "씬1"
                    result[1].name shouldBe "씬2"
                }
            }
        }

        Given("Scene 수정을 진행할 때") {
            When("이름만 수정") {
                val scene = dummyScene()
                val facilityId = 1L
                every { sceneRepository.findByIdWithDetails(1L) } returns scene
                every { scene.facility.id } returns facilityId

                Then("이름만 변경됨") {
                    val updateRequest =
                        SceneUpdateRequest(
                            name = "수정된 씬",
                            description = null,
                            duration = null,
                            rotation = null,
                            position = null,
                            sceneDeviceActionRequests = null,
                        )
                    sceneService.updateScene(facilityId, 1L, updateRequest)
                    scene.name shouldBe "수정된 씬"
                    scene.description shouldBe "테스트 설명" // 기존 값 유지
                }
            }

            When("sceneDeviceAction 추가") {
                val scene = dummyScene()
                val existingAction = dummySceneDeviceAction(id = 1L, scene = scene)
                scene.sceneDeviceActions.add(existingAction)

                every { cctvRepository.existsById(any()) } returns true
                every { scene.facility.id } returns 1L
                every { sceneRepository.findByIdWithDetails(1L) } returns scene

                Then("기존 항목 유지하고 새 항목 추가") {
                    val updateRequest =
                        SceneUpdateRequest(
                            name = null,
                            description = null,
                            duration = null,
                            rotation = null,
                            position = null,
                            sceneDeviceActionRequests =
                                arrayListOf(
                                    SceneDeviceActionRequest(
                                        sceneDeviceActionId = 1L, // 기존 항목 유지
                                        deviceId = "cctv-1",
                                        deviceAction = DeviceAction.VIEW,
                                        deviceType = DeviceType.CCTV,
                                        actionParam = null,
                                        executionOrder = 0,
                                    ),
                                    SceneDeviceActionRequest(
                                        sceneDeviceActionId = null, // 신규 항목
                                        deviceId = "cctv-2",
                                        deviceAction = DeviceAction.VIEW,
                                        deviceType = DeviceType.CCTV,
                                        actionParam = null,
                                        executionOrder = 1,
                                    ),
                                ),
                        )
                    sceneService.updateScene(1L, 1L, updateRequest)
                    scene.sceneDeviceActions.size shouldBe 2
                }
            }

            When("sceneDeviceAction 삭제") {
                val scene = dummyScene()
                val action1 = dummySceneDeviceAction(id = 1L, scene = scene, deviceId = "cctv-1")
                val action2 = dummySceneDeviceAction(id = 2L, scene = scene, deviceId = "cctv-2")
                scene.sceneDeviceActions.addAll(listOf(action1, action2))

                every { cctvRepository.existsById(any()) } returns true
                every { scene.facility.id } returns 1L
                every { sceneRepository.findByIdWithDetails(1L) } returns scene

                Then("요청에 없는 항목 삭제") {
                    val updateRequest =
                        SceneUpdateRequest(
                            name = null,
                            description = null,
                            duration = null,
                            rotation = null,
                            position = null,
                            sceneDeviceActionRequests =
                                arrayListOf(
                                    SceneDeviceActionRequest(
                                        sceneDeviceActionId = 1L, // id=2는 제외되어 삭제됨
                                        deviceId = "cctv-1",
                                        deviceAction = DeviceAction.VIEW,
                                        deviceType = DeviceType.CCTV,
                                        actionParam = null,
                                        executionOrder = 0,
                                    ),
                                ),
                        )
                    sceneService.updateScene(1L, 1L, updateRequest)
                    scene.sceneDeviceActions.size shouldBe 1
                    scene.sceneDeviceActions[0].deviceId shouldBe "cctv-1"
                }
            }

            When("존재하지 않는 id로 수정 요청") {
                every { sceneRepository.findByIdWithDetails(999L) } returns null

                Then("NOT_FOUND_SCENE 예외 발생") {
                    val updateRequest =
                        SceneUpdateRequest(
                            name = "수정",
                            description = null,
                            duration = null,
                            rotation = null,
                            position = null,
                            sceneDeviceActionRequests = null,
                        )
                    shouldThrowExactly<CustomException> {
                        sceneService.updateScene(1L, 999L, updateRequest)
                    }.errorCode shouldBe ErrorCode.NOT_FOUND_SCENE
                }
            }
        }

        Given("Scene 삭제를 진행할 때") {
            When("유효한 id로 삭제 요청") {
                val scene = dummyScene()

                every { scene.facility.id } returns 1L
                every { sceneRepository.deleteByIdAndFacilityId(1L, 1L) } returns 1L

                Then("정상 삭제") {
                    sceneService.deleteScene(1L, 1L)
                    verify(exactly = 1) { sceneRepository.deleteByIdAndFacilityId(any(), any()) }
                }
            }

            When("존재하지 않는 id로 삭제 요청") {
                every { sceneRepository.deleteByIdAndFacilityId(1L, 1L) } returns 0L

                Then("NOT_FOUND_SCENE 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        sceneService.deleteScene(1L, 1L)
                    }.errorCode shouldBe ErrorCode.NOT_FOUND_SCENE
                }
            }
        }
    })
