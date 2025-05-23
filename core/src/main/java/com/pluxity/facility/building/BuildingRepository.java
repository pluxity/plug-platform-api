package com.pluxity.facility.building;

import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuildingRepository extends JpaRepository<Building, Long> {

    @NonNull
    List<Building> findAll();

    @NonNull
    Optional<Building> findById(@NonNull Long id);
}
