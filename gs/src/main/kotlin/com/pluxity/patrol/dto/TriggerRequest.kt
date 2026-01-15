package com.pluxity.patrol.dto

import com.pluxity.patrol.constant.CronDayOfWeek
import com.pluxity.patrol.constant.TriggerRequestType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import java.time.LocalDate

@Schema(description = "트리거 수정 요청")
data class TriggerRequest(
    @field:Schema(
        description = "트리거 ID",
        example = "1",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val triggerId: Long? = null,
    @field:Schema(
        description = "트리거 타입",
        example = "DAILY",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val triggerType: TriggerRequestType,
    @field:Min(0, message = "시간은 0 이상이어야 합니다")
    @field:Max(23, message = "시간은 23 이하이어야 합니다")
    @field:Schema(
        description = "실행 시간 (0-23)",
        example = "9",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val hour: Int,
    @field:Min(0, message = "분은 0 이상이어야 합니다")
    @field:Max(59, message = "분은 59 이하이어야 합니다")
    @field:Schema(
        description = "실행 분 (0-59)",
        example = "0",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val minute: Int,
    @field:Schema(
        description = "실행 요일 목록 (WEEKLY 타입일 때 필수)",
        example = "[\"MON\", \"WED\", \"FRI\"]",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val daysOfWeek: List<CronDayOfWeek>? = null,
    @field:Schema(
        description = "일회성 실행 날짜 (ONCE 타입일 때 필수)",
        example = "2026-02-15",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val onceDate: LocalDate? = null,
    @field:Schema(
        description = "반복 시작 날짜 (DAILY, WEEKLY 타입에서 선택)",
        example = "2026-01-01",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val startDate: LocalDate? = null,
    @field:Schema(
        description = "반복 종료 날짜 (선택)",
        example = "2026-12-31",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val endDate: LocalDate? = null,
    @field:Schema(
        description = "활성화 여부",
        example = "true",
        defaultValue = "true",
    )
    val isActive: Boolean = true,
)
