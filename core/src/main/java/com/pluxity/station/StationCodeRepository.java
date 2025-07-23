package com.pluxity.station;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StationCodeRepository extends JpaRepository<StationCode, Long> {
    List<StationCode> findByStationOrderByCreatedAtDesc(Station station);

    void deleteByStation(Station station);

    List<StationCode> findByStationInOrderByCreatedAtDesc(List<Station> stationIds);
}
