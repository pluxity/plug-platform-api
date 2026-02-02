package com.pluxity.patrol.entity

import com.pluxity.global.entity.IdentityIdEntity
import com.pluxity.patrol.constant.TriggerType
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Index
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "trigger",
    indexes = [
        Index(
            name = "idx_trigger_schedule",
            columnList = "is_active, next_execution_time",
        ),
    ],
)
class Trigger(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    var scenario: Scenario,
    @OneToMany(mappedBy = "trigger", cascade = [CascadeType.ALL], orphanRemoval = true)
    val triggerTargets: MutableList<TriggerTarget> = mutableListOf(),
    @Column(nullable = false, length = 100)
    var cronExpression: String,
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    var triggerType: TriggerType,
    @Column(nullable = false)
    var nextExecutionTime: LocalDateTime,
    var startDate: LocalDate? = null,
    var endDate: LocalDate? = null,
    var isActive: Boolean = true,
) : IdentityIdEntity() {
    fun updateTrigger(
        triggerType: TriggerType,
        nextExecutionTime: LocalDateTime,
        startDate: LocalDate,
        endDate: LocalDate?,
        cronExpression: String,
        isActive: Boolean,
    ) {
        this.triggerType = triggerType
        this.nextExecutionTime = nextExecutionTime
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
