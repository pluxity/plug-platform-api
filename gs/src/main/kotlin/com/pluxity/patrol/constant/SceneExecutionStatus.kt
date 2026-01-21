package com.pluxity.patrol.constant

enum class SceneExecutionStatus(
    displayName: String,
) {
    PENDING("대기"),
    RUNNING("실행중"),
    COMPLETED("완료"),
    FAILED("실패"),
    SKIPPED("건너뜀"),
}
