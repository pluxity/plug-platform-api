package com.pluxity.onboarding.service

import com.pluxity.device.repository.DeviceRepository
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.user.repository.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

/**
 * 온보딩 과제: 장비 제어 권한 체크 서비스 구현
 */
@Service
class OnboardingDeviceService(
    private val userRepository: UserRepository,
    private val deviceRepository: DeviceRepository
) {

    @Transactional(readOnly = true)
    fun controlDevice(userId: Long, deviceId: String) {
        // 사용자 조회
        val user = userRepository.findWithGraphById(userId)
            ?: throw CustomException(ErrorCode.NOT_FOUND_USER, userId)

        // 장비 조회
        val device = deviceRepository.findByIdWithCategory(deviceId)
            ?: throw CustomException(ErrorCode.NOT_FOUND_DEVICE, deviceId)

        // 권한 확인
        if (!user.canAccess(device.resourceType.name, device.resourceId)) {
            throw CustomException(ErrorCode.PERMISSION_DENIED, device.resourceId)
        }

        // 제어 성공 로그
        log.info {"Device Controlled"}
    }
}