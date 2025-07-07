package com.pluxity.facility;

import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {

    @NonNull
    List<Facility> findAll();

    @NonNull
    Optional<Facility> findById(@NonNull Long id);

    boolean existsByCode(String code);

    Optional<Facility> findByCode(String code);
}
