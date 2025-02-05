package com.pluxity.user.controller;

import com.pluxity.user.annotation.ResponseCreated;
import com.pluxity.user.dto.RequestPermission;
import com.pluxity.user.dto.ResponsePermission;
import com.pluxity.user.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    public ResponseEntity<List<ResponsePermission>> getAllPermissions() {
        return ResponseEntity.ok(permissionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponsePermission> getPermission(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(permissionService.findById(id));
    }

    @PostMapping
    @ResponseCreated
    public ResponseEntity<Long> createPermission(
            @Valid @RequestBody RequestPermission request) {
        ResponsePermission response = permissionService.save(request);
        return ResponseEntity.ok(response.id());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponsePermission> updatePermission(
            @PathVariable(name = "id") Long id, @Valid @RequestBody RequestPermission request) {
        return ResponseEntity.ok(permissionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable(name = "id") Long id) {
        permissionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
