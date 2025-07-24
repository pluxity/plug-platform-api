package com.pluxity.station;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StationLineRepository extends JpaRepository<StationLine, Long> {
    List<StationLine> findByStationOrderByCreatedAtDesc(Station station);

    void deleteByStation(Station station);

    List<StationLine> findByStationInOrderByCreatedAtDesc(List<Station> stations);

    boolean existsByStationAndLine(Station station, Line line);

    Optional<StationLine> findByStationAndLine(Station station, Line line);
}
