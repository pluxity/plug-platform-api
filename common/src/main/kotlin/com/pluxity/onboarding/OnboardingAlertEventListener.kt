package com.pluxity.onboarding

import com.pluxity.device.repository.DeviceRepository
import com.pluxity.permission.ResourceType
import com.pluxity.user.repository.UserRepository
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OnboardingAlertEventListener(
    private val alertRepository: AlertRepository,
    private val deviceRepository: DeviceRepository,
    private val userRepository: UserRepository,
) {
    @Async
    @EventListener
    @Transactional
    fun handleAlert(event: AlertEvent) {
        val devices = deviceRepository.findByIdInWithFeatureAndFacility(event.targetDeviceIds) ?: return

        devices.forEach { device ->
            val facility = device.feature?.facility ?: return
            val users = userRepository.findByPermission(ResourceType.FACILITY.name, facility.id!!)

            users.forEach { user ->
                alertRepository.save(
                    Alert(
                        user = user,
                        facility = facility,
                        device = device,
                    ),
                )
            }
        }
    }
}
