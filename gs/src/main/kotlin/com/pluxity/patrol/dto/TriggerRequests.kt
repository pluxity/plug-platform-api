package com.pluxity.patrol.dto

import com.pluxity.patrol.constant.TriggerTargetType
import com.pluxity.patrol.constant.TriggerType
import io.swagger.v3.oas.annotations.media.Schema
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
        description = """
            UNIX 표준 5자리 크론식 (분 시 일 월 요일).
            - 분/시/일/월: 숫자 또는 * 만 허용
            - 요일: 숫자, *, - 허용 (0,7:일, 1:월... 6:토)
            - ONCE일 경우 일/월 표시
            - 예시: '30 9 * * *' (매일 09:30 실행), '0 10 * * 1,5' (월,금 10시 실행) , '0 10 * * 1-5' (월-금 10시 실행), '0 10 25 1 *'(1월 25일 10시 실행)

        """,
        type = "string",
        example = "'30 9 * * 1,5'",
    )
    val cronExpression: String,
    @field:Schema(
        description = "트리거 타입",
        example = "REPEAT",
        requiredMode = Schema.RequiredMode.REQUIRED,
    )
    val triggerType: TriggerType,
    @field:Schema(
        description = "반복 시작 날짜",
        example = "2026-01-01",
        requiredMode = Schema.RequiredMode.NOT_REQUIRED,
    )
    val startDate: LocalDate? = null,
    @field:Schema(
        description = "반복 종료 날짜",
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
    @field:Schema(
        description = "트리거 타겟",
    )
    val triggerTargetRequests: List<TriggerTargetRequest>? = null,
)

@Schema(description = "트리거 타겟 요청")
data class TriggerTargetRequest(
    @field:Schema(description = "타겟 타입 (USER, GROUP 등)", defaultValue = "USER")
    val targetType: TriggerTargetType = TriggerTargetType.USER,
    @field:Schema(description = "타겟 ID 또는 식별자", example = "user-123")
    val targetId: String,
)
