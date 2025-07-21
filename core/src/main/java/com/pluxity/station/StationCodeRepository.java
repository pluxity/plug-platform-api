package com.pluxity.station;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StationCodeRepository extends JpaRepository<StationCode, Long> {
    List<StationCode> findByStationIdOrderByCreatedAtDesc(Long stationId);

    void deleteByStationId(Long stationId);
}
