package com.pluxity.station;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StationLineRepository extends JpaRepository<StationLine, Long> {
    List<StationLine> findByStationOrderByCreatedAtDesc(Station station);

    void deleteByStation(Station station);

    List<StationLine> findByStationInOrderByCreatedAtDesc(List<Station> stations);

    boolean existsByStationAndLine(Station station, Line line);

    void deleteByStationAndLine(Station station, Line line);
}
