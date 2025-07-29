package com.pluxity.global.aop;

import static com.pluxity.global.constant.ErrorCode.PERMISSION_DENIED;

import com.pluxity.global.annotation.CheckPermission;
import com.pluxity.global.config.SpelExpressionEvaluator;
import com.pluxity.global.exception.CustomException;
import com.pluxity.user.entity.ExecutionPhase;
import com.pluxity.user.entity.PermissionStrategy;
import com.pluxity.user.entity.PermissionStrategyResolver;
import com.pluxity.user.entity.User;
import com.pluxity.user.service.UserService;
import java.util.Collection;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
@Profile("!local")
public class PermissionCheckAspect {

    private final UserService userService;

    private final SpelExpressionEvaluator spelEvaluator;

    private final PermissionStrategyResolver strategyResolver;

    @Around("@annotation(checkPermission)")
    public Object execute(ProceedingJoinPoint joinPoint, CheckPermission checkPermission)
            throws Throwable {

        User user = getCurrentUserIfApplicable();
        if (user == null) {
            return joinPoint.proceed();
        }

        PermissionStrategy strategy = strategyResolver.resolve(checkPermission.type());

        if (checkPermission.phase() == ExecutionPhase.BEFORE) {
            Object target = evaluateSpel(joinPoint, checkPermission.target(), null);
            if (!strategy.check(user, target)) {
                throw new CustomException(PERMISSION_DENIED);
            }
        }

        Object returnObject = joinPoint.proceed();

        if (checkPermission.phase() == ExecutionPhase.AFTER) {
            Object target = evaluateSpel(joinPoint, checkPermission.target(), returnObject);
            if (!strategy.check(user, target)) {
                throw new CustomException(PERMISSION_DENIED);
            }
        }

        if (checkPermission.phase() == ExecutionPhase.FILTER) {
            Object target = evaluateSpel(joinPoint, checkPermission.target(), returnObject);
            if (target instanceof Collection<?> collection) {
                collection.removeIf(item -> !strategy.check(user, item));
            }
        }

        return returnObject;
    }

    private Object evaluateSpel(
            ProceedingJoinPoint joinPoint, String expression, Object returnObject) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        return spelEvaluator.evaluate(
                expression, signature.getParameterNames(), joinPoint.getArgs(), returnObject, Object.class);
    }

    private User getCurrentUserIfApplicable() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new CustomException(PERMISSION_DENIED);
        }

        User user = userService.findUserByUsername(authentication.getName());

        if (user.getRoles().stream().anyMatch(role -> "ADMIN".equals(role.getName()))) {
            return null;
        }
        return user;
    }
}
