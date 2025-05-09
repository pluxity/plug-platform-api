package com.pluxity.facility.repository;

import com.pluxity.facility.entity.Facility;
import com.pluxity.facility.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findAllByFacility(Facility facility);

    void deleteByFacility(Facility facility);
}
