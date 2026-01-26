package com.pluxity.patrol.service

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.patrol.constant.TriggerTargetType
import com.pluxity.patrol.constant.TriggerType
import com.pluxity.patrol.dto.TriggerRequest
import com.pluxity.patrol.dto.TriggerTargetRequest
import com.pluxity.patrol.entity.dummyScenario
import com.pluxity.patrol.repository.TriggerRepository
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

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
                    result.dayOfWeek shouldBe 127
                    result.cronExpression shouldBe "30 9 * * *"
                    result.executeHour shouldBe 9
                    result.executeMinute shouldBe 30
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
                    result.dayOfWeek shouldBe 42
                    result.cronExpression shouldBe "0 12 * * 1,3,5"
                    result.executeHour shouldBe 12
                    result.executeMinute shouldBe 0
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
                    result.dayOfWeek shouldBe 127
                    result.month shouldBe 2
                    result.dayOfMonth shouldBe 15
                    result.executeHour shouldBe 18
                    result.executeMinute shouldBe 30
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
                    trigger.executeHour shouldBe 14
                    trigger.executeMinute shouldBe 0
                    trigger.dayOfWeek shouldBe 42
                    trigger.triggerTargets.size shouldBe 1
                    trigger.triggerTargets[0].targetId shouldBe "user-3"
                }
            }
        }
    })
