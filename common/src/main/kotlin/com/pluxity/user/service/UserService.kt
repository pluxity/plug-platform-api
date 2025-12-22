package com.pluxity.user.service

import com.pluxity.authentication.repository.RefreshTokenRepository
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.global.properties.UserProperties
import com.pluxity.global.utils.SortUtils
import com.pluxity.user.dto.UserCreateRequest
import com.pluxity.user.dto.UserLoggedInResponse
import com.pluxity.user.dto.UserPasswordUpdateRequest
import com.pluxity.user.dto.UserResponse
import com.pluxity.user.dto.UserRoleUpdateRequest
import com.pluxity.user.dto.UserUpdateRequest
import com.pluxity.user.dto.toUserLoggedInResponse
import com.pluxity.user.dto.toUserResponse
import com.pluxity.user.entity.Role
import com.pluxity.user.entity.User
import com.pluxity.user.repository.RoleRepository
import com.pluxity.user.repository.UserRepository
import com.pluxity.user.repository.UserRoleRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val roleRepository: RoleRepository,
    private val passwordEncoder: PasswordEncoder,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val userRoleRepository: UserRoleRepository,
    private val userProperties: UserProperties,
) {
    @Transactional(readOnly = true)
    fun findById(id: Long): UserResponse = findUserById(id).toUserResponse()

    @Transactional(readOnly = true)
    fun findAll(): List<UserResponse> = userRepository.findAllBy(SortUtils.orderByCreatedAtDesc).map { it.toUserResponse() }

    @Transactional(readOnly = true)
    fun findByUsername(username: String): UserResponse = findUserByUsername(username).toUserResponse()

    @Transactional
    fun save(request: UserCreateRequest): UserResponse {
        val user =
            User(
                username = request.username,
                password = passwordEncoder.encode(request.password),
                name = request.name,
                code = request.code,
                phoneNumber = request.phoneNumber,
                department = request.department,
            )

        if (request.roleIds.isNotEmpty()) {
            val roles = request.roleIds.map { findRoleById(it) }
            user.addRoles(roles)
        }

        return userRepository.save(user).toUserResponse()
    }

    @Transactional
    fun update(
        id: Long,
        request: UserUpdateRequest,
    ): UserResponse {
        val user = findUserById(id)
        updateUserFields(user, request)
        changeRole(request.roleIds, user)
        return user.toUserResponse()
    }

    private fun changeRole(
        roleIds: List<Long>?,
        user: User,
    ) {
        if (roleIds == null) {
            return
        }
        val newRoles = roleRepository.findAllById(roleIds)
        val newRoleIds =
            newRoles
                .map {
                    it.id
                }.toSet()

        val rolesToRemove =
            user.userRoles
                .filter {
                    !newRoleIds.contains(it.role.id)
                }

        if (rolesToRemove.isNotEmpty()) {
            userRoleRepository.deleteAll(rolesToRemove)
        }
        user.updateRoles(newRoles)
    }

    @Transactional
    fun delete(id: Long) {
        val user = findUserById(id)
        userRoleRepository.deleteAllByUser(user)
        userRepository.delete(user)
    }

    @Transactional
    fun removeRoleFromUser(
        userId: Long,
        roleId: Long,
    ) {
        val user = findUserById(userId)
        val role = findRoleById(roleId)
        user.removeRole(role)
    }

    private fun findUserById(id: Long): User =
        userRepository.findWithGraphById(id)
            ?: throw CustomException(ErrorCode.NOT_FOUND_USER, id)

    private fun findRoleById(id: Long): Role =
        roleRepository
            .findByIdOrNull(id)
            ?: throw CustomException(ErrorCode.NOT_FOUND_ROLE, id)

    fun findUserByUsername(username: String): User =
        userRepository
            .findByUsername(username)
            ?: throw CustomException(ErrorCode.NOT_FOUND_USER, username)

    @Transactional
    fun updateUserPassword(
        id: Long,
        request: UserPasswordUpdateRequest,
    ) {
        val user = findUserById(id)

        if (!passwordEncoder.matches(request.currentPassword, user.password)) {
            throw CustomException(ErrorCode.INVALID_ID_OR_PASSWORD, "현재 비밀번호가 일치하지 않습니다.")
        }

        user.changePassword(passwordEncoder.encode(request.newPassword))
    }

    @Transactional
    fun updateUserRoles(
        id: Long,
        request: UserRoleUpdateRequest,
    ) {
        val user = findUserById(id)
        changeRole(request.roleIds, user)
    }

    private fun updateUserFields(
        user: User,
        request: UserUpdateRequest,
    ) {
        if (!request.name.isNullOrBlank()) {
            user.changeName(request.name)
        }
        if (!request.code.isNullOrBlank()) {
            user.changeCode(request.code)
        }
        if (request.phoneNumber != null) {
            user.changePhoneNumber(request.phoneNumber)
        }
        if (request.department != null) {
            user.changeDepartment(request.department)
        }
    }

    @Transactional(readOnly = true)
    fun isLoggedIn(): List<UserLoggedInResponse> {
        val users = userRepository.findAllBy(SortUtils.orderByCreatedAtDesc)
        return users.map { user ->
            val refreshToken = refreshTokenRepository.findByIdOrNull(user.username)
            val isLoggedIn = refreshToken != null
            user.toUserLoggedInResponse(isLoggedIn)
        }
    }

    @Transactional
    fun initPassword(id: Long) {
        val user = findUserById(id)
        user.initPassword(passwordEncoder.encode(userProperties.initPassword))
    }

    @Transactional
    fun updateUserPassword(
        name: String,
        dto: UserPasswordUpdateRequest,
    ) {
        val id = findByUsername(name).id
        updateUserPassword(id, dto)
    }
}
