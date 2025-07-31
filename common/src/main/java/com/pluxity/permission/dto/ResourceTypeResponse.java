package com.pluxity.permission.dto;

import com.pluxity.permission.ResourceType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "권한 설정 가능 리소스 타입 정보")
public record ResourceTypeResponse(
        @Schema(description = "리소스 타입의 고유 키 (Enum 상수 이름)", example = "FACILITY") String key,
        @Schema(description = "리소스 타입의 한글 이름", example = "시설") String name,
        @Schema(description = "관련 API 엔드포인트 경로", example = "facilities") String endpoint) {
    public static ResourceTypeResponse from(ResourceType resourceType) {
        return new ResourceTypeResponse(
                resourceType.name(), resourceType.getResourceName(), resourceType.getEndpoint());
    }
}
