package com.pluxity.user.controller;

import com.pluxity.user.annotation.ResponseCreated;
import com.pluxity.user.dto.RequestUser;
import com.pluxity.user.dto.RequestUserRoles;
import com.pluxity.user.dto.ResponseUser;
import com.pluxity.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService service;

    @GetMapping
    public ResponseEntity<List<ResponseUser>> getUsers() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<ResponseUser> getUser(@PathVariable("id") Long id) {
        return ResponseEntity.ok(service.findById(id));
    }


    @PostMapping
    @ResponseCreated
    public ResponseEntity<Long> saveUser(@Valid @RequestBody RequestUser request) {
        return ResponseEntity.ok(service.save(request).id());
    }


    @PutMapping(value = "/{id}")
    public ResponseEntity<ResponseUser> updateUser(@PathVariable("id") Long id, @RequestBody RequestUser dto) {
        ResponseUser response = service.update(id, dto);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/{userId}/roles")
    public ResponseEntity<ResponseUser> assignRolesToUser(
            @PathVariable("userId") Long userId,
            @RequestBody RequestUserRoles request) {
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
            @PathVariable("userId") Long userId,
            @PathVariable("roleId") Long roleId) {
        service.removeRoleFromUser(userId, roleId);
        return ResponseEntity.noContent().build();
    }
}
