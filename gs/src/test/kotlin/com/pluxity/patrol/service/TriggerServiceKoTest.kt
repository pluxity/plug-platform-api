package com.pluxity.patrol.service

import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.patrol.constant.CronDayOfWeek
import com.pluxity.patrol.dto.TriggerRequest
import com.pluxity.patrol.dto.TriggerRequestType
import com.pluxity.patrol.entity.dummyScenario
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class TriggerServiceKoTest :
    BehaviorSpec({

        val service = TriggerService()

        Given("트리거 생성 할때") {

            val dummyScenario = dummyScenario()

            When("매일 오전 8시 설정하면") {
                val request =
                    TriggerRequest(
                        triggerType = TriggerRequestType.DAILY,
                        hour = 8,
                        minute = 0,
                    )

                val result = service.createTrigger(request, dummyScenario)

                Then("성공") {
                    result.dayOfWeek shouldBe 127
                    result.cronExpression shouldBe "0 0 8 * * ?"
                    result.executeHour shouldBe 8
                    result.executeMinute shouldBe 0
                }
            }

            When("매주 월, 수, 금 오후 12시 설정하면") {
                val request =
                    TriggerRequest(
                        triggerType = TriggerRequestType.WEEKLY,
                        hour = 12,
                        minute = 0,
                        daysOfWeek = listOf(CronDayOfWeek.MON, CronDayOfWeek.WED, CronDayOfWeek.FRI),
                    )

                val result = service.createTrigger(request, dummyScenario)

                Then("성공") {
                    result.dayOfWeek shouldBe 42
                    result.cronExpression shouldBe "0 0 12 ? * 1,3,5"
                }
            }

            When("일회성 2026년 2월 15일 오후 6시 30분 설정하면") {
                val request =
                    TriggerRequest(
                        triggerType = TriggerRequestType.ONCE,
                        hour = 18,
                        minute = 30,
                        onceDate = LocalDate.of(2026, 2, 15),
                    )

                val result = service.createTrigger(request, dummyScenario)

                Then("성공") {
                    result.dayOfWeek shouldBe 0
                    result.month shouldBe 2
                    result.dayOfMonth shouldBe 15
                    result.cronExpression shouldBe "once:2026-02-15 18:30"
                }
            }

            When("비활성 상태로 생성하면") {
                val request =
                    TriggerRequest(
                        triggerType = TriggerRequestType.DAILY,
                        hour = 9,
                        minute = 0,
                        isActive = false,
                    )

                val result = service.createTrigger(request, dummyScenario)

                Then("isActive가 false") {
                    result.isActive shouldBe false
                }
            }
        }

        Given("트리거 생성 시 필수값 검증") {

            val dummyScenario = dummyScenario()

            When("WEEKLY인데 요일을 선택하지 않으면") {
                val request =
                    TriggerRequest(
                        triggerType = TriggerRequestType.WEEKLY,
                        hour = 9,
                        minute = 0,
                        daysOfWeek = null,
                    )

                Then("TRIGGER_WEEKLY_DAYS_REQUIRED 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        service.createTrigger(request, dummyScenario)
                    }.errorCode shouldBe ErrorCode.TRIGGER_WEEKLY_DAYS_REQUIRED
                }
            }

            When("WEEKLY인데 요일이 빈 리스트면") {
                val request =
                    TriggerRequest(
                        triggerType = TriggerRequestType.WEEKLY,
                        hour = 9,
                        minute = 0,
                        daysOfWeek = emptyList(),
                    )

                Then("TRIGGER_WEEKLY_DAYS_REQUIRED 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        service.createTrigger(request, dummyScenario)
                    }.errorCode shouldBe ErrorCode.TRIGGER_WEEKLY_DAYS_REQUIRED
                }
            }

            When("ONCE인데 날짜를 설정하지 않으면") {
                val request =
                    TriggerRequest(
                        triggerType = TriggerRequestType.ONCE,
                        hour = 18,
                        minute = 30,
                        onceDate = null,
                    )

                Then("TRIGGER_ONCE_DATE_REQUIRED 예외 발생") {
                    shouldThrowExactly<CustomException> {
                        service.createTrigger(request, dummyScenario)
                    }.errorCode shouldBe ErrorCode.TRIGGER_ONCE_DATE_REQUIRED
                }
            }
        }
    })
