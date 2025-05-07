package com.pluxity.facility.repository;

import com.pluxity.facility.entity.Location;
import com.pluxity.facility.entity.LocationId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, LocationId> {
}
