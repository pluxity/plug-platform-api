package com.pluxity.building.controller;

import com.pluxity.authentication.security.CustomUserDetails;
import com.pluxity.building.dto.BuildingPermissionCreateRequest;
import com.pluxity.building.dto.BuildingPermissionResponse;
import com.pluxity.building.service.BuildingPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/building-permissions")
@RequiredArgsConstructor
public class BuildingPermissionController {

    private final BuildingPermissionService buildingPermissionService;

    @GetMapping
    public ResponseEntity<List<BuildingPermissionResponse>> getMyPermissions(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(buildingPermissionService.findByUser(userDetails.user().getId()));
    }

    @GetMapping("/check/{buildingId}")
    public ResponseEntity<Boolean> checkPermission(
            @PathVariable Long buildingId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(buildingPermissionService.hasPermission(userDetails.user().getId(), buildingId));
    }

    @PostMapping
    public ResponseEntity<BuildingPermissionResponse> create(@RequestBody BuildingPermissionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(buildingPermissionService.create(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        buildingPermissionService.delete(id);
        return ResponseEntity.noContent().build();
    }
} 