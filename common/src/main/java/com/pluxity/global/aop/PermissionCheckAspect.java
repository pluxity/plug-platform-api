package com.pluxity.global.aop;

import static com.pluxity.global.constant.ErrorCode.NOT_FOUND_RESOURCE_ID;
import static com.pluxity.global.constant.ErrorCode.PERMISSION_DENIED;

import com.pluxity.global.annotation.CheckPermission;
import com.pluxity.global.annotation.CheckPermissionAfter;
import com.pluxity.global.annotation.CheckPermissionAll;
import com.pluxity.global.annotation.CheckPermissionCategory;
import com.pluxity.global.config.SpelExpressionEvaluator;
import com.pluxity.global.exception.CustomException;
import com.pluxity.user.entity.ResourceType;
import com.pluxity.user.entity.User;
import com.pluxity.user.service.UserService;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.ApplicationContext;
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

    private final ApplicationContext applicationContext;

    @Before("@annotation(checkPermission)")
    public void check(JoinPoint joinPoint, CheckPermission checkPermission) {
        AuthInfo result = CheckAuth();
        if (result == null) return;

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Long resourceId =
                spelEvaluator.evaluate(
                        checkPermission.resourceId(),
                        signature.getParameterNames(),
                        joinPoint.getArgs(),
                        Long.class);

        String resourceName = checkPermission.resourceName();

        if (resourceId == null) {
            throw new CustomException(NOT_FOUND_RESOURCE_ID);
        }

        if (!result.user().canAccess(resourceName, resourceId)) {
            throw new CustomException(
                    PERMISSION_DENIED, result.authentication().getName(), resourceId, resourceName);
        }
    }

    @AfterReturning(pointcut = "@annotation(checkPermissionAfter)", returning = "returnObject")
    public void checkAfter(
            JoinPoint joinPoint, CheckPermissionAfter checkPermissionAfter, Object returnObject) {

        AuthInfo result = CheckAuth();
        if (result == null) return;

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();

        Long resourceId =
                spelEvaluator.evaluate(
                        checkPermissionAfter.resourceId(),
                        new String[] {"returnObject"},
                        new Object[] {returnObject},
                        Long.class);

        String resourceName = checkPermissionAfter.resourceName();

        if (!result.user.canAccess(resourceName, resourceId)) {
            throw new CustomException(
                    PERMISSION_DENIED, result.authentication.getName(), resourceId, resourceName);
        }
    }

    @AfterReturning(pointcut = "@annotation(checkPermissionAll)", returning = "returnObject")
    public void filterCollection(
            JoinPoint joinPoint, CheckPermissionAll checkPermissionAll, Object returnObject) {
        if (!(returnObject instanceof Collection<?> collection)) {
            return;
        }

        User user = getCurrentUser();
        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"))) {
            return;
        }

        String resourceName = checkPermissionAll.resourceName();
        Iterator<?> iterator = collection.iterator();

        while (iterator.hasNext()) {
            Object item = iterator.next();
            Long itemId = getItemId(item);

            if (itemId == null) {
                continue;
            }
            if (!user.canAccess(resourceName, itemId)) {
                iterator.remove();
            }
        }
    }

    @AfterReturning(pointcut = "@annotation(check)", returning = "returnObject")
    //    @CheckPermissionCategory(categoryResourceType = ResourceType.DEVICE_CATEGORY)
    public void checkOrFilterByCategory(CheckPermissionCategory check, Object returnObject) {
        // 1. 공통 로직: 반환값이 없거나, ADMIN이면 검사하지 않음
        if (returnObject == null) {
            return;
        }
        AuthInfo authInfo = CheckAuth();
        if (authInfo == null) {
            return; // ADMIN 통과
        }

        ResourceType categoryResourceType = check.categoryResourceType();

        // 2. 반환값의 타입에 따라 로직 분기
        if (returnObject instanceof Collection<?> collection) {
            // === 목록 필터링 로직 ===
            Iterator<?> iterator = collection.iterator();
            while (iterator.hasNext()) {
                Object item = iterator.next();
                if (!hasCategoryPermission(item, categoryResourceType, authInfo.user())) {
                    iterator.remove();
                }
            }
        } else {
            // === 단일 객체 검사 로직 ===
            if (!hasCategoryPermission(returnObject, categoryResourceType, authInfo.user())) {
                throw new CustomException(
                        PERMISSION_DENIED, "Access denied based on the resource's category permission.");
            }
        }
    }

    /** [헬퍼 메서드] 주어진 객체(item)의 카테고리 권한이 있는지 확인합니다. */
    private boolean hasCategoryPermission(Object item, ResourceType categoryResourceType, User user) {
        try {
            // 리플렉션으로 getCategory() 메서드 호출
            Method getCategoryMethod = item.getClass().getMethod("getCategory");
            Object category = getCategoryMethod.invoke(item);

            if (category == null) {
                return false; // 카테고리가 없으면 접근 불가
            }

            // 리플렉션으로 category.getId() 메서드 호출
            Method getIdMethod = category.getClass().getMethod("getId");
            Long categoryId = (Long) getIdMethod.invoke(category);

            // 최종 권한 검사
            return user.canAccess(categoryResourceType.name(), categoryId);

        } catch (Exception e) {
            // 리플렉션 실패 등 예외 발생 시 안전하게 접근 거부로 처리
            return false;
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userService.findUserByUsername(username);
    }

    private Long getItemId(Object item) {
        try {
            Method getIdMethod = item.getClass().getMethod("getId");
            Object idObj = getIdMethod.invoke(item);
            if (idObj instanceof Long) {
                return (Long) idObj;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            try {
                Method idMethod = item.getClass().getMethod("id");
                Object idObj = idMethod.invoke(item);
                if (idObj instanceof Long) {
                    return (Long) idObj;
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
                return null;
            }
        }
        return null;
    }

    private AuthInfo CheckAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new CustomException(PERMISSION_DENIED);
        }

        User user = getCurrentUser();
        if (user.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN"))) {
            return null;
        }
        return new AuthInfo(authentication, user);
    }

    private record AuthInfo(Authentication authentication, User user) {}
}
