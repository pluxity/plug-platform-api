package com.pluxity.patrol

import com.pluxity.facility.FacilityRepository
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.patrol.constant.TriggerRequestType
import com.pluxity.patrol.dto.ScenarioCreateRequest
import com.pluxity.patrol.dto.ScenarioSceneCreateRequest
import com.pluxity.patrol.dto.ScenarioSceneUpdateRequest
import com.pluxity.patrol.dto.ScenarioUpdateRequest
import com.pluxity.patrol.dto.TriggerRequest
import com.pluxity.patrol.entity.ScenarioScene
import com.pluxity.patrol.entity.Trigger
import com.pluxity.patrol.entity.dummyScenario
import com.pluxity.patrol.entity.dummyScenarioScene
import com.pluxity.patrol.entity.dummyScene
import com.pluxity.patrol.repository.ScenarioRepository
import com.pluxity.patrol.repository.SceneRepository
import com.pluxity.patrol.service.ScenarioService
import com.pluxity.patrol.service.TriggerService
import facility.dummyFacility
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.springframework.data.repository.findByIdOrNull

class ScenarioServiceKoTest :
    BehaviorSpec({

        val scenarioRepository = mockk<ScenarioRepository>()
        val sceneRepository = mockk<SceneRepository>()
        val facilityRepository = mockk<FacilityRepository>()
        val triggerService = mockk<TriggerService>()

        val scenarioService =
            ScenarioService(
                scenarioRepository,
                sceneRepository,
                facilityRepository,
                triggerService,
            )

        Given("Scenario 생성을 진행할 때") {
            When("유효한 요청으로 생성") {
                val facilityId = 1L
                val facility = dummyFacility(facilityId)
                val scenario = dummyScenario(facility = facility)
                val createRequest =
                    ScenarioCreateRequest(
                        name = "테스트 시나리오",
                        description = "테스트 설명",
                        isActive = true,
                        scenarioSceneRequests = null,
                    )

                every { facilityRepository.findByIdOrNull(facilityId) } returns facility
                every { scenarioRepository.save(any()) } returns scenario

                Then("정상 생성") {
                    val savedId = scenarioService.createScenario(facilityId, createRequest)
                    savedId shouldBe 1L
                }
            }

            When("존재하지 않는 facilityId로 생성 요청") {
                val invalidFacilityId = 999L
                val createRequest =
                    ScenarioCreateRequest(
                        name = "테스트 시나리오",
                        description = "테스트 설명",
                        isActive = true,
                        scenarioSceneRequests = null,
                    )

                every { facilityRepository.findByIdOrNull(invalidFacilityId) } returns null

                Then("NOT_FOUND_FACILITY 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        scenarioService.createScenario(invalidFacilityId, createRequest)
                    }.errorCode shouldBe ErrorCode.NOT_FOUND_FACILITY
                }
            }

            When("scenarioSceneRequests와 함께 생성") {
                val facilityId = 1L
                val facility = dummyFacility(facilityId)
                val scene = dummyScene(id = 1L, facility = facility)
                val scenario = dummyScenario(facility = facility)

                val createRequest =
                    ScenarioCreateRequest(
                        name = "테스트 시나리오",
                        description = "테스트 설명",
                        isActive = true,
                        scenarioSceneRequests =
                            listOf(
                                ScenarioSceneCreateRequest(
                                    sceneId = 1L,
                                    order = 1,
                                    duration = 10.0,
                                ),
                            ),
                    )

                every { facilityRepository.findByIdOrNull(facilityId) } returns facility
                every { scene.facility.id } returns facilityId
                every { sceneRepository.findAllById(listOf(1L)) } returns listOf(scene)
                every { scenarioRepository.save(any()) } returns scenario

                Then("Scenario와 ScenarioScene 함께 생성") {
                    val savedId = scenarioService.createScenario(facilityId, createRequest)
                    savedId shouldBe 1L
                }
            }

            When("중복된 executionOrder로 생성 요청") {
                val facilityId = 1L
                val facility = dummyFacility(facilityId)

                val createRequest =
                    ScenarioCreateRequest(
                        name = "테스트 시나리오",
                        description = "테스트 설명",
                        isActive = true,
                        scenarioSceneRequests =
                            listOf(
                                ScenarioSceneCreateRequest(sceneId = 1L, order = 1),
                                ScenarioSceneCreateRequest(sceneId = 2L, order = 1), // 중복
                            ),
                    )

                every { facilityRepository.findByIdOrNull(facilityId) } returns facility

                Then("DUPLICATE_EXECUTION_ORDER 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        scenarioService.createScenario(facilityId, createRequest)
                    }.errorCode shouldBe ErrorCode.DUPLICATE_EXECUTION_ORDER
                }
            }

            When("유효하지 않은 executionOrder로 생성 요청 (0 이하)") {
                val facilityId = 1L
                val facility = dummyFacility(facilityId)

                val createRequest =
                    ScenarioCreateRequest(
                        name = "테스트 시나리오",
                        description = "테스트 설명",
                        isActive = true,
                        scenarioSceneRequests =
                            listOf(
                                ScenarioSceneCreateRequest(sceneId = 1L, order = 0), // 0 이하
                            ),
                    )

                every { facilityRepository.findByIdOrNull(facilityId) } returns facility

                Then("INVALID_EXECUTION_ORDER 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        scenarioService.createScenario(facilityId, createRequest)
                    }.errorCode shouldBe ErrorCode.INVALID_EXECUTION_ORDER
                }
            }

            When("다른 facility의 scene으로 생성 요청") {
                val facilityId = 1L
                val otherFacilityId = 2L
                val facility = dummyFacility(facilityId)
                val otherFacility = dummyFacility(otherFacilityId)
                val scene = dummyScene(id = 1L, facility = otherFacility)

                val createRequest =
                    ScenarioCreateRequest(
                        name = "테스트 시나리오",
                        description = "테스트 설명",
                        isActive = true,
                        scenarioSceneRequests =
                            listOf(
                                ScenarioSceneCreateRequest(sceneId = 1L, order = 1),
                            ),
                    )

                every { facilityRepository.findByIdOrNull(facilityId) } returns facility
                every { facility.id } returns facilityId
                every { scene.facility.id } returns otherFacilityId
                every { sceneRepository.findAllById(listOf(1L)) } returns listOf(scene)

                Then("UNMATCHED_FACILITY_SCENE 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        scenarioService.createScenario(facilityId, createRequest)
                    }.errorCode shouldBe ErrorCode.UNMATCHED_FACILITY_SCENE
                }
            }
        }

        Given("Scenario 상세 조회를 진행할 때") {
            When("유효한 id로 조회") {
                val facilityId = 1L
                val scenario = dummyScenario()

                every { scenario.facility.id } returns facilityId
                every { scenario.facility.requiredId } returns facilityId
                every { scenario.facility.name } returns "테스트 시설"
                every { scenarioRepository.findByIdWithDetails(1L) } returns scenario

                Then("정상 조회") {
                    val response = scenarioService.getScenario(facilityId, 1L)
                    response.id shouldBe 1L
                    response.name shouldBe "테스트 시나리오"
                }
            }

            When("존재하지 않는 id로 조회") {
                every { scenarioRepository.findByIdWithDetails(999L) } returns null

                Then("NOT_FOUND_SCENARIO 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        scenarioService.getScenario(1L, 999L)
                    }.errorCode shouldBe ErrorCode.NOT_FOUND_SCENARIO
                }
            }

            When("다른 facility의 scenario 조회 시도") {
                val facilityId = 1L
                val otherFacilityId = 2L
                val scenario = dummyScenario()

                every { scenario.facility.id } returns otherFacilityId
                every { scenarioRepository.findByIdWithDetails(1L) } returns scenario

                Then("UNMATCHED_FACILITY_SCENARIO 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        scenarioService.getScenario(facilityId, 1L)
                    }.errorCode shouldBe ErrorCode.UNMATCHED_FACILITY_SCENARIO
                }
            }
        }

        Given("Scenario 목록 조회를 진행할 때") {
            When("facilityId로 조회") {
                val scenario1 = dummyScenario(id = 1L, name = "시나리오1")
                val scenario2 = dummyScenario(id = 2L, name = "시나리오2")

                every { facilityRepository.existsById(1L) } returns true
                every { scenarioRepository.findByFacilityId(1L) } returns listOf(scenario1, scenario2)

                Then("해당 facility의 Scenario 목록 반환") {
                    val result = scenarioService.getScenarioByFacilityId(1L)
                    result.size shouldBe 2
                    result[0].name shouldBe "시나리오1"
                    result[1].name shouldBe "시나리오2"
                }
            }

            When("존재하지 않는 facilityId로 조회") {
                every { facilityRepository.existsById(999L) } returns false

                Then("NOT_FOUND_FACILITY 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        scenarioService.getScenarioByFacilityId(999L)
                    }.errorCode shouldBe ErrorCode.NOT_FOUND_FACILITY
                }
            }
        }

        Given("Scenario 수정을 진행할 때") {
            When("기본 필드 수정") {
                val facilityId = 1L
                val scenario = dummyScenario()

                every { scenario.facility.id } returns facilityId
                every { scenarioRepository.findByIdWithDetails(1L) } returns scenario

                Then("모든 필드가 요청 값으로 업데이트됨") {
                    val updateRequest =
                        ScenarioUpdateRequest(
                            name = "수정된 시나리오",
                            description = "수정된 설명",
                            isActive = false,
                            scenarioScenes = null,
                        )
                    scenarioService.updateScenario(facilityId, 1L, updateRequest)
                    scenario.name shouldBe "수정된 시나리오"
                }
            }

            When("scenarioScene 추가") {
                val facilityId = 1L
                val facility = dummyFacility(facilityId)
                val scenario = dummyScenario(facility = facility)
                val scene = dummyScene(id = 1L, facility = facility)

                every { scenario.facility.id } returns facilityId
                every { scene.facility.id } returns facilityId
                every { scenarioRepository.findByIdWithDetails(1L) } returns scenario
                every { sceneRepository.findAllById(listOf(1L)) } returns listOf(scene)

                Then("새 ScenarioScene 추가됨") {
                    val updateRequest =
                        ScenarioUpdateRequest(
                            name = "테스트 시나리오",
                            description = null,
                            isActive = null,
                            scenarioScenes =
                                listOf(
                                    ScenarioSceneUpdateRequest(
                                        scenarioSceneId = null,
                                        sceneId = 1L,
                                        order = 1,
                                        duration = 10.0,
                                    ),
                                ),
                        )
                    scenarioService.updateScenario(facilityId, 1L, updateRequest)
                    scenario.scenarioScenes.size shouldBe 1
                }
            }

            When("scenarioScenes가 null이면 기존 항목 전체 삭제") {
                val facilityId = 1L
                val facility = dummyFacility(facilityId)
                val scenario = dummyScenario(facility = facility)
                val scene = dummyScene(id = 1L, facility = facility)
                val scenarioScene =
                    ScenarioScene(
                        scenario = scenario,
                        scene = scene,
                        executionOrder = 1,
                        duration = 10.0,
                    )
                scenario.scenarioScenes.add(scenarioScene)

                every { scenario.facility.id } returns facilityId
                every { scenarioRepository.findByIdWithDetails(1L) } returns scenario

                Then("모든 ScenarioScene 삭제됨") {
                    val updateRequest =
                        ScenarioUpdateRequest(
                            name = "테스트 시나리오",
                            description = null,
                            isActive = null,
                            scenarioScenes = null,
                        )
                    scenarioService.updateScenario(facilityId, 1L, updateRequest)
                    scenario.scenarioScenes.size shouldBe 0
                }
            }

            When("존재하지 않는 id로 수정 요청") {
                every { scenarioRepository.findByIdWithDetails(999L) } returns null

                Then("NOT_FOUND_SCENARIO 예외 발생") {
                    val updateRequest =
                        ScenarioUpdateRequest(
                            name = "수정",
                            description = null,
                            isActive = null,
                            scenarioScenes = null,
                        )
                    shouldThrowExactly<CustomException> {
                        scenarioService.updateScenario(1L, 999L, updateRequest)
                    }.errorCode shouldBe ErrorCode.NOT_FOUND_SCENARIO
                }
            }

            When("다른 facility의 scenario 수정 시도") {
                val facilityId = 1L
                val otherFacilityId = 2L
                val scenario = dummyScenario()

                every { scenario.facility.id } returns otherFacilityId
                every { scenarioRepository.findByIdWithDetails(1L) } returns scenario

                Then("UNMATCHED_FACILITY_SCENARIO 예외 발생") {
                    val updateRequest =
                        ScenarioUpdateRequest(
                            name = "수정",
                            description = null,
                            isActive = null,
                            scenarioScenes = null,
                        )
                    shouldThrowExactly<CustomException> {
                        scenarioService.updateScenario(facilityId, 1L, updateRequest)
                    }.errorCode shouldBe ErrorCode.UNMATCHED_FACILITY_SCENARIO
                }
            }

            When("scenarioScene의 scene을 다른 scene으로 변경") {
                val facilityId = 1L
                val facility = dummyFacility(facilityId)
                val scenario = dummyScenario(facility = facility)
                val oldScene = dummyScene(id = 1L, facility = facility, name = "기존 씬")
                val newScene = dummyScene(id = 2L, facility = facility, name = "새 씬")
                val scenarioScene = dummyScenarioScene(id = 1L, scenario = scenario, scene = oldScene)
                scenario.scenarioScenes.add(scenarioScene)

                every { scenario.facility.id } returns facilityId
                every { oldScene.facility.id } returns facilityId
                every { newScene.facility.id } returns facilityId
                every { scenarioRepository.findByIdWithDetails(1L) } returns scenario
                every { sceneRepository.findAllById(listOf(2L)) } returns listOf(newScene)

                Then("scene이 새 scene으로 변경됨") {
                    val updateRequest =
                        ScenarioUpdateRequest(
                            name = "테스트 시나리오",
                            description = null,
                            isActive = null,
                            scenarioScenes =
                                listOf(
                                    ScenarioSceneUpdateRequest(
                                        scenarioSceneId = 1L,
                                        sceneId = 2L,
                                        order = 1,
                                        duration = 15.0,
                                    ),
                                ),
                        )
                    scenarioService.updateScenario(facilityId, 1L, updateRequest)
                    scenario.scenarioScenes[0].scene.id shouldBe 2L
                    scenario.scenarioScenes[0].scene.name shouldBe "새 씬"
                }
            }

            When("scenarioScene의 scene을 존재하지 않는 scene으로 변경") {
                val facilityId = 1L
                val facility = dummyFacility(facilityId)
                val scenario = dummyScenario(facility = facility)
                val oldScene = dummyScene(id = 1L, facility = facility)
                val scenarioScene = dummyScenarioScene(id = 1L, scenario = scenario, scene = oldScene)
                scenario.scenarioScenes.add(scenarioScene)

                every { scenario.facility.id } returns facilityId
                every { oldScene.facility.id } returns facilityId
                every { scenarioRepository.findByIdWithDetails(1L) } returns scenario
                every { sceneRepository.findAllById(listOf(999L)) } returns emptyList()

                Then("NOT_FOUND_SCENE 예외 발생") {
                    val updateRequest =
                        ScenarioUpdateRequest(
                            name = "테스트 시나리오",
                            description = null,
                            isActive = null,
                            scenarioScenes =
                                listOf(
                                    ScenarioSceneUpdateRequest(
                                        scenarioSceneId = 1L,
                                        sceneId = 999L,
                                        order = 1,
                                        duration = null,
                                    ),
                                ),
                        )
                    shouldThrowExactly<CustomException> {
                        scenarioService.updateScenario(facilityId, 1L, updateRequest)
                    }.errorCode shouldBe ErrorCode.NOT_FOUND_SCENE
                }
            }

            When("scenarioScene의 scene을 다른 facility의 scene으로 변경") {
                val facilityId = 1L
                val otherFacilityId = 2L
                val facility = dummyFacility(facilityId)
                val otherFacility = dummyFacility(otherFacilityId)
                val scenario = dummyScenario(facility = facility)
                val oldScene = dummyScene(id = 1L, facility = facility)
                val otherScene = dummyScene(id = 2L, facility = otherFacility)
                val scenarioScene = dummyScenarioScene(id = 1L, scenario = scenario, scene = oldScene)
                scenario.scenarioScenes.add(scenarioScene)

                every { scenario.facility.id } returns facilityId
                every { oldScene.facility.id } returns facilityId
                every { otherScene.facility.id } returns otherFacilityId
                every { scenarioRepository.findByIdWithDetails(1L) } returns scenario
                every { sceneRepository.findAllById(listOf(2L)) } returns listOf(otherScene)

                Then("UNMATCHED_FACILITY_SCENE 예외 발생") {
                    val updateRequest =
                        ScenarioUpdateRequest(
                            name = "테스트 시나리오",
                            description = null,
                            isActive = null,
                            scenarioScenes =
                                listOf(
                                    ScenarioSceneUpdateRequest(
                                        scenarioSceneId = 1L,
                                        sceneId = 2L,
                                        order = 1,
                                        duration = null,
                                    ),
                                ),
                        )
                    shouldThrowExactly<CustomException> {
                        scenarioService.updateScenario(facilityId, 1L, updateRequest)
                    }.errorCode shouldBe ErrorCode.UNMATCHED_FACILITY_SCENE
                }
            }
        }

        Given("Scenario 삭제를 진행할 때") {
            When("유효한 id로 삭제 요청") {
                every { scenarioRepository.deleteByIdAndFacilityId(1L, 1L) } returns 1L

                Then("정상 삭제") {
                    scenarioService.deleteScenario(1L, 1L)
                    verify(exactly = 1) { scenarioRepository.deleteByIdAndFacilityId(any(), any()) }
                }
            }
        }

        Given("Scenario 수정 시 Trigger 업데이트") {
            When("triggerId 없이 새 trigger 추가 요청") {
                val facilityId = 1L
                val facility = dummyFacility(facilityId)
                val scenario = dummyScenario(facility = facility)
                val newTrigger = mockk<Trigger>(relaxed = true)

                every { scenario.facility.id } returns facilityId
                every { scenarioRepository.findByIdWithDetails(1L) } returns scenario
                every { triggerService.createTrigger(any(), any()) } returns newTrigger

                Then("새 Trigger 생성 후 scenario에 추가") {
                    val updateRequest =
                        ScenarioUpdateRequest(
                            name = "수정된 시나리오",
                            description = null,
                            isActive = null,
                            scenarioScenes = null,
                            triggers =
                                listOf(
                                    TriggerRequest(
                                        triggerId = null,
                                        triggerType = TriggerRequestType.DAILY,
                                        hour = 10,
                                        minute = 0,
                                    ),
                                ),
                        )
                    scenarioService.updateScenario(facilityId, 1L, updateRequest)
                    verify(exactly = 1) { triggerService.createTrigger(any(), any()) }
                    scenario.triggers.size shouldBe 1
                }
            }

            When("triggerId로 기존 trigger 업데이트 요청") {
                val facilityId = 1L
                val facility = dummyFacility(facilityId)
                val scenario = dummyScenario(facility = facility)
                val existingTrigger = mockk<Trigger>(relaxed = true)

                scenario.triggers.add(existingTrigger)

                every { scenario.facility.id } returns facilityId
                every { existingTrigger.id } returns 10L
                every { scenarioRepository.findByIdWithDetails(1L) } returns scenario
                every { triggerService.updateTrigger(any(), any()) } returns Unit

                Then("기존 Trigger 업데이트") {
                    val updateRequest =
                        ScenarioUpdateRequest(
                            name = "수정된 시나리오",
                            description = null,
                            isActive = null,
                            scenarioScenes = null,
                            triggers =
                                listOf(
                                    TriggerRequest(
                                        triggerId = 10L,
                                        triggerType = TriggerRequestType.DAILY,
                                        hour = 15,
                                        minute = 30,
                                    ),
                                ),
                        )
                    scenarioService.updateScenario(facilityId, 1L, updateRequest)
                    verify(exactly = 1) { triggerService.updateTrigger(existingTrigger, any()) }
                }
            }

            When("요청에 없는 기존 trigger는 삭제") {
                val facilityId = 1L
                val facility = dummyFacility(facilityId)
                val scenario = dummyScenario(facility = facility)
                val existingTrigger1 = mockk<Trigger>(relaxed = true)
                val existingTrigger2 = mockk<Trigger>(relaxed = true)

                scenario.triggers.add(existingTrigger1)
                scenario.triggers.add(existingTrigger2)

                every { scenario.facility.id } returns facilityId
                every { existingTrigger1.id } returns 10L
                every { existingTrigger2.id } returns 20L
                every { scenarioRepository.findByIdWithDetails(1L) } returns scenario
                every { triggerService.updateTrigger(any(), any()) } returns Unit

                Then("요청에 포함된 trigger만 유지, 나머지 삭제") {
                    val updateRequest =
                        ScenarioUpdateRequest(
                            name = "수정된 시나리오",
                            description = null,
                            isActive = null,
                            scenarioScenes = null,
                            triggers =
                                listOf(
                                    TriggerRequest(
                                        triggerId = 10L,
                                        triggerType = TriggerRequestType.DAILY,
                                        hour = 10,
                                        minute = 0,
                                    ),
                                ),
                        )
                    scenarioService.updateScenario(facilityId, 1L, updateRequest)
                    scenario.triggers.size shouldBe 1
                    scenario.triggers[0].id shouldBe 10L
                }
            }

            When("triggers가 빈 리스트면 전체 삭제") {
                val facilityId = 1L
                val facility = dummyFacility(facilityId)
                val scenario = dummyScenario(facility = facility)
                val existingTrigger = mockk<Trigger>(relaxed = true)

                scenario.triggers.add(existingTrigger)

                every { scenario.facility.id } returns facilityId
                every { existingTrigger.id } returns 10L
                every { scenarioRepository.findByIdWithDetails(1L) } returns scenario

                Then("모든 Trigger 삭제됨") {
                    val updateRequest =
                        ScenarioUpdateRequest(
                            name = "수정된 시나리오",
                            description = null,
                            isActive = null,
                            scenarioScenes = null,
                            triggers = emptyList(),
                        )
                    scenarioService.updateScenario(facilityId, 1L, updateRequest)
                    scenario.triggers.size shouldBe 0
                }
            }

            When("존재하지 않는 triggerId로 업데이트 요청") {
                val facilityId = 1L
                val facility = dummyFacility(facilityId)
                val scenario = dummyScenario(facility = facility)

                every { scenario.facility.id } returns facilityId
                every { scenarioRepository.findByIdWithDetails(1L) } returns scenario

                Then("NOT_FOUND_TRIGGER 예외 발생") {
                    val updateRequest =
                        ScenarioUpdateRequest(
                            name = "수정된 시나리오",
                            description = null,
                            isActive = null,
                            scenarioScenes = null,
                            triggers =
                                listOf(
                                    TriggerRequest(
                                        triggerId = 999L,
                                        triggerType = TriggerRequestType.DAILY,
                                        hour = 10,
                                        minute = 0,
                                    ),
                                ),
                        )
                    shouldThrowExactly<CustomException> {
                        scenarioService.updateScenario(facilityId, 1L, updateRequest)
                    }.errorCode shouldBe ErrorCode.NOT_FOUND_TRIGGER
                }
            }
        }

        Given("Scenario와 Trigger를 함께 생성할 때") {
            When("단일 Trigger와 함께 생성 요청") {
                val facilityId = 1L
                val facility = dummyFacility(facilityId)
                val scenario = dummyScenario(facility = facility)
                val trigger = mockk<Trigger>()

                val createRequest =
                    ScenarioCreateRequest(
                        name = "테스트 시나리오",
                        description = "테스트 설명",
                        isActive = true,
                        scenarioSceneRequests = null,
                        triggers =
                            listOf(
                                TriggerRequest(
                                    triggerType = TriggerRequestType.DAILY,
                                    hour = 9,
                                    minute = 0,
                                ),
                            ),
                    )

                every { facilityRepository.findByIdOrNull(facilityId) } returns facility
                every { scenarioRepository.save(any()) } returns scenario
                every { triggerService.createTrigger(any(), any()) } returns trigger

                Then("Scenario와 Trigger 함께 생성") {
                    val savedId = scenarioService.createScenario(facilityId, createRequest)
                    savedId shouldBe 1L
                    verify(exactly = 1) { triggerService.createTrigger(any(), any()) }
                }
            }

            When("여러 Trigger와 함께 생성 요청") {
                val facilityId = 1L
                val facility = dummyFacility(facilityId)
                val scenario = dummyScenario(facility = facility)
                val trigger = mockk<Trigger>()

                val createRequest =
                    ScenarioCreateRequest(
                        name = "테스트 시나리오",
                        description = "테스트 설명",
                        isActive = true,
                        scenarioSceneRequests = null,
                        triggers =
                            listOf(
                                TriggerRequest(
                                    triggerType = TriggerRequestType.DAILY,
                                    hour = 9,
                                    minute = 0,
                                ),
                                TriggerRequest(
                                    triggerType = TriggerRequestType.DAILY,
                                    hour = 18,
                                    minute = 0,
                                ),
                            ),
                    )

                every { facilityRepository.findByIdOrNull(facilityId) } returns facility
                every { scenarioRepository.save(any()) } returns scenario
                every { triggerService.createTrigger(any(), any()) } returns trigger

                Then("Scenario와 여러 Trigger 함께 생성") {
                    val savedId = scenarioService.createScenario(facilityId, createRequest)
                    savedId shouldBe 1L
                    verify(exactly = 2) { triggerService.createTrigger(any(), any()) }
                }
            }

            When("Trigger 없이 생성 요청") {
                val facilityId = 1L
                val facility = dummyFacility(facilityId)
                val scenario = dummyScenario(facility = facility)

                val createRequest =
                    ScenarioCreateRequest(
                        name = "테스트 시나리오",
                        description = "테스트 설명",
                        isActive = true,
                        scenarioSceneRequests = null,
                        triggers = null,
                    )

                every { facilityRepository.findByIdOrNull(facilityId) } returns facility
                every { scenarioRepository.save(any()) } returns scenario

                Then("Scenario만 생성되고 Trigger는 생성되지 않음") {
                    val savedId = scenarioService.createScenario(facilityId, createRequest)
                    savedId shouldBe 1L
                    verify(exactly = 0) { triggerService.createTrigger(any(), any()) }
                }
            }
        }
    })
