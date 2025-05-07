package com.pluxity.facility.strategy;

import com.pluxity.facility.dto.LocationRequest;
import com.pluxity.facility.dto.LocationResponse;
import com.pluxity.facility.entity.Facility;
import com.pluxity.facility.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationStrategy implements FacilityStrategy<LocationRequest, LocationResponse> {

    private final LocationRepository repository;

    @Override
    public void save(Facility facility, LocationRequest data) {

    }

    @Override
    public List<LocationResponse> findAllByFacility(Facility facility) {
        return List.of();
    }

    @Override
    public void update(Facility facility, LocationRequest data) {

    }

    @Override
    public void delete(Facility facility) {

    }
}
