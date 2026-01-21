package com.pluxity.patrol.dto

import com.pluxity.patrol.constant.ScenarioExecutionStatus
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDate

data class ScenarioExecutionSearchRequest(
    @field:Parameter(description = "실행 시작일 범위 시작 (YYYY-MM-DD)")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val startDate: LocalDate? = null,
    @field:Parameter(description = "실행 시작일 범위 끝 (YYYY-MM-DD)")
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    val endDate: LocalDate? = null,
    @field:Parameter(description = "실행 상태 필터")
    val status: ScenarioExecutionStatus? = null,
)
