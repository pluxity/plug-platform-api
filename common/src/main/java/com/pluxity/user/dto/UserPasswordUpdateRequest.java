package com.pluxity.user.dto;

public record UserPasswordUpdateRequest(String currentPassword, String newPassword) {}
