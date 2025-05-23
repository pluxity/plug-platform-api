package com.pluxity.facility.strategy;

import com.pluxity.facility.facility.Facility;
import com.pluxity.facility.location.Location;
import com.pluxity.facility.location.LocationRepository;
import com.pluxity.facility.location.dto.LocationRequest;
import com.pluxity.facility.location.dto.LocationResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LocationStrategy implements FacilityStrategy<LocationRequest, LocationResponse> {

    private final LocationRepository repository;

    @Override
    public <T extends Facility> void save(T facility, LocationRequest data) {
        repository.save(toEntity(facility, data));
    }

    @Override
    public <T extends Facility> LocationResponse findByFacility(T facility) {
        return null;
    }

    @Override
    public <T extends Facility> List<LocationResponse> findAllByFacility(T facility) {
        return repository.findAllByFacility(facility).stream().map(LocationResponse::from).toList();
    }

    @Override
    public <T extends Facility> void update(T facility, LocationRequest data) {
        repository
                .findById(facility.getId())
                .ifPresent(
                        location -> {
                            location.update(data);
                            repository.save(location);
                        });
    }

    @Override
    public <T extends Facility> void delete(T facility) {
        repository.deleteByFacility(facility);
    }

    private Location toEntity(Facility facility, LocationRequest request) {
        return Location.builder()
                .facility(facility)
                .longitude(request.longitude())
                .latitude(request.latitude())
                .altitude(request.altitude())
                .build();
    }
}
