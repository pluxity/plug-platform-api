package com.pluxity.user.controller;

import com.pluxity.user.dto.RequestUser;
import com.pluxity.user.dto.RequestUserRoles;
import com.pluxity.user.dto.ResponseUser;
import com.pluxity.user.service.UserService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService service;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<List<ResponseUser>> getUsers() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<ResponseUser> getUser(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<ResponseUser> saveUser(@Valid @RequestBody RequestUser request) {
        ResponseUser response = service.save(request);
        URI location =
                ServletUriComponentsBuilder.fromCurrentRequest().buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @PutMapping(value = "/{id}")
    public ResponseEntity<ResponseUser> updateUser(
            @PathVariable("id") Long id, @Valid @RequestBody RequestUser dto) {
        ResponseUser response = service.update(id, dto);
        URI location =
                ServletUriComponentsBuilder.fromCurrentRequest().buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/roles")
    public ResponseEntity<ResponseUser> assignRolesToUser(
            @PathVariable("userId") Long userId, @RequestBody RequestUserRoles request) {
        ResponseUser response = service.assignRolesToUser(userId, request);
        URI location =
                ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{userId}/roles")
                        .buildAndExpand(response.id())
                        .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    public ResponseEntity<Void> removeRoleFromUser(
            @PathVariable("userId") Long userId, @PathVariable("roleId") Long roleId) {
        service.removeRoleFromUser(userId, roleId);
        return ResponseEntity.noContent().build();
    }
}
