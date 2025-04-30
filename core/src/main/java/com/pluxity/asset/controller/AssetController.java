package com.pluxity.asset.controller;

import com.pluxity.asset.dto.AssetCreateRequest;
import com.pluxity.asset.dto.AssetResponse;
import com.pluxity.asset.dto.AssetUpdateRequest;
import com.pluxity.asset.service.AssetService;
import com.pluxity.global.annotation.ResponseCreated;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/assets")
@RestController
@RequiredArgsConstructor
public class AssetController {

    private final AssetService service;

    @GetMapping
    public ResponseEntity<List<AssetResponse>> getAssets() {
        return ResponseEntity.ok(service.getAssets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssetResponse> getAsset(@PathVariable Long id) {
        return ResponseEntity.ok(service.getAsset(id));
    }

    @PostMapping
    @ResponseCreated
    public void createAsset(@RequestBody AssetCreateRequest request) {
        service.createAsset(request);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Void> updateAsset(@PathVariable Long id, @RequestBody AssetUpdateRequest request) {
        service.updateAsset(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAsset(@PathVariable Long id) {
        service.deleteAsset(id);
        return ResponseEntity.noContent().build();
    }
}
