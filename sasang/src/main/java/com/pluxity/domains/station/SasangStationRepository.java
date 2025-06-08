package com.pluxity.domains.station;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SasangStationRepository extends JpaRepository<SasangStation, Long> {
    Optional<SasangStation> findByExternalCode(String externalCode);

    Optional<SasangStation> findByCode(String code);

    Optional<SasangStation> findByName(String name);

    Optional<SasangStation> findByNameAndIdNot(String name, Long id);

    Optional<SasangStation> findByCodeAndIdNot(String code, Long id);
}
