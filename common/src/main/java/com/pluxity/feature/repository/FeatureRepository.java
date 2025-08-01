package com.pluxity.feature.repository;

import com.pluxity.facility.Facility;
import com.pluxity.feature.entity.Feature;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, String> {
    List<Feature> findByFacilityOrderByCreatedAtDesc(Facility facility);
}
