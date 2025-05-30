package com.pluxity.domains.device.repository;

import com.pluxity.domains.device.entity.Nflux;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NfluxRepository extends JpaRepository<Nflux, Long> {
    List<Nflux> findByCategoryId(Long categoryId);

    @EntityGraph(attributePaths = {"feature", "feature.facility"})
    @Query(""" 
            SELECT n FROM Nflux n
                        JOIN n.feature f JOIN f.facility fac
                                    WHERE fac.id = :stationId AND TYPE(fac) = com.pluxity.facility.station.Station""")
    List<Nflux> findByStationId(@Param("stationId") Long stationId);
}
