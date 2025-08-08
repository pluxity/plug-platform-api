package com.pluxity.feature.repository;

import com.pluxity.facility.Facility;
import com.pluxity.feature.entity.Feature;
import com.pluxity.feature.entity.FeatureType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, String> {
    List<Feature> findByFacilityOrderByCreatedAtDesc(Facility facility);

    List<Feature> findByFacilityAndTypeOrderByCreatedAtDesc(Facility facility, FeatureType type);

    List<Feature> findByAssetId(Long assetId);
}
