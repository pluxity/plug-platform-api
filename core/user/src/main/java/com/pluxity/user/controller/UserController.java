package com.pluxity.user.controller;

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
    public ResponseEntity<UserResponse> getUser(Authentication authentication) {
        var username = authentication.getName();
        return ResponseEntity.ok(service.findByUsername(username));
    }

    @PutMapping("/me")
    public ResponseEntity<UserResponse> updateUser(
            Authentication authentication, @RequestBody UserUpdateRequest dto) {
        Long id = service.findByUsername(authentication.getName()).id();
        return ResponseEntity.ok(service.update(id, dto));
    }
}
