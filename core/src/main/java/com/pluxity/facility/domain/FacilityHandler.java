package com.pluxity.facility.domain;

import com.pluxity.facility.entity.Facility;
import com.pluxity.facility.strategy.FacilityStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FacilityHandler<REQ, RES> {
    
    private final Map<FacilityType, FacilityStrategy<REQ, RES>> strategies = new EnumMap<>(FacilityType.class);
    
    public void registerStrategy(FacilityType type, FacilityStrategy<REQ, RES> strategy) {
        strategies.put(type, strategy);
    }
    
    private FacilityStrategy<REQ, RES> getStrategy(Facility facility) {
        return Optional.ofNullable(strategies.get(facility.getFacilityType()))
                .orElseThrow(() -> new IllegalArgumentException("지원하지 않는 시설 타입: " + facility.getFacilityType()));
    }
    
    public void save(Facility facility, REQ data) {
        getStrategy(facility).save(facility, data);
    }
    
    public List<RES> findAllByFacility(Facility facility) {
        return getStrategy(facility).findAllByFacility(facility);
    }
    
    public void update(Facility facility, REQ data) {
        getStrategy(facility).update(facility, data);
    }
    
    public void delete(Facility facility) {
        getStrategy(facility).delete(facility);
    }
} 