package com.pluxity.patrol.entity

import com.pluxity.global.entity.IdentityIdEntity
import com.pluxity.patrol.constant.TriggerTargetType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class TriggerTarget(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trigger_id")
    var trigger: Trigger,
    @Enumerated(EnumType.STRING)
    val targetType: TriggerTargetType = TriggerTargetType.USER,
    val targetId: String,
) : IdentityIdEntity()
