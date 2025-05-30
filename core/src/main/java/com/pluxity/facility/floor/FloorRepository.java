package com.pluxity.facility.floor;

import com.pluxity.facility.facility.Facility;
import java.util.List;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FloorRepository extends JpaRepository<Floor, Long> {

    @BatchSize(size = 2)
    List<Floor> findAllByFacility(Facility facility);

    @Query("SELECT f FROM Floor f WHERE f.facility IN :facilities")
    <T extends Facility> List<Floor> findAllByFacilities(List<T> facilities);

    @Modifying
    @Query("DELETE FROM Floor f WHERE f.facility = :facility")
    void deleteByFacility(Facility facility);
}
