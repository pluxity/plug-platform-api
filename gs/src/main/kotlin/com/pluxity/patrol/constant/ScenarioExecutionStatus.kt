package com.pluxity.patrol.constant

enum class ScenarioExecutionStatus(
    displayName: String,
) {
    TRIGGERED("트리거됨"), // 스케줄러에서 알림 발송
    RUNNING("실행중"), // 프론트에서 실행 시작
    COMPLETED("완료"), // 정상 종료
    FAILED("실패"), // 에러 발생
    CANCELLED("취소됨"), // 사용자 취소
}
