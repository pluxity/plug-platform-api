package com.pluxity.user.controller;

import com.pluxity.global.annotation.ResponseCreated;
import com.pluxity.global.response.CreatedResponseBody;
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
    public ResponseEntity<CreatedResponseBody<Long>> saveUser(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(CreatedResponseBody.of(service.save(request).id()));
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable("id") Long id, @RequestBody UserUpdateRequest dto) {
        UserResponse response = service.update(id, dto);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{id}/password")
    public ResponseEntity<UserResponse> updatePassword(
            @PathVariable("id") Long id, @Valid @RequestBody UserPasswordUpdateRequest dto) {
        return ResponseEntity.ok(service.updateUserPassword(id, dto));
    }

    @PutMapping(value = "/{id}/roles")
    public ResponseEntity<UserResponse> updateRoles(
            @PathVariable("id") Long id, @Valid @RequestBody UserRoleUpdateRequest dto) {
        return ResponseEntity.ok(service.updateUserRoles(id, dto));
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/roles")
    @ResponseCreated
    public ResponseEntity<CreatedResponseBody<Long>> assignRolesToUser(
            @PathVariable("userId") Long userId,
            @RequestBody UserRoleAssignRequest request) {
        return ResponseEntity.ok(CreatedResponseBody.of(service.assignRolesToUser(userId, request).id()));
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<Void> removeRoleFromUser(
            @PathVariable("userId") Long userId,
            @PathVariable("roleId") Long roleId) {
        service.removeRoleFromUser(userId, roleId);
        return ResponseEntity.noContent().build();
    }
}
