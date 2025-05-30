package com.pluxity.facility.facility;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacilityRevisionRepository
        extends RevisionRepository<Facility, Long, Integer>, JpaRepository<Facility, Long> {}
