package com.pluxity.onboarding

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class OnboardingJob(
    private val collector: OnboardingCollector,
    private val persister: OnboardingPersister,
) {
    val scope =
        CoroutineScope(
            Dispatchers.IO +
                CoroutineName("onboarding-job") +
                CoroutineExceptionHandler { _, throwable ->
                    log.error { "onboarding-job error: ${throwable.message}" }
                },
        )

    fun trigger() {
        scope.launch {
            val data = collector.collectData()
            persister.persistAndAlert(data)
        }
    }
}
