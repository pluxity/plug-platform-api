package com.pluxity.domains.sasang.station;

import com.pluxity.domains.sasang.station.SasangStation;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Added import
import org.springframework.data.repository.query.Param; // Added import
import org.springframework.stereotype.Repository;

@Repository
public interface SasangStationRepository extends JpaRepository<SasangStation, Long> {
    Optional<SasangStation> findByExternalCode(String externalCode);

    @Query("SELECT ss FROM SasangStation ss JOIN ss.station s JOIN s.facility f WHERE f.code = :code")
    Optional<SasangStation> findByCode(@Param("code") String code);

    @Query("SELECT ss FROM SasangStation ss JOIN ss.station s JOIN s.facility f WHERE f.name = :name")
    Optional<SasangStation> findByName(@Param("name") String name);

    @Query("SELECT ss FROM SasangStation ss JOIN ss.station s JOIN s.facility f WHERE f.name = :name AND ss.id <> :id")
    Optional<SasangStation> findByNameAndIdNot(@Param("name") String name, @Param("id") Long id);

    @Query("SELECT ss FROM SasangStation ss JOIN ss.station s JOIN s.facility f WHERE f.code = :code AND ss.id <> :id")
    Optional<SasangStation> findByCodeAndIdNot(@Param("code") String code, @Param("id") Long id);
}
