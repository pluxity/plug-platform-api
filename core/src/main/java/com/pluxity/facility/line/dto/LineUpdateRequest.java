package com.pluxity.facility.line.dto;

import jakarta.validation.constraints.Size;

// Fields are optional for update. Service layer will handle partial updates.
public record LineUpdateRequest(
    @Size(max = 50, message = "Line name must be less than or equal to 50 characters.")
    String name, // Can be null if not updating name

    @Size(max = 255, message = "Color string must be less than or equal to 255 characters.")
    String color // Can be null if not updating color
) {}
