package com.pluxity.user.dto;

import com.pluxity.user.entity.User;
import java.util.List;

public record ResponseUser(
        Long id, String username, String name, String code, List<ResponseRole> roles) {
    public static ResponseUser from(User user) {
        return new ResponseUser(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getCode(),
                user.getRoles().stream().map(ResponseRole::from).toList());
    }
}
