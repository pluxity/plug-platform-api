package com.pluxity.facility.station;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Added import
import org.springframework.data.repository.query.Param; // Added import
import org.springframework.stereotype.Repository;

@Repository
public interface StationRepository extends JpaRepository<Station, Long> {
    @Query("SELECT s FROM Station s JOIN s.facility f WHERE f.code = :stationCode")
    Optional<Station> findByCode(@Param("stationCode") String stationCode);
}
