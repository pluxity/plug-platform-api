package com.pluxity.user.controller;

import com.pluxity.global.annotation.ResponseCreated;
import com.pluxity.global.response.DataResponseBody;
import com.pluxity.global.response.ResponseBody;
import com.pluxity.user.dto.*;
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
    public ResponseEntity<DataResponseBody<RoleResponse>> getRole(@PathVariable(name = "id") Long id) {
        return ResponseEntity.ok(DataResponseBody.of(roleService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<DataResponseBody<List<RoleResponse>>> getAllRoles() {
        return ResponseEntity.ok(DataResponseBody.of(roleService.findAll()));
    }

    @PostMapping
    @ResponseCreated
    public ResponseEntity<Long> createRole(@Valid @RequestBody RoleCreateRequest request) {
        return ResponseEntity.ok(roleService.save(request).id());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateRole(
            @PathVariable(name = "id") Long id, @Valid @RequestBody RoleUpdateRequest request) {
        roleService.update(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable(name = "id") Long id) {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
