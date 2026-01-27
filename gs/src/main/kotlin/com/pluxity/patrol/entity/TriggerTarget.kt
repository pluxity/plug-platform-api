package com.pluxity.patrol.entity

import com.pluxity.global.entity.IdentityIdEntity
import com.pluxity.patrol.constant.TriggerTargetType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Table(
    uniqueConstraints = [
        UniqueConstraint(
            name = "unique_trigger_target",
            columnNames = ["trigger_id", "target_type", "target_id"],
        ),
    ],
)
@Entity
class TriggerTarget(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trigger_id")
    var trigger: Trigger,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val targetType: TriggerTargetType = TriggerTargetType.USER,
    @Column(nullable = false)
    val targetId: String,
) : IdentityIdEntity()
