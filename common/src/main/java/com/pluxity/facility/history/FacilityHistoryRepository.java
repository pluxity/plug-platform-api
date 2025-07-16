package com.pluxity.facility.history;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FacilityHistoryRepository extends JpaRepository<FacilityHistory, Long> {

    List<FacilityHistory> findByFacilityIdOrderByCreatedAtDesc(Long facilityId);
}
