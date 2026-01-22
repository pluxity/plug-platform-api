package com.pluxity.patrol.entity

import com.pluxity.global.entity.IdentityIdEntity
import com.pluxity.patrol.constant.TriggerType
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import java.time.LocalDate

@Entity
class Trigger(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    var scenario: Scenario,
    @OneToMany(mappedBy = "trigger", cascade = [CascadeType.ALL], orphanRemoval = true)
    val triggerTargets: MutableList<TriggerTarget> = mutableListOf(),
    @Column(nullable = false)
    var cronExpression: String,
    @Column(nullable = false)
    var triggerType: TriggerType,
    var executeMinute: Int? = null,
    var executeHour: Int? = null,
    var month: Int? = null,
    var dayOfMonth: Int? = null,
    var dayOfWeek: Int = 0,
    var startDate: LocalDate? = null,
    var endDate: LocalDate? = null,
    var isActive: Boolean = true,
) : IdentityIdEntity() {
    fun updateTrigger(
        triggerType: TriggerType,
        hour: Int,
        minute: Int,
        dayOfWeek: Int,
        dayOfMonth: Int?,
        month: Int?,
        startDate: LocalDate,
        endDate: LocalDate?,
        cronExpression: String,
        isActive: Boolean,
    ) {
        this.triggerType = triggerType
        this.executeHour = hour
        this.executeMinute = minute
        this.dayOfWeek = dayOfWeek
        this.dayOfMonth = dayOfMonth
        this.month = month
        this.startDate = startDate
        this.endDate = endDate
        this.cronExpression = cronExpression
        this.isActive = isActive
    }

    fun addTriggerTarget(triggerTarget: TriggerTarget) {
        this.triggerTargets.add(triggerTarget)
        triggerTarget.trigger = this
    }
}
