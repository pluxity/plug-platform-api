package com.pluxity.patrol.entity

import com.pluxity.global.entity.IdentityIdEntity
import com.pluxity.patrol.constant.TriggerType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.LocalDate

@Entity
class Trigger(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    val scenario: Scenario,
    @Column(nullable = false)
    val cronExpression: String,
    @Column(nullable = false)
    val triggerType: TriggerType,
    val executeMinute: Int? = null,
    val executeHour: Int? = null,
    val month: Int? = null,
    val dayOfMonth: Int? = null,
    val dayOfWeek: Int = 0,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val isActive: Boolean = true,
) : IdentityIdEntity()
