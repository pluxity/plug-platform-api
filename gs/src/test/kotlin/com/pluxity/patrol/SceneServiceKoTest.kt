package com.pluxity.patrol

import com.pluxity.facility.FacilityRepository
import com.pluxity.feature.entity.Spatial
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.patrol.constant.DeviceAction
import com.pluxity.patrol.constant.DeviceType
import com.pluxity.patrol.dto.SceneCreateRequest
import com.pluxity.patrol.dto.SceneDeviceActionCreateRequest
import com.pluxity.patrol.dto.SceneDeviceActionUpdateRequest
import com.pluxity.patrol.dto.SceneUpdateRequest
import com.pluxity.patrol.entity.dummyScene
import com.pluxity.patrol.entity.dummySceneDeviceAction
import com.pluxity.patrol.repository.ScenarioSceneRepository
import com.pluxity.patrol.repository.SceneRepository
import com.pluxity.patrol.service.DeviceManager
import com.pluxity.patrol.service.SceneService
import facility.dummyFacility
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
import org.springframework.data.repository.findByIdOrNull

class SceneServiceKoTest :
    BehaviorSpec({

        val facilityRepository: FacilityRepository = mockk()
        val sceneRepository: SceneRepository = mockk()
        val deviceManager: DeviceManager = mockk()
        val scenarioSceneRepository: ScenarioSceneRepository = mockk()
        val sceneService = SceneService(facilityRepository, sceneRepository, scenarioSceneRepository, deviceManager)

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
                        duration = 0.0,
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
                        duration = 0.0,
                        position = null,
                        rotation = null,
                        sceneDeviceActionRequests =
                            arrayListOf(
                                SceneDeviceActionCreateRequest(
                                    deviceId = "cctv-1",
                                    deviceAction = DeviceAction.VIEW,
                                    deviceType = DeviceType.CCTV,
                                    actionParam = null,
                                    executionOrder = 0,
                                ),
                            ),
                    )

                every { deviceManager.checkDeviceExists(any(), any()) } just runs
                every { deviceManager.validateAction(any(), any()) } just runs
                every { facilityRepository.findByIdOrNull(1L) } returns facility
                every { sceneRepository.save(any()) } returns scene

                Then("Scene과 SceneDeviceAction 함께 생성") {
                    val savedId = sceneService.createScene(facilityId, createRequest)
                    savedId shouldBe 1L
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
                val facility = dummyFacility()

                val scene1 = dummyScene(id = 1L, name = "씬1")
                val scene2 = dummyScene(id = 2L, name = "씬2")

                every { facilityRepository.findByIdOrNull(1L) } returns facility
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
            When("전체 필드 수정") {
                val scene = dummyScene()
                val facilityId = 1L
                every { sceneRepository.findByIdWithDetails(1L) } returns scene
                every { scene.facility.id } returns facilityId

                Then("모든 필드가 요청 값으로 덮어씌워짐") {
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
                    scene.description shouldBe null
                }
            }

            When("모든 필드와 함께 수정") {
                val scene = dummyScene()
                val facilityId = 1L
                every { sceneRepository.findByIdWithDetails(1L) } returns scene
                every { scene.facility.id } returns facilityId

                Then("모든 필드가 요청 값으로 업데이트됨") {
                    val updateRequest =
                        SceneUpdateRequest(
                            name = "수정된 씬",
                            description = "수정된 설명",
                            duration = 20.0,
                            rotation = Spatial(1.0, 1.0, 1.0),
                            position = Spatial(2.0, 2.0, 2.0),
                            sceneDeviceActionRequests = null,
                        )
                    sceneService.updateScene(facilityId, 1L, updateRequest)
                    scene.name shouldBe "수정된 씬"
                    scene.description shouldBe "수정된 설명"
                    scene.duration shouldBe 20.0
                }
            }

            When("sceneDeviceAction 추가") {
                val scene = dummyScene()
                val existingAction = dummySceneDeviceAction(id = 1L, scene = scene)
                scene.sceneDeviceActions.add(existingAction)

                every { deviceManager.checkDeviceExists(any(), any()) } just runs
                every { deviceManager.validateAction(any(), any()) } just runs
                every { scene.facility.id } returns 1L
                every { sceneRepository.findByIdWithDetails(1L) } returns scene

                Then("기존 항목 유지하고 새 항목 추가") {
                    val updateRequest =
                        SceneUpdateRequest(
                            name = "test",
                            description = null,
                            duration = null,
                            rotation = null,
                            position = null,
                            sceneDeviceActionRequests =
                                arrayListOf(
                                    SceneDeviceActionUpdateRequest(
                                        sceneDeviceActionId = 1L, // 기존 항목 유지
                                        deviceId = "cctv-1",
                                        deviceAction = DeviceAction.VIEW,
                                        deviceType = DeviceType.CCTV,
                                        actionParam = null,
                                        executionOrder = 0,
                                    ),
                                    SceneDeviceActionUpdateRequest(
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

                every { deviceManager.checkDeviceExists(any(), any()) } just runs
                every { deviceManager.validateAction(any(), any()) } just runs
                every { scene.facility.id } returns 1L
                every { sceneRepository.findByIdWithDetails(1L) } returns scene

                Then("요청에 없는 항목 삭제") {
                    val updateRequest =
                        SceneUpdateRequest(
                            name = "test",
                            description = null,
                            duration = null,
                            rotation = null,
                            position = null,
                            sceneDeviceActionRequests =
                                arrayListOf(
                                    SceneDeviceActionUpdateRequest(
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

            When("sceneDeviceActionRequests가 null이면 기존 항목 전체 삭제") {
                val scene = dummyScene()
                val action1 = dummySceneDeviceAction(id = 1L, scene = scene, deviceId = "cctv-1")
                scene.sceneDeviceActions.add(action1)

                every { scene.facility.id } returns 1L
                every { sceneRepository.findByIdWithDetails(1L) } returns scene

                Then("모든 sceneDeviceAction 삭제됨") {
                    val updateRequest =
                        SceneUpdateRequest(
                            name = "test",
                            description = null,
                            duration = null,
                            rotation = null,
                            position = null,
                            sceneDeviceActionRequests = null,
                        )
                    sceneService.updateScene(1L, 1L, updateRequest)
                    scene.sceneDeviceActions.size shouldBe 0
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
                every { scenarioSceneRepository.deleteAllBySceneId(1L) } returns 1
                every { sceneRepository.deleteByIdAndFacilityId(1L, 1L) } returns 1L

                Then("정상 삭제") {
                    sceneService.deleteScene(1L, 1L)
                    verify(exactly = 1) { sceneRepository.deleteByIdAndFacilityId(any(), any()) }
                }
            }

            When("존재하지 않는 id로 삭제 요청") {
                every { scenarioSceneRepository.deleteAllBySceneId(1L) } returns 1
                every { sceneRepository.deleteByIdAndFacilityId(1L, 1L) } returns 0L

                Then("NOT_FOUND_SCENE 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        sceneService.deleteScene(1L, 1L)
                    }.errorCode shouldBe ErrorCode.NOT_FOUND_SCENE
                }
            }
        }
    })
