package com.pluxity.building.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BuildingResponseDto {
    private String name;
    private String code;
    private String address;

    public BuildingResponseDto(String name, String code, String address) {
        this.name = name;
        this.code = code;
        this.address = address;
    }
}
