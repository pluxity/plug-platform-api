package com.pluxity.patrol.service

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.patrol.constant.TriggerTargetType
import com.pluxity.patrol.constant.TriggerType
import com.pluxity.patrol.dto.TriggerRequest
import com.pluxity.patrol.dto.TriggerTargetRequest
import com.pluxity.patrol.entity.Trigger
import com.pluxity.patrol.entity.dummyScenario
import com.pluxity.patrol.repository.TriggerRepository
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDate

class TriggerServiceKoTest :
    BehaviorSpec({

        val triggerRepository = mockk<TriggerRepository>()
        val service = TriggerService(triggerRepository)

        beforeSpec {
            every { triggerRepository.save(any()) } answers { firstArg() }
        }

        Given("트리거 생성 할때") {

            val dummyScenario = dummyScenario()

            When("매일 오전 8시 설정하면") {
                val request =
                    TriggerRequest(
                        triggerType = TriggerType.REPEAT,
                        cronExpression = "30 9 * * *",
                    )

                val result = service.createTrigger(request, dummyScenario)

                Then("성공") {
                    result.cronExpression shouldBe "30 9 * * *"
                    result.nextExecutionTime shouldNotBe null
                    result.nextExecutionTime!!.hour shouldBe 9
                    result.nextExecutionTime!!.minute shouldBe 30
                }
            }

            When("매주 월, 수, 금 오후 12시 설정하면") {
                val request =
                    TriggerRequest(
                        triggerType = TriggerType.REPEAT,
                        cronExpression = "0 12 * * 1,3,5",
                    )

                val result = service.createTrigger(request, dummyScenario)

                Then("성공") {
                    result.cronExpression shouldBe "0 12 * * 1,3,5"
                    result.nextExecutionTime shouldNotBe null
                    result.nextExecutionTime!!.hour shouldBe 12
                    result.nextExecutionTime!!.minute shouldBe 0
                }
            }

            When("일회성 2월 15일 오후 6시 30분 설정하면") {
                val request =
                    TriggerRequest(
                        triggerType = TriggerType.ONCE,
                        cronExpression = "30 18 15 2 *",
                    )

                val result = service.createTrigger(request, dummyScenario)

                Then("성공") {
                    result.nextExecutionTime shouldNotBe null
                    result.nextExecutionTime!!.hour shouldBe 18
                    result.nextExecutionTime!!.minute shouldBe 30
                }
            }

            When("유효하지 않은 cron 표현식으로 생성하면") {
                val request =
                    TriggerRequest(
                        triggerType = TriggerType.REPEAT,
                        cronExpression = "invalid cron",
                    )

                Then("INVALID_CRON_EXPRESSION 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        service.createTrigger(request, dummyScenario)
                    }.errorCode shouldBe ErrorCode.INVALID_CRON_EXPRESSION
                }
            }

            When("triggerTargets와 함께 생성하면") {
                val request =
                    TriggerRequest(
                        triggerType = TriggerType.REPEAT,
                        cronExpression = "0 10 * * *",
                        triggerTargetRequests =
                            listOf(
                                TriggerTargetRequest(
                                    targetType = TriggerTargetType.USER,
                                    targetId = "user-1",
                                ),
                                TriggerTargetRequest(
                                    targetType = TriggerTargetType.USER,
                                    targetId = "user-2",
                                ),
                            ),
                    )

                val result = service.createTrigger(request, dummyScenario)

                Then("triggerTargets이 함께 생성됨") {
                    result.triggerTargets.size shouldBe 2
                    result.triggerTargets[0].targetId shouldBe "user-1"
                    result.triggerTargets[1].targetId shouldBe "user-2"
                }
            }

            When("triggerTargets이 중복으로 요청올때") {
                val request =
                    TriggerRequest(
                        triggerType = TriggerType.REPEAT,
                        cronExpression = "0 10 * * *",
                        triggerTargetRequests =
                            listOf(
                                TriggerTargetRequest(
                                    targetType = TriggerTargetType.USER,
                                    targetId = "user-1",
                                ),
                                TriggerTargetRequest(
                                    targetType = TriggerTargetType.USER,
                                    targetId = "user-1",
                                ),
                            ),
                    )
                val result = service.createTrigger(request, dummyScenario)

                Then("중복 제거") {
                    result.triggerTargets.size shouldBe 1
                    result.triggerTargets[0].targetId shouldBe "user-1"
                }
            }

            When("요청 startDate가 endDate보다 이후일때") {
                val request =
                    TriggerRequest(
                        triggerType = TriggerType.REPEAT,
                        cronExpression = "0 10 * * *",
                        startDate = LocalDate.now().plusYears(1),
                        endDate = LocalDate.now().plusMonths(6),
                    )

                Then("START_DATE_AFTER_END_DATE 예외발생") {
                    shouldThrowExactly<CustomException> {
                        service.createTrigger(request, dummyScenario)
                    }.errorCode shouldBe ErrorCode.START_DATE_AFTER_END_DATE
                }
            }
            When("endDate가 다음 실행시간보다 이전일때") {
                val request =
                    TriggerRequest(
                        triggerType = TriggerType.REPEAT,
                        cronExpression = "0 10 * * *",
                        endDate = LocalDate.now().minusDays(1),
                    )

                Then("END_DATE_ALREADY_PASSED 예외발생") {
                    shouldThrowExactly<CustomException> {
                        service.createTrigger(request, dummyScenario)
                    }.errorCode shouldBe ErrorCode.END_DATE_ALREADY_PASSED
                }
            }
        }

        Given("트리거 수정 할때") {
            When("cron 재파싱 및 target 재구성") {
                val scenario = dummyScenario()
                val createRequest =
                    TriggerRequest(
                        triggerType = TriggerType.REPEAT,
                        cronExpression = "30 9 * * *",
                        triggerTargetRequests =
                            listOf(
                                TriggerTargetRequest(
                                    targetType = TriggerTargetType.USER,
                                    targetId = "user-1",
                                ),
                            ),
                    )
                val trigger = service.createTrigger(createRequest, scenario)

                val updateRequest =
                    TriggerRequest(
                        triggerType = TriggerType.REPEAT,
                        cronExpression = "0 14 * * 1,3,5",
                        triggerTargetRequests =
                            listOf(
                                TriggerTargetRequest(
                                    targetType = TriggerTargetType.USER,
                                    targetId = "user-3",
                                ),
                            ),
                    )

                service.updateTrigger(trigger, updateRequest)

                Then("cron 값과 target이 업데이트됨") {
                    trigger.cronExpression shouldBe "0 14 * * 1,3,5"
                    trigger.triggerTargets.size shouldBe 1
                    trigger.triggerTargets[0].targetId shouldBe "user-3"
                    trigger.nextExecutionTime shouldNotBe null
                    trigger.nextExecutionTime!!.hour shouldBe 14
                    trigger.nextExecutionTime!!.minute shouldBe 0
                }
            }
            When("cron 재파싱 및 target 중복 제거") {
                val scenario = dummyScenario()
                val createRequest =
                    TriggerRequest(
                        triggerType = TriggerType.REPEAT,
                        cronExpression = "30 9 * * *",
                        triggerTargetRequests =
                            listOf(
                                TriggerTargetRequest(
                                    targetType = TriggerTargetType.USER,
                                    targetId = "user-1",
                                ),
                            ),
                    )
                val trigger = service.createTrigger(createRequest, scenario)

                val updateRequest =
                    TriggerRequest(
                        triggerType = TriggerType.REPEAT,
                        cronExpression = "0 14 * * 1,3,5",
                        triggerTargetRequests =
                            listOf(
                                TriggerTargetRequest(
                                    targetType = TriggerTargetType.USER,
                                    targetId = "user-3",
                                ),
                                TriggerTargetRequest(
                                    targetType = TriggerTargetType.USER,
                                    targetId = "user-3",
                                ),
                            ),
                    )

                service.updateTrigger(trigger, updateRequest)

                Then("cron 값과 target이 업데이트됨") {
                    trigger.cronExpression shouldBe "0 14 * * 1,3,5"
                    trigger.triggerTargets.size shouldBe 1
                    trigger.triggerTargets[0].targetId shouldBe "user-3"
                }
            }
        }

        Given("트리거가 실행되어 다음 실행 시간 업데이트될때") {
            When("유효한 트리거일때") {
                val trigger =
                    Trigger(
                        scenario = dummyScenario(),
                        triggerType = TriggerType.REPEAT,
                        cronExpression = "0 10 * * *",
                        endDate = LocalDate.now().plusYears(1),
                    )

                service.updateNextExecutionTime(trigger)

                Then("다음 실행 시간을 계산한다.") {
                    trigger.nextExecutionTime!!.hour shouldBe 10
                    trigger.nextExecutionTime!!.minute shouldBe 0
                    trigger.isActive shouldBe true
                }
            }
            When("유효하지 않은 트리거일때") {
                val trigger =
                    Trigger(
                        scenario = dummyScenario(),
                        triggerType = TriggerType.REPEAT,
                        cronExpression = "0 10 * * *",
                        endDate = LocalDate.now().minusDays(1),
                    )

                service.updateNextExecutionTime(trigger)

                Then("비활성화 된다.") {
                    trigger.nextExecutionTime shouldBe null
                    trigger.isActive shouldBe false
                }
            }
            When("다음 실행 시간보다 startDate가 이후일때") {
                val futureStartDate = LocalDate.now().plusMonths(6)
                val trigger =
                    Trigger(
                        scenario = dummyScenario(),
                        triggerType = TriggerType.REPEAT,
                        cronExpression = "0 10 * * *",
                        startDate = futureStartDate,
                        endDate = LocalDate.now().plusYears(1),
                    )

                service.updateNextExecutionTime(trigger)

                Then("startDate에 맞게 다시 계산한다.") {
                    trigger.nextExecutionTime!!.toLocalDate() shouldBe futureStartDate
                    trigger.nextExecutionTime!!.hour shouldBe 10
                    trigger.nextExecutionTime!!.minute shouldBe 0
                }
            }
            When("다음 실행시간 보다 endDate가 이전일때") {
                val trigger =
                    Trigger(
                        scenario = dummyScenario(),
                        triggerType = TriggerType.REPEAT,
                        cronExpression = "0 10 * * *",
                        startDate = LocalDate.now().minusMonths(6),
                        endDate = LocalDate.now().minusDays(1),
                    )

                service.updateNextExecutionTime(trigger)

                Then("해당 트리거는 비활성화된다.") {
                    trigger.isActive shouldBe false
                }
            }
        }
    })
