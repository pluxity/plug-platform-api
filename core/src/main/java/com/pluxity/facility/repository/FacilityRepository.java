package com.pluxity.facility.repository;

import com.pluxity.facility.entity.Facility;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {

    @NonNull
    List<Facility> findAll();

    @NonNull
    Optional<Facility> findById(@NonNull Long id);


}
