package com.pluxity.facility.station;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StationLineRepository extends JpaRepository<StationLine, Long> {}
