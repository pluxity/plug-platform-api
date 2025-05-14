package com.pluxity.facility.strategy;

import com.pluxity.facility.dto.FloorRequest;
import com.pluxity.facility.dto.FloorResponse;
import com.pluxity.facility.entity.Facility;
import com.pluxity.facility.entity.Floor;
import com.pluxity.facility.repository.FloorRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FloorStrategy implements FacilityStrategy<FloorRequest, FloorResponse> {

    private final FloorRepository repository;

    @Override
    public <T extends Facility> void save(T facility, FloorRequest data) {
        repository.save(toEntity(facility, data));
    }

    @Override
    public <T extends Facility> FloorResponse findByFacility(T facility) {
        return null;
    }

    @Override
    public <T extends Facility> List<FloorResponse> findAllByFacility(T facility) {
        return repository.findAllByFacility(facility).stream().map(FloorResponse::from).toList();
    }

    public <T extends Facility> Map<Facility, List<Floor>> findAllByFacilities(List<T> facilities) {
        return repository.findAllByFacilities(facilities).stream()
                .collect(Collectors.groupingBy(Floor::getFacility));
    }

    @Override
    public <T extends Facility> void update(T facility, FloorRequest data) {}

    @Override
    public <T extends Facility> void delete(T facility) {
        repository.deleteByFacility(facility);
    }

    private Floor toEntity(Facility facility, FloorRequest request) {
        return Floor.builder()
                .facility(facility)
                .groupId(request.groupId())
                .name(request.name())
                .build();
    }
}
