package com.pluxity.global.aop

import com.pluxity.global.annotation.CheckPermission
import com.pluxity.global.constant.ErrorCode
import com.pluxity.global.exception.CustomException
import com.pluxity.user.entity.ExecutionPhase
import com.pluxity.user.entity.PermissionStrategyResolver
import com.pluxity.user.entity.Role
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
    @Throws(Throwable::class)
    fun execute(
        joinPoint: ProceedingJoinPoint,
        checkPermission: CheckPermission,
    ): Any {
        val user = this.currentUserIfApplicable ?: return joinPoint.proceed()

        val strategy = strategyResolver.resolve(checkPermission.type)

        val returnObject = joinPoint.proceed()

        if (checkPermission.phase == ExecutionPhase.AFTER) {
            if (!strategy.check(user, returnObject)) {
                throw CustomException(ErrorCode.PERMISSION_DENIED)
            }
        }

        if (checkPermission.phase == ExecutionPhase.FILTER) {
            if (returnObject is MutableCollection<*>) {
                returnObject.removeIf { item: Any? -> !strategy.check(user, item!!) }
            }
        }

        return returnObject
    }

    private val currentUserIfApplicable: User?
        get() {
            val authentication =
                SecurityContextHolder.getContext().authentication
            if (authentication == null || !authentication.isAuthenticated || "anonymousUser" == authentication.principal) {
                throw CustomException(ErrorCode.PERMISSION_DENIED)
            }

            val user = userService.findUserByUsername(authentication.name)

            if (user.getRoles().stream().anyMatch { role: Role? -> "ADMIN" == role?.name }) {
                return null
            }
            return user
        }
}
