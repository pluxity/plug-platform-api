package com.pluxity.user.controller;

import com.pluxity.global.annotation.ResponseCreated;
import com.pluxity.global.response.CreatedResponseBody;
import com.pluxity.global.response.DataResponseBody;
import com.pluxity.global.response.ResponseBody;
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
    public ResponseEntity<DataResponseBody<List<UserResponse>>> getUsers() {
        return ResponseEntity.ok(DataResponseBody.of(service.findAll()));
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<DataResponseBody<UserResponse>> getUser(@PathVariable("id") Long id) {
        return ResponseEntity.ok(DataResponseBody.of(service.findById(id)));
    }

    @PostMapping
    @ResponseCreated
    public ResponseEntity<CreatedResponseBody<Long>> saveUser(@Valid @RequestBody UserCreateRequest request) {
        return ResponseEntity.ok(CreatedResponseBody.of(service.save(request).id()));
    }

    @PostMapping("/{userId}/roles")
    @ResponseCreated
    public ResponseEntity<CreatedResponseBody<Long>> assignRolesToUser(
            @PathVariable("userId") Long userId,
            @RequestBody UserRoleAssignRequest request) {
        return ResponseEntity.ok(CreatedResponseBody.of(service.assignRolesToUser(userId, request).id()));
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<ResponseBody> updateUser(
            @PathVariable("id") Long id, @RequestBody UserUpdateRequest dto) {
        service.update(id, dto);
        return ResponseEntity.ok(ResponseBody.of());
    }

    @PutMapping(value = "/{id}/password")
    public ResponseEntity<ResponseBody> updatePassword(
            @PathVariable("id") Long id, @Valid @RequestBody UserPasswordUpdateRequest dto) {
        service.updateUserPassword(id, dto);
        return ResponseEntity.ok(ResponseBody.of());
    }

    @PutMapping(value = "/{id}/roles")
    public ResponseEntity<ResponseBody> updateRoles(
            @PathVariable("id") Long id, @Valid @RequestBody UserRoleUpdateRequest dto) {
        service.updateUserRoles(id, dto);
        return ResponseEntity.ok(ResponseBody.of());
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<Void> removeRoleFromUser(
            @PathVariable("userId") Long userId,
            @PathVariable("roleId") Long roleId) {
        service.removeRoleFromUser(userId, roleId);
        return ResponseEntity.noContent().build();
    }
}
