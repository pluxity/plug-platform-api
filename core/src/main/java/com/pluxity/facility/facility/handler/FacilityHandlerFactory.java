package com.pluxity.facility.facility.handler;

import com.pluxity.facility.facility.FacilityType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class FacilityHandlerFactory {

    private final Map<FacilityType, FacilityHandler> handlerMap;

    public FacilityHandlerFactory(List<FacilityHandler> handlers) {
        this.handlerMap = handlers.stream()
            .collect(Collectors.toUnmodifiableMap(FacilityHandler::getType, Function.identity()));
    }

    public FacilityHandler getHandler(FacilityType facilityType) {
        return Optional.ofNullable(handlerMap.get(facilityType))
            .orElseThrow(() -> new IllegalArgumentException("Unsupported facility type: " + facilityType));
    }
} 