package com.pluxity.user.controller;

import com.pluxity.user.dto.RequestUser;
import com.pluxity.user.dto.ResponseUser;
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
    public ResponseEntity<ResponseUser> getUser(Authentication authentication) {
        var username = authentication.getName();
        return ResponseEntity.ok(service.findByUsername(username));
    }

    @PutMapping("/me")
    public ResponseEntity<ResponseUser> updateUser(Authentication authentication, @RequestBody RequestUser dto) {
        Long id = service.findByUsername(authentication.getName()).id();
        return ResponseEntity.ok(service.update(id, dto));
    }
}
