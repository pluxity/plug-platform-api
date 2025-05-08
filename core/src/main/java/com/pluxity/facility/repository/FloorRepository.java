package com.pluxity.facility.repository;

import com.pluxity.facility.entity.Facility;
import com.pluxity.facility.entity.Floor;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

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
