package com.pluxity.global.aop

import com.pluxity.global.annotation.CheckPermission
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.constant.SecurityConstants
import com.pluxity.global.exception.CustomException
import com.pluxity.user.entity.PermissionCheckType
import com.pluxity.user.entity.PermissionStrategyResolver
import com.pluxity.user.entity.ResourceAllPermissible
import com.pluxity.user.entity.RoleType
import com.pluxity.user.entity.User
import com.pluxity.user.service.UserService
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.context.annotation.Profile
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Aspect
@Component
@Profile("!local")
class PermissionCheckAspect(
    private val userService: UserService,
    private val strategyResolver: PermissionStrategyResolver,
) {
    @Around("@annotation(checkPermission)")
    fun execute(
        joinPoint: ProceedingJoinPoint,
        checkPermission: CheckPermission,
    ): Any {
        val user = getCurrentUserIfApplicable() ?: return joinPoint.proceed()
        val strategy = strategyResolver.resolve(checkPermission.type)
        val returnObject = joinPoint.proceed()

        return when (checkPermission.phase) {
            PermissionCheckType.SINGLE_ITEM -> {
                if (!strategy.check(user, returnObject)) {
                    throw CustomException(ErrorCode.PERMISSION_DENIED)
                }
                returnObject
            }

            PermissionCheckType.ITEM_LIST -> {
                when (returnObject) {
                    is MutableCollection<*> -> {
                        returnObject.removeIf { item: Any? -> item == null || !strategy.check(user, item) }
                    }
                }
                returnObject
            }

            PermissionCheckType.FULL_ACCESS -> {
                if (!strategy.check(user, ResourceAllPermissible(checkPermission.resourceType))) {
                    when (returnObject) {
                        is MutableCollection<*> -> returnObject.clear()
                        else -> throw CustomException(ErrorCode.PERMISSION_DENIED)
                    }
                }
                returnObject
            }
        }
    }

    private fun getCurrentUserIfApplicable(): User? {
        val authentication =
            SecurityContextHolder.getContext().authentication
                ?: throw CustomException(ErrorCode.PERMISSION_DENIED)

        if (!authentication.isAuthenticated || SecurityConstants.ANONYMOUS_USER == authentication.principal) {
            throw CustomException(ErrorCode.PERMISSION_DENIED)
        }

        val user = userService.findUserByUsername(authentication.name)

        return if (user.getRoles().any { it.auth == RoleType.ADMIN.roleName }) null else user
    }
}
