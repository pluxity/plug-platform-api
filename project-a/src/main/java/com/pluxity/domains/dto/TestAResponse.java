package com.pluxity.domains.dto;

import lombok.Builder;

@Builder
public record TestAResponse(
        String name,
        String code
) {
}
