package com.pluxity.facility.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacilityCategoryRepository extends JpaRepository<FacilityCategory, Long> {}
