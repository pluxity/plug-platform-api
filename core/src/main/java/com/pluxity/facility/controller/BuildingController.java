package com.pluxity.facility.controller;

import com.pluxity.facility.dto.*;
import com.pluxity.facility.service.BuildingService;
import com.pluxity.global.annotation.ResponseCreated;
import com.pluxity.global.response.DataResponseBody;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService service;

    @PostMapping
    @ResponseCreated
    public ResponseEntity<Long> create(
            @Valid @RequestBody BuildingCreateRequest request) {

        Long id = service.save(request);

        return ResponseEntity.ok(id);
    }

    @GetMapping
    public ResponseEntity<DataResponseBody<List<BuildingResponse>>> getAll() {
        return ResponseEntity.ok(DataResponseBody.of(service.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DataResponseBody<BuildingResponse>> get(
            @PathVariable Long id) {
        return ResponseEntity.ok(DataResponseBody.of(service.findById(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> patch(@PathVariable Long id, @Valid @RequestBody BuildingUpdateRequest request) {
        service.update(id, request);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/drawings")
    public ResponseEntity<Void> patchDrawing(@PathVariable Long id) {
//        service.updateDrawing(id, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }


}
