package com.pluxity.patrol.constant

enum class DeviceType(
    val displayName: String,
    val availableActions: List<DeviceAction>,
) {
    CCTV("CCTV", listOf(DeviceAction.VIEW)),
    TEMPERATURE_HUMIDITY("온습도계", listOf(DeviceAction.GET_STATUS)),
    SHUTTER("셔터", listOf(DeviceAction.OPEN, DeviceAction.CLOSE, DeviceAction.GET_STATUS)),
    LIGHT("조명", listOf(DeviceAction.TURN_ON, DeviceAction.TURN_OFF, DeviceAction.GET_STATUS)),
    ;

    fun supports(action: DeviceAction): Boolean = availableActions.contains(action)
}
