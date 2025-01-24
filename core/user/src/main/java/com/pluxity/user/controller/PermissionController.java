package com.pluxity.user.controller;

import com.pluxity.user.dto.RequestPermission;
import com.pluxity.user.dto.ResponsePermission;
import com.pluxity.user.service.PermissionService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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
    public ResponseEntity<ResponsePermission> createPermission(
            @Valid @RequestBody RequestPermission request) {
        ResponsePermission response = permissionService.save(request);
        URI location =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}")
                        .buildAndExpand(response.id())
                        .toUri();

        return ResponseEntity.created(location).body(response);
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
