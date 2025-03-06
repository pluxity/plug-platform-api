package com.pluxity.user.dto;

import java.util.List;

public record UserRoleUpdateRequest(List<Long> roleIds) {}
