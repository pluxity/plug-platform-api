package com.pluxity.patrol.dto

import com.pluxity.patrol.constant.TriggerTargetType

data class TriggerTargetRequest(
    val targetType: TriggerTargetType = TriggerTargetType.USER,
    val targetId: String,
)
