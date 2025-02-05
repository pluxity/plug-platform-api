package com.pluxity.user.controller;

import com.pluxity.user.annotation.ResponseCreated;
import com.pluxity.user.dto.RequestRole;
import com.pluxity.user.dto.RequestRolePermissions;
import com.pluxity.user.dto.ResponsePermission;
import com.pluxity.user.dto.ResponseRole;
import com.pluxity.user.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/{id}")
    public ResponseEntity<ResponseRole> getRole(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(roleService.findById(id));
    }

    @GetMapping
    public ResponseEntity<List<ResponseRole>> getAllRoles() {
        return ResponseEntity.ok(roleService.findAll());
    }

    @GetMapping("/{roleId}/permissions")
    public ResponseEntity<List<ResponsePermission>> getRolePermissions(
            @PathVariable(name = "roleId") Long roleId) {
        return ResponseEntity.ok(roleService.getRolePermissions(roleId));
    }

    @PostMapping
    @ResponseCreated
    public ResponseEntity<Long> createRole(@Valid @RequestBody RequestRole request) {
        return ResponseEntity.ok(roleService.save(request).id());
    }

    @PostMapping("/{roleId}/permissions")
    @ResponseCreated
    public ResponseEntity<Long> assignPermissionsToRole(
            @PathVariable(name = "roleId") Long roleId,
            @Valid @RequestBody RequestRolePermissions request) {
        return ResponseEntity.ok(roleService.assignPermissionsToRole(roleId, request).id());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseRole> updateRole(
            @PathVariable(name = "id") Long id, @Valid @RequestBody RequestRole request) {
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
