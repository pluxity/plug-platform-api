package com.pluxity.facility.strategy;

import com.pluxity.facility.Facility;
import com.pluxity.facility.floor.Floor;
import com.pluxity.facility.floor.FloorRepository;
import com.pluxity.facility.floor.dto.FloorRequest;
import com.pluxity.facility.floor.dto.FloorResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class FloorService {

    private final FloorRepository repository;

    @Transactional
    public <T extends Facility> void save(T facility, List<FloorRequest> floorRequests) {
        if (CollectionUtils.isEmpty(floorRequests)) {
            return;
        }

        List<Floor> floors =
                floorRequests.stream().map(request -> toEntity(facility, request)).toList();

        repository.saveAll(floors);
    }

    @Transactional
    public <T extends Facility> void update(T facility, List<FloorRequest> floorRequests) {
        repository.deleteByFacility(facility);

        save(facility, floorRequests);
    }

    @Transactional(readOnly = true)
    public <T extends Facility> List<FloorResponse> findAllByFacility(T facility) {
        return repository.findAllByFacility(facility).stream().map(FloorResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public <T extends Facility> Map<Facility, List<Floor>> findAllByFacilities(List<T> facilities) {
        if (CollectionUtils.isEmpty(facilities)) {
            return Collections.emptyMap();
        }
        return repository.findAllByFacilities(facilities).stream()
                .collect(Collectors.groupingBy(Floor::getFacility));
    }

    @Transactional
    public <T extends Facility> void delete(T facility) {
        repository.deleteByFacility(facility);
    }

    private Floor toEntity(Facility facility, FloorRequest request) {
        return Floor.builder()
                .facility(facility)
                .floorId(request.floorId())
                .name(request.name())
                .build();
    }
}
