package com.pluxity.facility.strategy;

import com.pluxity.facility.dto.FloorRequest;
import com.pluxity.facility.dto.FloorResponse;
import com.pluxity.facility.entity.Facility;
import com.pluxity.facility.entity.Floor;
import com.pluxity.facility.repository.FloorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FloorStrategy implements FacilityStrategy<FloorRequest, FloorResponse> {

    private final FloorRepository repository;

    @Override
    public void save(Facility facility, FloorRequest data) {
        repository.save(toEntity(facility, data));
    }

    @Override
    public List<FloorResponse> findAllByFacility(Facility facility) {
        return repository.findAllByFacility(facility)
                .stream()
                .map(FloorResponse::from)
                .toList();
    }

    @Override
    public void update(Facility facility, FloorRequest data) {
    }

    @Override
    public void delete(Facility facility) {
        // 시설에 연결된 모든 층 삭제
        List<Floor> floors = repository.findAllByFacility(facility);
        if (!floors.isEmpty()) {
            repository.deleteAll(floors);
        }
    }

    private Floor toEntity(Facility facility, FloorRequest request) {
        return Floor.builder()
                .facility(facility)
                .groupId(request.groupId())
                .name(request.name())
                .build();
    }
}
