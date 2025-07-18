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
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
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
@Slf4j
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

    @Around("@annotation(checkPermissionCategory)")
    public Object checkOrFilterByCategory(
            ProceedingJoinPoint pjp, CheckPermissionCategory checkPermissionCategory) throws Throwable {
        Object returnObject = pjp.proceed();

        if (returnObject == null) {
            return null;
        }

        AuthInfo authInfo = CheckAuth();
        if (authInfo == null) {
            log.debug("ADMIN user detected. Skipping permission check and returning original object.");
            return returnObject; // ADMIN 통과
        }

        ResourceType categoryResourceType = checkPermissionCategory.categoryResourceType();

        if (returnObject instanceof Collection<?> collection) {
            // === 목록 필터링 로직 ===

            // 필터링하여 새로운 리스트를 생성합니다. (원본을 수정하지 않음)
            List<?> filteredList =
                    collection.stream()
                            .filter(
                                    item -> hasDirectCategoryPermission(item, categoryResourceType, authInfo.user()))
                            .collect(Collectors.toList());

            log.debug(
                    "Finished filtering. Original size: {}, Filtered size: {}. Returning filtered list.",
                    collection.size(),
                    filteredList.size());

            // 4. 필터링된 '새로운' 리스트를 최종 반환값으로 리턴합니다.
            return filteredList;

        } else {
            // === 단일 객체 검사 로직 ===
            if (!hasDirectCategoryPermission(returnObject, categoryResourceType, authInfo.user())) {
                throw new CustomException(
                        PERMISSION_DENIED,
                        "Access denied. User lacks direct permission for the resource's category.");
            }

            // 권한이 있으면 원본 객체를 그대로 반환
            return returnObject;
        }
    }

    private boolean hasDirectCategoryPermission(
            Object item, ResourceType categoryResourceType, User user) {
        try {
            Object category;
            // item이 리소스(Device 등)인지, 카테고리 자체인지 확인
            if (item.getClass().getSimpleName().endsWith("Category")) {
                category = item;
            } else {
                category = item.getClass().getMethod("getCategory").invoke(item);
            }

            if (category == null) {
                return false; // 카테고리가 없으면 접근 불가
            }

            Long categoryId = (Long) category.getClass().getMethod("getId").invoke(category);

            boolean hasAccess = user.canAccess(categoryResourceType.getResourceName(), categoryId);

            return hasAccess;

        } catch (Exception e) {
            log.error(
                    "Error during direct category permission check for item: {}", getNodeIdentifier(item), e);
            return false;
        }
    }

    // 로그용 헬퍼 메서드
    private String getNodeIdentifier(Object node) {
        if (node == null) return "null";
        try {
            Long id = (Long) node.getClass().getMethod("getId").invoke(node);
            String name = (String) node.getClass().getMethod("getName").invoke(node);
            return String.format("%s(id=%d, name=%s)", node.getClass().getSimpleName(), id, name);
        } catch (Exception e) {
            return node.toString();
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
