package com.pluxity.onboarding.service

import com.pluxity.onboarding.dto.AdminUserCreateRequest
import com.pluxity.user.entity.Role
import com.pluxity.user.entity.RoleType
import com.pluxity.user.entity.User
import com.pluxity.user.repository.RoleRepository
import com.pluxity.user.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OnboardingUserService(
    private val roleRepository: RoleRepository,
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    @Transactional
    fun createAdminUser(request: AdminUserCreateRequest): Long {
        val user =
            User(
                username = request.username,
                password = passwordEncoder.encode(request.password),
                name = request.name,
                code = null
            )

        val role = roleRepository.findByName(RoleType.ADMIN.roleName)
            ?: roleRepository.save(Role(name = RoleType.ADMIN.roleName, description = "test role desc"))

        user.addRole(role)

        return userRepository.save(user).id!!
    }
}