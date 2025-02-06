package com.pluxity.user.dto;

import jakarta.validation.constraints.NotBlank;

public record TemplateCreateRequest(
    @NotBlank(message = "Template name is required")
    String name,
    
    @NotBlank(message = "Template URL is required")
    String url
) {} 