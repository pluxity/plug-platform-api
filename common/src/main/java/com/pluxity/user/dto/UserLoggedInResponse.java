package com.pluxity.user.dto;

import java.util.List;

public record UserLoggedInResponse(
        Long id,
        String username,
        String name,
        String code,
        String phoneNumber,
        String department,
        Boolean isLoggedIn,
        List<RoleResponse> roles) {
    public static UserLoggedInResponse from(
            Long id,
            String username,
            String name,
            String code,
            String phoneNumber,
            String department,
            Boolean isLoggedIn,
            List<RoleResponse> roles) {
        return new UserLoggedInResponse(
                id, username, name, code, phoneNumber, department, isLoggedIn, roles);
    }
}
