package com.pluxity.patrol.constant

enum class DeviceAction(
    val displayName: String,
) {
    // CCTV
    VIEW("보기"),

    // LIGHT
    TURN_ON("켜기"),
    TURN_OFF("끄기"),

    // SHUTTER
    OPEN("올리기"),
    CLOSE("내리기"),

    // 공통
    GET_STATUS("상태 조회"),
}
