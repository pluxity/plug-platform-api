package com.pluxity.domains.device.repository;

import com.pluxity.domains.device.entity.SpaceText;
import jakarta.annotation.Nonnull;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SpaceTextRepository extends JpaRepository<SpaceText, String> {

    @Override
    @Nonnull
    @EntityGraph(attributePaths = {"feature.facility"})
    List<SpaceText> findAll();

    @Override
    @Nonnull
    @EntityGraph(attributePaths = {"feature.facility"})
    Optional<SpaceText> findById(@Nonnull String id);

    @EntityGraph(attributePaths = {"feature.facility"})
    @Query("SELECT st FROM SpaceText st WHERE st.feature.facility.id = :facilityId")
    List<SpaceText> findAllByFacilityId(String facilityId);
}
