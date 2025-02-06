package com.pluxity.user.controller;

import com.pluxity.global.annotation.ResponseCreated;
import com.pluxity.user.dto.*;
import com.pluxity.user.service.RoleService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/{id}")
    public ResponseEntity<RoleResponse> getRole(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(roleService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        return ResponseEntity.ok(roleService.findAll());
    }

    @GetMapping("/{roleId}/permissions")
    public ResponseEntity<List<PermissionResponse>> getRolePermissions(
            @PathVariable(name = "roleId") Long roleId) {
        return ResponseEntity.ok(roleService.getRolePermissions(roleId));
    }

    @PostMapping
    @ResponseCreated
    public ResponseEntity<Long> createRole(@Valid @RequestBody RoleCreateRequest request) {
        return ResponseEntity.ok(roleService.save(request).id());
    }

    @PostMapping("/{roleId}/permissions")
    @ResponseCreated
    public ResponseEntity<Long> assignPermissionsToRole(
            @PathVariable(name = "roleId") Long roleId,
            @Valid @RequestBody RolePermissionAssignRequest request) {
        return ResponseEntity.ok(roleService.assignPermissionsToRole(roleId, request).id());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleResponse> updateRole(
            @PathVariable(name = "id") Long id, @Valid @RequestBody RoleUpdateRequest request) {
        return ResponseEntity.ok(roleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable(name = "id") Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    public ResponseEntity<Void> removePermissionFromRole(
            @PathVariable(name = "roleId") Long roleId,
            @PathVariable(name = "permissionId") Long permissionId) {
        roleService.removePermissionFromRole(roleId, permissionId);
        return ResponseEntity.noContent().build();
    }
}
