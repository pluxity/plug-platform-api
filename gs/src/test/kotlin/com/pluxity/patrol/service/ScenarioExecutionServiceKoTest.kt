package com.pluxity.patrol.service

import base.entity.withId
import com.pluxity.facility.FacilityRepository
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.patrol.constant.ScenarioExecutionStatus
import com.pluxity.patrol.constant.TriggerSource
import com.pluxity.patrol.constant.TriggerTargetType
import com.pluxity.patrol.constant.TriggerType
import com.pluxity.patrol.entity.Scenario
import com.pluxity.patrol.entity.ScenarioExecution
import com.pluxity.patrol.entity.Trigger
import com.pluxity.patrol.entity.TriggerTarget
import com.pluxity.patrol.entity.dummyScenario
import com.pluxity.patrol.entity.dummyScenarioScene
import com.pluxity.patrol.entity.dummyScene
import com.pluxity.patrol.repository.ScenarioExecutionRepository
import com.pluxity.patrol.repository.ScenarioRepository
import facility.dummyFacility
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.springframework.data.repository.findByIdOrNull

class ScenarioExecutionServiceKoTest :
    BehaviorSpec({

        val scenarioExecutionRepository = mockk<ScenarioExecutionRepository>()
        val scenarioRepository = mockk<ScenarioRepository>()
        val facilityRepository = mockk<FacilityRepository>()

        val service =
            ScenarioExecutionService(
                scenarioExecutionRepository,
                scenarioRepository,
                facilityRepository,
            )

        Given("시나리오 자동 실행 (execute)") {
            When("트리거로 시나리오 실행하면") {
                val facility = dummyFacility(1L)
                every { facility.name } returns "테스트 시설"

                val scenario = dummyScenario(facility = facility)
                val scene = dummyScene(facility = facility)
                val scenarioScene = dummyScenarioScene(scenario = scenario, scene = scene)
                scenario.scenarioScenes.add(scenarioScene)

                val trigger = dummyTrigger(scenario = scenario)
                val triggerTarget = dummyTriggerTarget(trigger = trigger)
                trigger.triggerTargets.add(triggerTarget)

                val executionSlot = slot<ScenarioExecution>()
                every { scenarioExecutionRepository.save(capture(executionSlot)) } answers {
                    firstArg<ScenarioExecution>().withId(1L)
                }

                service.execute(scenario, trigger)

                Then("ScenarioExecution이 TRIGGERED 상태로 생성됨") {
                    executionSlot.captured.triggerType shouldBe TriggerSource.AUTO
                    executionSlot.captured.executionStatus shouldBe ScenarioExecutionStatus.TRIGGERED
                    executionSlot.captured.scenarioName shouldBe scenario.name
                    executionSlot.captured.facilityName shouldBe facility.name
                }

                Then("SceneExecution이 생성됨") {
                    executionSlot.captured.sceneExecutions.size shouldBe 1
                    executionSlot.captured.sceneExecutions[0].sceneName shouldBe scene.name
                }
            }
        }

        Given("시나리오 수동 실행 (startManually)") {
            When("유효한 scenarioId로 수동 실행하면") {
                val facility = dummyFacility(1L)
                every { facility.name } returns "테스트 시설"

                val scenario = dummyScenario(facility = facility)
                val scene = dummyScene(facility = facility)
                val scenarioScene = dummyScenarioScene(scenario = scenario, scene = scene)
                scenario.scenarioScenes.add(scenarioScene)

                val executionSlot = slot<ScenarioExecution>()
                every { scenarioRepository.findByIdWithDetails(1L) } returns scenario
                every { scenarioExecutionRepository.save(capture(executionSlot)) } answers {
                    firstArg<ScenarioExecution>().withId(100L)
                }

                val result = service.startManually(1L)

                Then("ScenarioExecution ID 반환") {
                    result shouldBe 100L
                }

                Then("MANUAL, RUNNING 상태로 생성됨") {
                    executionSlot.captured.triggerType shouldBe TriggerSource.MANUAL
                    executionSlot.captured.executionStatus shouldBe ScenarioExecutionStatus.RUNNING
                }

                Then("SceneExecution이 생성됨") {
                    executionSlot.captured.sceneExecutions.size shouldBe 1
                }
            }

            When("존재하지 않는 scenarioId로 실행하면") {
                every { scenarioRepository.findByIdWithDetails(999L) } returns null

                Then("NOT_FOUND_SCENARIO 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        service.startManually(999L)
                    }.errorCode shouldBe ErrorCode.NOT_FOUND_SCENARIO
                }
            }
        }

        Given("시나리오 실행 완료 (complete)") {
            When("RUNNING 상태에서 완료하면") {
                val execution =
                    dummyScenarioExecution(
                        executionStatus = ScenarioExecutionStatus.RUNNING,
                    )

                every { scenarioExecutionRepository.findByIdOrNull(1L) } returns execution

                service.complete(1L)

                Then("COMPLETED 상태로 변경됨") {
                    execution.executionStatus shouldBe ScenarioExecutionStatus.COMPLETED
                    execution.finishedAt shouldNotBe null
                }
            }

            When("RUNNING이 아닌 상태에서 완료하면") {
                val execution =
                    dummyScenarioExecution(
                        executionStatus = ScenarioExecutionStatus.CANCELLED,
                    )

                every { scenarioExecutionRepository.findByIdOrNull(2L) } returns execution

                Then("INVALID_EXECUTION_STATUS 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        service.complete(2L)
                    }.errorCode shouldBe ErrorCode.INVALID_EXECUTION_STATUS
                }
            }

            When("존재하지 않는 executionId로 완료하면") {
                every { scenarioExecutionRepository.findByIdOrNull(999L) } returns null

                Then("NOT_FOUND_SCENARIO_EXECUTION 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        service.complete(999L)
                    }.errorCode shouldBe ErrorCode.NOT_FOUND_SCENARIO_EXECUTION
                }
            }

            When("TRIGGERED 상태에서 완료하면") {
                val execution =
                    dummyScenarioExecution(
                        id = 10L,
                        executionStatus = ScenarioExecutionStatus.TRIGGERED,
                    )

                every { scenarioExecutionRepository.findByIdOrNull(10L) } returns execution

                service.complete(10L)

                Then("COMPLETED 상태로 변경됨") {
                    execution.executionStatus shouldBe ScenarioExecutionStatus.COMPLETED
                    execution.finishedAt shouldNotBe null
                }
            }
        }

        Given("시나리오 실행 실패 (fail)") {
            When("RUNNING 상태에서 실패 처리하면") {
                val execution =
                    dummyScenarioExecution(
                        executionStatus = ScenarioExecutionStatus.RUNNING,
                    )

                every { scenarioExecutionRepository.findByIdOrNull(1L) } returns execution

                service.fail(1L, "에러 메시지")

                Then("FAILED 상태로 변경되고 에러 메시지 저장됨") {
                    execution.executionStatus shouldBe ScenarioExecutionStatus.FAILED
                    execution.errorMessage shouldBe "에러 메시지"
                    execution.finishedAt shouldNotBe null
                }
            }

            When("RUNNING이 아닌 상태에서 실패 처리하면") {
                val execution =
                    dummyScenarioExecution(
                        executionStatus = ScenarioExecutionStatus.COMPLETED,
                    )

                every { scenarioExecutionRepository.findByIdOrNull(2L) } returns execution

                Then("INVALID_EXECUTION_STATUS 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        service.fail(2L, "에러")
                    }.errorCode shouldBe ErrorCode.INVALID_EXECUTION_STATUS
                }
            }

            When("존재하지 않는 executionId로 실패 처리하면") {
                every { scenarioExecutionRepository.findByIdOrNull(999L) } returns null

                Then("NOT_FOUND_SCENARIO_EXECUTION 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        service.fail(999L, "에러")
                    }.errorCode shouldBe ErrorCode.NOT_FOUND_SCENARIO_EXECUTION
                }
            }

            When("TRIGGERED 상태에서 실패 처리하면") {
                val execution =
                    dummyScenarioExecution(
                        id = 10L,
                        executionStatus = ScenarioExecutionStatus.TRIGGERED,
                    )

                every { scenarioExecutionRepository.findByIdOrNull(10L) } returns execution

                service.fail(10L, "트리거 에러")

                Then("FAILED 상태로 변경됨") {
                    execution.executionStatus shouldBe ScenarioExecutionStatus.FAILED
                    execution.errorMessage shouldBe "트리거 에러"
                    execution.finishedAt shouldNotBe null
                }
            }
        }

        Given("시나리오 실행 취소 (cancel)") {
            When("RUNNING 상태에서 취소하면") {
                val execution =
                    dummyScenarioExecution(
                        executionStatus = ScenarioExecutionStatus.RUNNING,
                    )

                every { scenarioExecutionRepository.findByIdOrNull(1L) } returns execution

                service.cancel(1L)

                Then("CANCELLED 상태로 변경됨") {
                    execution.executionStatus shouldBe ScenarioExecutionStatus.CANCELLED
                    execution.finishedAt shouldNotBe null
                }
            }

            When("TRIGGERED 상태에서 취소하면") {
                val execution =
                    dummyScenarioExecution(
                        executionStatus = ScenarioExecutionStatus.TRIGGERED,
                    )

                every { scenarioExecutionRepository.findByIdOrNull(2L) } returns execution

                service.cancel(2L)

                Then("CANCELLED 상태로 변경됨") {
                    execution.executionStatus shouldBe ScenarioExecutionStatus.CANCELLED
                }
            }

            When("COMPLETED 상태에서 취소하면") {
                val execution =
                    dummyScenarioExecution(
                        executionStatus = ScenarioExecutionStatus.COMPLETED,
                    )

                every { scenarioExecutionRepository.findByIdOrNull(3L) } returns execution

                Then("INVALID_EXECUTION_STATUS 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        service.cancel(3L)
                    }.errorCode shouldBe ErrorCode.INVALID_EXECUTION_STATUS
                }
            }
        }

        Given("시나리오 실행 상세 조회 (findById)") {
            When("유효한 id로 조회하면") {
                val execution = dummyScenarioExecution()

                every { scenarioExecutionRepository.findByIdWithDetails(1L) } returns execution

                val result = service.findById(1L)

                Then("상세 정보 반환") {
                    result.execution.id shouldBe 1L
                    result.execution.scenarioName shouldBe "테스트 시나리오"
                }
            }

            When("존재하지 않는 id로 조회하면") {
                every { scenarioExecutionRepository.findByIdWithDetails(999L) } returns null

                Then("NOT_FOUND_SCENARIO_EXECUTION 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        service.findById(999L)
                    }.errorCode shouldBe ErrorCode.NOT_FOUND_SCENARIO_EXECUTION
                }
            }
        }

        Given("시나리오 실행 이력 목록 조회 (findByFilters) ") {
            When("존재하지 않는 facilityId로 조회하면") {
                val invalidFacilityId = 999L

                every { facilityRepository.findByIdOrNull(invalidFacilityId) } returns null

                Then("NOT_FOUND_FACILITY 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        service.findByFilters(invalidFacilityId, null, null, null, null)
                    }.errorCode shouldBe ErrorCode.NOT_FOUND_FACILITY
                }
            }
            When("존재하지 않는 scenarioId로 조회하면") {
                val invalidScenarioId = 999L

                every { scenarioRepository.findByIdOrNull(invalidScenarioId) } returns null

                Then("NOT_FOUND_SCENARIO 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        service.findByFilters(null, invalidScenarioId, null, null, null)
                    }.errorCode shouldBe ErrorCode.NOT_FOUND_SCENARIO
                }
            }
        }
    })

private fun dummyScenarioExecution(
    id: Long = 1L,
    scenarioName: String = "테스트 시나리오",
    facilityName: String = "테스트 시설",
    triggerType: TriggerSource = TriggerSource.MANUAL,
    executionStatus: ScenarioExecutionStatus = ScenarioExecutionStatus.RUNNING,
): ScenarioExecution =
    ScenarioExecution(
        scenarioName = scenarioName,
        facilityName = facilityName,
        triggerType = triggerType,
        executionStatus = executionStatus,
    ).withId(id)

private fun dummyTrigger(
    id: Long = 1L,
    scenario: Scenario,
): Trigger =
    Trigger(
        scenario = scenario,
        cronExpression = "0 0 9 * * ?",
        triggerType = TriggerType.REPEAT,
        executeHour = 9,
        executeMinute = 0,
        dayOfWeek = 127,
    ).withId(id)

private fun dummyTriggerTarget(
    id: Long = 1L,
    trigger: Trigger,
    targetType: TriggerTargetType = TriggerTargetType.USER,
    targetId: String = "user-1",
): TriggerTarget =
    TriggerTarget(
        trigger = trigger,
        targetType = targetType,
        targetId = targetId,
    ).withId(id)
