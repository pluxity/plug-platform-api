package com.pluxity.domains.acl.service; // acl.util 또는 common 패키지가 더 적합할 수 있음

import org.springframework.security.acls.domain.BasePermission;
import org.springframework.security.acls.model.Permission;

/** 문자열 권한 표현식을 Spring Security ACL의 Permission 객체로 변환하는 유틸리티 클래스 */
public final class PermissionMapper {

    private PermissionMapper() {}

    /** 문자열 권한 표현식을 Permission 객체로 변환 */
    public static Permission fromString(String permission) {
        if (permission == null || permission.isEmpty()) {
            throw new IllegalArgumentException("Permission cannot be null or empty");
        }

        return switch (permission.toUpperCase()) {
            case "READ" -> BasePermission.READ;
            case "WRITE" -> BasePermission.WRITE;
            case "CREATE" -> BasePermission.CREATE;
            case "DELETE" -> BasePermission.DELETE;
            case "ADMIN", "ADMINISTRATION" -> BasePermission.ADMINISTRATION;
            default -> throw new IllegalArgumentException("Unsupported permission: " + permission);
        };
    }
}
