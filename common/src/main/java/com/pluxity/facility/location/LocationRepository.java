package com.pluxity.facility.location;

import com.pluxity.facility.Facility;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findAllByFacility(Facility facility);

    void deleteByFacility(Facility facility);
}
