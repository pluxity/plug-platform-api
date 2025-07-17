package com.pluxity.facility.path;

import static com.pluxity.global.constant.ErrorCode.NOT_FOUND_PATH_TYPE;

import com.pluxity.global.exception.CustomException;
import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PathType {
    SUBWAY("지하철"),
    WAY("길찾기"),
    PATROL("순찰");

    private final String name;

    public static PathType from(String type) {
        return Arrays.stream(PathType.values())
                .filter(e -> e.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new CustomException(NOT_FOUND_PATH_TYPE, type));
    }
}
