package com.pluxity;

import lombok.Builder;

@Builder
public record TestBResponse (
        String name,
        String description
) {
}
