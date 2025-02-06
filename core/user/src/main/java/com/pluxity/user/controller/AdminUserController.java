package com.pluxity.user.controller;

import com.pluxity.global.annotation.ResponseCreated;
import com.pluxity.user.dto.*;
import com.pluxity.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService service;

    @GetMapping
    public ResponseEntity<List<UserResponse>> getUsers() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<UserResponse> getUser(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @ResponseCreated
    public ResponseEntity<Long> saveUser(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(service.save(request).id());
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable("id") Long id, @RequestBody UserUpdateRequest dto) {
        UserResponse response = service.update(id, dto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/roles")
    @ResponseCreated
    public ResponseEntity<Long> assignRolesToUser(
            @PathVariable("userId") Long userId, @RequestBody UserRoleAssignRequest request) {
        return ResponseEntity.ok(service.assignRolesToUser(userId, request).id());
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<Void> removeRoleFromUser(
            @PathVariable("userId") Long userId, @PathVariable("roleId") Long roleId) {
        service.removeRoleFromUser(userId, roleId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/template/{templateId}")
    @ResponseCreated
    public ResponseEntity<Long> assignTemplateToUser(
            @PathVariable("userId") Long userId,
            @PathVariable("templateId") Long templateId) {
        return ResponseEntity.ok(service.assignTemplateToUser(userId, templateId).id());
    }

    @GetMapping("/{userId}/template")
    public ResponseEntity<TemplateResponse> getUserTemplate(
            @PathVariable("userId") Long userId) {
        return ResponseEntity.ok(service.getUserTemplate(userId));
    }

    @DeleteMapping("/{userId}/template")
    public ResponseEntity<Void> removeUserTemplate(
            @PathVariable("userId") Long userId) {
        service.removeUserTemplate(userId);
        return ResponseEntity.noContent().build();
    }

}
