package com.pluxity.user.controller;

import com.pluxity.user.dto.PermissionCreateRequest;
import com.pluxity.user.dto.PermissionResponse;
import com.pluxity.user.dto.PermissionUpdateRequest;
import com.pluxity.user.service.PermissionService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    public ResponseEntity<Void> createPermission(
            @Valid @RequestBody PermissionCreateRequest request) {
        Long permissionId = permissionService.create(request);
        URI location =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(permissionId)
                        .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping
    public ResponseEntity<List<PermissionResponse>> getPermissions() {
        return ResponseEntity.ok(permissionService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PermissionResponse> getPermission(@PathVariable Long id) {
        return ResponseEntity.ok(PermissionResponse.from(permissionService.findById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updatePermission(
            @PathVariable Long id, @Valid @RequestBody PermissionUpdateRequest request) {
        permissionService.update(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePermission(@PathVariable Long id) {
        permissionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
