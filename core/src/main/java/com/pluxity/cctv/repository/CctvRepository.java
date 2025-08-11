package com.pluxity.cctv.repository;

import com.pluxity.cctv.entity.Cctv;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CctvRepository extends JpaRepository<Cctv, String> {}
