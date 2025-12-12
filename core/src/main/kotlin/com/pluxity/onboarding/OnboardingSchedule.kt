package com.pluxity.onboarding

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["onboarding.schedule"], havingValue = "true")
class OnboardingSchedule(
    private val job: OnboardingJob,
) {
    @Scheduled(cron = "0 0/1 * * * *")
    fun schedule() {
        job.trigger()
    }
}
