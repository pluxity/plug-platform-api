package com.pluxity.panorama;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PanoramaRepository extends JpaRepository<Panorama, Long> {}
