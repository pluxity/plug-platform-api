package com.pluxity.facility.repository;

import com.pluxity.facility.entity.Panorama;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PanoramaRepository extends JpaRepository<Panorama, Long> {}
