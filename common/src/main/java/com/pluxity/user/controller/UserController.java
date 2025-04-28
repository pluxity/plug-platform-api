package com.pluxity.user.controller;

import com.pluxity.global.response.DataResponseBody;
import com.pluxity.global.response.ResponseBody;
import com.pluxity.user.dto.UserResponse;
import com.pluxity.user.dto.UserUpdateRequest;
import com.pluxity.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    @GetMapping("/me")
    public ResponseEntity<DataResponseBody<UserResponse>> getUser(Authentication authentication) {
        var username = authentication.getName();
        return ResponseEntity.ok(DataResponseBody.of(service.findByUsername(username)));
    }

    @PutMapping("/me")
    public ResponseEntity<Void> updateUser(
            Authentication authentication, @RequestBody UserUpdateRequest dto) {
        Long id = service.findByUsername(authentication.getName()).id();
        service.update(id, dto);
        return ResponseEntity.noContent().build();
    }
}
