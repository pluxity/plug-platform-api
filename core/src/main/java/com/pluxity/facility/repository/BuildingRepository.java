package com.pluxity.facility.repository;

import com.pluxity.facility.entity.Building;
import lombok.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {

    @EntityGraph(attributePaths = {"floors"})
    @NonNull
    List<Building> findAll();

    @EntityGraph(attributePaths = {"floors"})
    @NonNull
    Optional<Building> findById(@NonNull Long id);

}
