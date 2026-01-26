package com.pluxity.patrol.service

import com.pluxity.patrol.constant.TriggerType
import com.pluxity.patrol.dto.TriggerRequest
import com.pluxity.patrol.entity.dummyScenario
import com.pluxity.patrol.repository.TriggerRepository
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
        }
    })
