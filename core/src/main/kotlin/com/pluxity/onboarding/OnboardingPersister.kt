package com.pluxity.onboarding

import com.pluxity.climate.ClimateData
import com.pluxity.climate.ClimateDataRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OnboardingPersister(
    private val climateDataRepository: ClimateDataRepository,
    private val eventPublisher: ApplicationEventPublisher,
) {
    @Transactional
    fun persistAndAlert(dataList: List<ClimateData>) {
        saveClimateData(dataList)
        sendAlert(dataList)
    }

    private fun saveClimateData(dataList: List<ClimateData>) {
        climateDataRepository.saveAll(dataList)
    }

    private fun sendAlert(dataList: List<ClimateData>) {
        val targetDeviceIds =
            dataList
                .filter { it.temperature!! >= 80 }
                .mapNotNull { it.deviceId }
                .distinct()

        if (targetDeviceIds.isNotEmpty()) {
            eventPublisher.publishEvent(AlertEvent(targetDeviceIds))
        }
    }
}
