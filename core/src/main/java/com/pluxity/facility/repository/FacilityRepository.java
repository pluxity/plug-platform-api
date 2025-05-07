package com.pluxity.facility.repository;

import com.pluxity.facility.entity.Facility;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FacilityRepository extends JpaRepository<Facility, Long> {

    @EntityGraph(attributePaths = {"drawingFile", "thumbnailFile"})
    List<Facility> findAll();

    @EntityGraph(attributePaths = {"drawingFile", "thumbnailFile"})
    Optional<Facility> findById(Long id);


}
