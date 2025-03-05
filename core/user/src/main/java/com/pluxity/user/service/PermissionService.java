package com.pluxity.user.service;

import com.pluxity.user.dto.PermissionCreateRequest;
import com.pluxity.user.dto.PermissionResponse;
import com.pluxity.user.dto.PermissionUpdateRequest;

import java.util.List;

public interface PermissionService {

    List<PermissionResponse> findAll();

    PermissionResponse findById(Long id);

    PermissionResponse save(PermissionCreateRequest request);

    PermissionResponse update(Long id, PermissionUpdateRequest request);

    void delete(Long id);
}
