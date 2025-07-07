package com.pluxity.facility.floor.dto;

import jakarta.validation.constraints.NotBlank;

public record FloorRequest(@NotBlank String name, @NotBlank String floorId) {}
