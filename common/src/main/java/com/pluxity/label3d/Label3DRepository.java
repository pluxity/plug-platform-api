package com.pluxity.label3d;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

@Repository
public interface Label3DRepository extends JpaRepository<Label3D, String> {

    @EntityGraph(attributePaths = {"feature.facility"})
    @Query("SELECT l FROM Label3D l WHERE l.feature.facility.id = :facilityId")
    List<Label3D> findAllByFacilityId(String facilityId);

    @EntityGraph(attributePaths = {"feature.asset"})
    @NonNull
    List<Label3D> findAll();
}
