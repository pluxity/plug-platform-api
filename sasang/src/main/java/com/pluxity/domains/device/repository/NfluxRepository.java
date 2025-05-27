package com.pluxity.domains.device.repository;

import com.pluxity.domains.device.entity.Nflux;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NfluxRepository extends JpaRepository<Nflux, Long> {}
