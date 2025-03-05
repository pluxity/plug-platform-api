package com.pluxity.user.controller;

import com.pluxity.global.annotation.ResponseCreated;
import com.pluxity.user.dto.PermissionCreateRequest;
import com.pluxity.user.dto.PermissionResponse;
import com.pluxity.user.dto.PermissionUpdateRequest;
import com.pluxity.user.service.PermissionServiceImpl;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionServiceImpl permissionService;

    @GetMapping
    public ResponseEntity<List<PermissionResponse>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PermissionResponse> getPermission(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(permissionService.findById(id));
    }

    @PostMapping
    @ResponseCreated
    public ResponseEntity<Long> createPermission(
            @Valid @RequestBody PermissionCreateRequest request) {
        PermissionResponse response = permissionService.save(request);
        return ResponseEntity.ok(response.id());
    }

    @PutMapping("/{id}")
    public ResponseEntity<PermissionResponse> updatePermission(
            @PathVariable(name = "id") Long id, @Valid @RequestBody PermissionUpdateRequest request) {
        return ResponseEntity.ok(permissionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable(name = "id") Long id) {
        permissionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
