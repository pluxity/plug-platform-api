package com.pluxity.domains.station.repository;

import com.pluxity.domains.station.SasangStationDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SasangStationDetailsRepository extends JpaRepository<SasangStationDetails, Long> {
    Optional<SasangStationDetails> findByExternalCode(String externalCode);
} 