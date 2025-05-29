package com.pluxity.domains.station;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SasangStationRepository extends JpaRepository<SasangStation, Long> {
    Optional<SasangStation> findByExternalCode(String externalCode);
}
